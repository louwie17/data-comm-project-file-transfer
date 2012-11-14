import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

/**
 * The core server class for the project.
 */
public class MainServer
{
    private static final int PORT = 4444;
    private static final int BUFFER = 1024;
    private static final int MAX_SEQUENCE = 255;

    public static void main(String args[])
    {
        try
        {
            // prepare socket & packet
            DatagramSocket socket = new DatagramSocket(PORT);
            DatagramPacket packet = new DatagramPacket(
                new byte[BUFFER], BUFFER);
            boolean ok = false;

            // listen
            System.out.println("Listening on port: " + PORT);
            while (true)
            {
                // handle request for file
                socket.receive(packet);
                String received = new String(packet.getData(), 0,
                    packet.getLength());
                if (received.equals("GET_FILE"))
                {
                    System.out.println("Request from:  " +
                        packet.getAddress() + " at port: " + packet.getPort());
                    run(socket, packet);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Handles the transfer of a file.
     * @param socket the socket to use
     * @param packet the packet to use
     */
    private static void run(DatagramSocket socket, DatagramPacket packet) 
    {
        try
        {
            boolean validFile = false;
            boolean ok = false;
            String request = "";

            // reply to client
            String ready = "OKGO";
            DatagramPacket okgo = new DatagramPacket(ready.getBytes(),
                ready.getBytes().length, packet.getAddress(),
                packet.getPort());
            socket.send(okgo);

            // get file name request
            socket.receive(packet);
            request = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Filename: " + request);
            final File file = new File(request);
            String replyStr = "";
            if (!file.exists())
                replyStr = "Error: file doesn't exist.";
            else if (!file.isFile())
                replyStr = "Error: not a file";
            else if (!file.canRead())
                replyStr = "Error: can't read from file.";
            else
            {
                replyStr = Long.toString(file.length());
                validFile = true;
            }

            // reply with either the file size in bytes or an error message
            DatagramPacket error = new DatagramPacket(replyStr.getBytes(),
                replyStr.getBytes().length, packet.getAddress(),
                packet.getPort());
            socket.send(error);
            if (!validFile)
            {  
                System.out.println("File name invalid!!");
                return;
            }

            // open the file input stream
            final FileInputStream fin = new FileInputStream(file);

            System.out.println("Start transfer..");

            // hand the transfer off to the reliable protocol
            new TransferProtocolServer(socket, packet.getAddress(),
                packet.getPort(),
                (long) Math.ceil(file.length() * 1.0 / Chunk.DATA_BYTES),
                createChunkSupplier(file, fin));

            System.out.println("Finished transfer");
            fin.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Create a chunk supplier from a given file and file input stream.
     * @param file the source file
     * @param fin the source input stream
     * @return a chunk supplier
     */
    private static IChunkSupplier createChunkSupplier(final File file,
        final FileInputStream fin)
    {
        return new IChunkSupplier(){
            private boolean done = false;
            private long sent = 0;
            private long total = 0;

            public Chunk nextChunk()
            {
                if (done)
                    return null;
                int size;
                byte[] buffer = new byte[Chunk.DATA_BYTES];
                try
                {
                    if ((size = fin.read(buffer)) > 0)
                    {
                        total += size;
                        if (total >= file.length())
                            done = true;
                        sent++;
                        return new Chunk(buffer,
                            (int) (sent - 1) % MAX_SEQUENCE);
                    }
                }
                catch (IOException e)
                {
                    System.out.println("Error reading from file.");
                    System.exit(1);
                }
                // shouldn't get here
                done = true;
                return null;
            }
        };
    }
}
