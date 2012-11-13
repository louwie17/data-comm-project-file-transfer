import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransferProtocolClient
{
    private static final int WINDOW_SIZE = 50;
    private static final int MAX_SEQUENCE = 255;

    private DatagramSocket socket;
    private InetAddress address;
    private int port;
    private long numPackets;
    private IChunkHandler handler;
    
    /**
     * Creates a new protocol client to handle the transfer of chunkCount
     * number of packets.
     * @param aSocket the socket to receive from
     * @param aAddress the address of the client to send data to
     * @param aPort the port of the client to send data to
     * @param chunkCount the number of Chunk objects to receive
     * @param aListener the chunk handler which will be presented with the
     * received data
     * @throws IllegalArgumentException if aSocket, aAddress or aSupplier are
     * null or if chunkCount < 0 or if port < 0 || > 65536
     */
    public TransferProtocolClient(DatagramSocket aSocket,
                                  InetAddress aAddress, int aPort,
                                  long chunkCount, IChunkHandler aListener)
    {
        if (aSocket == null)
            throw new IllegalArgumentException("Created with null socket.");
        if (aAddress == null)
            throw new IllegalArgumentException("Created with null address.");
        if (aPort < 0 || aPort > 65536)
            throw new IllegalArgumentException("Created with port outside " +
                "valid range (" + aPort + ")");
        if (aListener == null)
            throw new IllegalArgumentException("Created with null listener.");
        if (chunkCount < 0)
            throw new IllegalArgumentException("Created with chunks < 0 (" +
                chunkCount + ")");
        socket = aSocket;
        address = aAddress;
        port = aPort;
        numPackets = chunkCount;
        handler = aListener;
        listen();
    }
    
    private void listen()
    {
        DatagramPacket packet = new DatagramPacket(
            new byte[Chunk.TOTAL_BYTES], Chunk.TOTAL_BYTES);
        long received = 0;
        int index;
        List<Chunk> buffer = new ArrayList<Chunk>();
        
        while (received < numPackets)
        {
            try
            {
                socket.receive(packet);
            }
            catch (IOException e)
            {
                System.out.println("Error listening.");
                return;
            }
            Chunk c = new Chunk(Arrays.copyOf(packet.getData(),
                packet.getData().length));
            
            int lowerBound = (int) received % MAX_SEQUENCE;
            int upperBound = (lowerBound + WINDOW_SIZE) % MAX_SEQUENCE;
            int seq = c.getSequenceNumber();
            
            System.out.println("Lower bound: " + lowerBound + "  Upper: " +
                upperBound);
                
            if (!c.checkCRC())
            {
                System.out.println("CRC failed.  Ignoring packet.");
                continue;
            }
            if (lowerBound < upperBound &&
                (seq < lowerBound || seq > upperBound))
            {
                fireACK(seq);
                continue;
            }
            if (lowerBound < lowerBound && seq < lowerBound &&
                seq > lowerBound)
            {
                fireACK(seq);
                continue;
            }
            
            java.util.Random r = new java.util.Random();
            if (r.nextInt(10) == 1)
            {
                System.out.println("Throwing away packet...");
                continue;
            }
            
            buffer.add(c);
            System.out.println("Received: " + seq);
            fireACK(seq);
            
            // flush buffer if we can
            while ((index = bufferContainsChunk(buffer, lowerBound)) >= 0)
            {
                System.out.println("Flushing buffer: " + received);
                handler.receiveData(buffer.remove(index));
                received++;
                lowerBound = (int) received % MAX_SEQUENCE;
            }
        }
        handler.finish();
    }
    
    /*private void printBuffer(List<Chunk> buffer)
    {
        List<String> print = new ArrayList<String>();
        for (Chunk c : buffer)
            print.add(c + "" + c.getSequenceNumber());
        System.out.println("Buffer: " + print);
    }*/
    
    private int bufferContainsChunk(List<Chunk> buffer, int number)
    {
        for (int i = 0; i < buffer.size(); i++)
        {
            Chunk c = buffer.get(i);
            if (c != null && c.getSequenceNumber() == number)
                return i;
        }
        return -1;
    }
    
    private boolean fireACK(int number)
    {
        ByteBuffer bb = ByteBuffer.allocate(6);
        bb.putInt(number).putShort((short) 3);
        
        System.out.println("FireACK: " + number);
        
        DatagramPacket packet = new DatagramPacket(bb.array(), 6,
            address, port);
        try
        {
            socket.send(packet);
            return true;
        }
        catch (IOException e)
        {
            return false;
        }
    }
}






