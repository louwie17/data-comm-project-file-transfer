import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.HashMap;

public class TransferProtocolServer
{
    private static final int WINDOW_SIZE = 50;
    private static final int TIMEOUT_MS = 300;

    private DatagramSocket socket;
    private long numPackets;
    private IChunkSupplier supplier;
    
    private Map<Chunk,Long> sentNeedACK;
    private long windowBase;
    private long canSend;
    
    /**
     * Creates a new protocol client to handle the transfer of chunkCount
     * number of packets.
     * @param aSocket the socket to receive from
     * @param chunkCount the number of Chunk objects to receive
     * @param aSupplier the chunk supplier from which data will be requested
     * @throws IllegalArgumentException if aSocket or aSupplier are null
     * or if chunkCount < 0.
     */
    public TransferProtocolServer(DatagramSocket aSocket, long chunkCount,
                                  IChunkSupplier aSupplier)
    {
        if (aSocket == null)
            throw new IllegalArgumentException("Created with null socket.");
        if (aSupplier == null)
            throw new IllegalArgumentException("Created with null supplier.");
        if (chunkCount < 0)
            throw new IllegalArgumentException("Created with chunks < 0 (" +
                chunkCount + ")");
        socket = aSocket;
        numPackets = chunkCount;
        supplier = aSupplier;

        // prepare & start sending
        windowBase = 0;
        canSend = WINDOW_SIZE;
        sentNeedACK = new HashMap<Chunk,Long>();
        fillSent();
        receiveACKS();
    }
    
    private void fillSent()
    {
        Chunk next;
        while (windowBase < canSend && sentNeedACK.size() < WINDOW_SIZE &&
                (next = supplier.nextChunk()) != null)
        {
            fireData(next);
        }
    }
    
    private void receiveACKS()
    {
        while (sentNeedACK.size() > 0)
        {
            DatagramPacket ack = new DatagramPacket(new byte[6], 6);
            try
            {
                socket.receive(ack);
            }
            catch (IOException e)
            {
                System.out.println("Error receiving ack.");
                System.exit(1);
            }
            ByteBuffer bb = ByteBuffer.allocate(6);
            bb.put(ack.getData());
            int sequence = bb.getInt(0);
            Chunk m = null;
            for (Chunk c : sentNeedACK.keySet())
                if (c.getSequenceNumber() == sequence)
                    m = c;
            if (m != null)
            {
                if (sentNeedACK.get(m) == windowBase)
                {
                    windowBase++;
                    canSend++;
                    fillSent();
                }
                sentNeedACK.remove(m);
            }
        }
    }
    
    private boolean fireData(Chunk chunk)
    {
        DatagramPacket packet = new DatagramPacket(chunk.getPacket(),
            chunk.getPacket().length);
        sentNeedACK.put(chunk, windowBase + sentNeedACK.size());
        try
        {
            socket.send(packet);
            // add timer
            return true;
        }
        catch (IOException e)
        {
            return false;
        }
    }   
}







