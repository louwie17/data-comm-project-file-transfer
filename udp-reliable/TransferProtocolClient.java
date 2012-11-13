import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class TransferProtocolClient
{
    private static final int WINDOW_SIZE = 50;
    private static final int MAX_SEQUENCE = 255;

    private DatagramSocket socket;
    private long numPackets;
    private IChunkHandler handler;
    
    /**
     * Creates a new protocol client to handle the transfer of chunkCount
     * number of packets.
     * @param aSocket the socket to receive from
     * @param chunkCount the number of Chunk objects to receive
     * @param aListener the chunk handler which will be presented with the
     * received data
     * @throws IllegalArgumentException if aSocket or aListener are null
     * or if chunkCount < 0.
     */
    public TransferProtocolClient(DatagramSocket aSocket, long chunkCount,
                                  IChunkHandler aListener)
    {
        if (aSocket == null)
            throw new IllegalArgumentException("Created with null socket.");
        if (aListener == null)
            throw new IllegalArgumentException("Created with null listener.");
        if (chunkCount < 0)
            throw new IllegalArgumentException("Created with chunks < 0 (" +
                chunkCount + ")");
        socket = aSocket;
        numPackets = chunkCount;
        handler = aListener;
        listen();
    }
    
    private void listen()
    {
        DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
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
            Chunk c = new Chunk(packet.getData());
            buffer.add(c);
            
            int lowerBound = (int) received % MAX_SEQUENCE;
            int upperBound = (lowerBound + WINDOW_SIZE) % MAX_SEQUENCE;
            
            System.out.println("Lower bound: " + lowerBound + "  Upper: " +
                upperBound);
            
            if (c.getSequenceNumber() < lowerBound)
            {
                fireACK(c.getSequenceNumber());
                // send ACK();
                System.out.println("ACK: " + c.getSequenceNumber());
                continue;
            }
            else if (c.getSequenceNumber() > upperBound)
            {
                // ignore
                System.out.println("Ignore: " + c.getSequenceNumber());
                continue;
            }
            
            // flush buffer if we can
            while ((index = bufferContainsChunk(buffer, lowerBound)) >= 0)
            {
                handler.receiveData(buffer.remove(index));
                received++;
                lowerBound = (int) received % MAX_SEQUENCE;
            }
        }
        handler.finish();
    }
    
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
        
        DatagramPacket packet = new DatagramPacket(bb.array(), 6);
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







