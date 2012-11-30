import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The server side implementation of the reliable transfer protocol.
 */
public class TransferProtocolServer
{
    private static final int WINDOW_SIZE = 127;
    private static final int TIMEOUT_MS = 300;

    private DatagramSocket socket;
    private InetAddress address;
    private int port;
    private long numPackets;
    private IChunkSupplier supplier;

    private Map<Chunk,Long> sentNeedACK;
    private Map<Chunk,Timer> timers;
    private long windowBase;
    private int sentAndACKed;
    private long sent = 0;

    /**
     * Creates a new protocol client to handle the transfer of chunkCount
     * number of packets.
     * @param aSocket the socket to receive from
     * @param aAddress the address of the client to send data to
     * @param aPort the port of the client to send data to
     * @param chunkCount the number of Chunk objects to receive
     * @param aSupplier the chunk supplier from which data will be requested
     * @throws IllegalArgumentException if aSocket, aAddress or aSupplier are
     * null or if chunkCount < 0 or if port < 0 || > 65536
     */
    public TransferProtocolServer(DatagramSocket aSocket,
        InetAddress aAddress, int aPort,
        long chunkCount, IChunkSupplier aSupplier)
    {
        if (aSocket == null)
            throw new IllegalArgumentException("Created with null socket.");
        if (aAddress == null)
            throw new IllegalArgumentException("Created with null address.");
        if (aPort < 0 || aPort > 65536)
            throw new IllegalArgumentException("Created with port outside " +
                "valid range (" + aPort + ")");
        if (aSupplier == null)
            throw new IllegalArgumentException("Created with null supplier.");
        if (chunkCount < 0)
            throw new IllegalArgumentException("Created with chunks < 0 (" +
                chunkCount + ")");
        socket = aSocket;
        address = aAddress;
        port = aPort;
        numPackets = chunkCount;
        supplier = aSupplier;

        // prepare & start sending
        windowBase = 0;
        sentAndACKed = 0;
        sentNeedACK = new HashMap<Chunk,Long>();
        timers = new HashMap<Chunk,Timer>();
        fillSent();
        receiveACKS();
    }

    /**
     * If possible, fills the window by sending more chunks.
     */
    private void fillSent()
    {
        Chunk next;
        while (sentNeedACK.size() < WINDOW_SIZE - sentAndACKed &&
            (next = supplier.nextChunk()) != null)
        {
            fireData(next, false);
        }
    }

    /**
     * Listens for and handles ACK packets.
     */
    private void receiveACKS()
    {
        report("Waiting for acks...");
        while (windowBase < numPackets) //sentNeedACK.size() > 0)
        {
            DatagramPacket datagram = new DatagramPacket(
                new byte[ACK.TOTAL_BYTES], ACK.TOTAL_BYTES);
            try
            {
                socket.receive(datagram);
            }
            catch (IOException e)
            {
                report("Error receiving ack.");
                System.exit(1);
            }
            ACK ack = new ACK(datagram.getData());

            // packet is invalid
            if (!ack.checkCRC() || !ack.checkCode())
            {
                report("Invalid ACK received with sequence " +
                    "number " + ack.getSequenceNumber());
                continue;
            }

            int sequence = ack.getSequenceNumber();
            report("Received ACK: " + sequence);
            Chunk m = null;
            for (Chunk c : sentNeedACK.keySet())
                if (c.getSequenceNumber() == sequence)
                    m = c;
            if (m != null)
            {
                if (sentNeedACK.get(m) == windowBase)
                {
                    sentNeedACK.remove(m);
                    windowBase++;
                    report("ACK was lowest.  Incrementing" +
                        "window.  (base: " + windowBase + ")");

                    if (sentNeedACK.size() == 0)
                    {
                        windowBase += WINDOW_SIZE - 1;
                        sentAndACKed = 0;
                        report("Empty window.  Fire!");
                        fillSent();
                    }
                    else
                    {
                        for (int i = 0; i < WINDOW_SIZE - 1; i++)
                        {
                            boolean inWindow = false;
                            for (Chunk c : sentNeedACK.keySet())
                            {
                                if (sentNeedACK.get(c) == windowBase)
                                {
                                    inWindow = true;
                                    break;
                                }
                            }
                            if (!inWindow)
                            {
                                sentAndACKed--;
                                if (sentAndACKed < 0)
                                {
                                    report("sentAndACKed got negative...");
                                    System.exit(0);
                                }
                                windowBase++;
                                report("windowBase++ = " + windowBase);
                            }
                        }
                        fillSent();
                    }
                }
                else
                {
                    sentNeedACK.remove(m);
                    sentAndACKed++;
                }
                Timer t = timers.remove(m);
                if (t != null)
                    t.cancel();
            }
        }
        System.out.println("windowBase: " + windowBase + " numPackets: " +
            numPackets);

        report("Out of ACK waiting loop.");
    }

    /**
     * Fires a chunk of data.
     * @param chunk the chunk to fire
     * @param isResend whether this is a resend or not
     */
    private boolean fireData(Chunk chunk, boolean isResend)
    {
        if (!isResend)
            sentNeedACK.put(chunk, sent++);
        report("FireData: " + sentNeedACK.get(chunk) +
            " (Seq#: " + chunk.getSequenceNumber() + ")");
        try
        {
            final Chunk sentChunk = chunk;
            Timer timer = new Timer();
            timers.put(chunk, timer);
            DatagramPacket packet = new DatagramPacket(chunk.getPacket(),
                chunk.getPacket().length, address, port);
            timer.schedule(new TimerTask(){
                public void run()
                {
                    if (sentNeedACK.containsKey(sentChunk))
                        fireData(sentChunk, true);
                }
            }, TIMEOUT_MS);
            socket.send(packet);
            return true;
        }
        catch (IOException e)
        {
            return false;
        }
    }

    /**
     * Handles the reporting of a message.
     * @param message the message to report
     */
    private static void report(String message)
    {
        System.out.println(message);
    }
}
