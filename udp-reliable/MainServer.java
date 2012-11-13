import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

public class MainServer
{
    public static final int PORT = 4444;
    public static final int BUFFER = 1024;
    private static final int MAX_SEQUENCE = 255;

    public static void main(String args[] )
    {
        try
        {
            DatagramSocket socket = new DatagramSocket(PORT);
            DatagramPacket packet = new DatagramPacket(
                new byte[BUFFER], BUFFER);
            boolean ok = false;
            System.out.println("Listening on port: " + PORT);
            while (true)
            {
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

    public static void run(DatagramSocket sock, DatagramPacket pack) 
    {
        try
        {
            DatagramSocket socket = sock;
            DatagramPacket packet = pack;
            boolean validFile = false;
            boolean ok = false;
            String request = "";

            String ready = "OKGO";
            DatagramPacket okgo = new DatagramPacket(ready.getBytes(),
                ready.getBytes().length, packet.getAddress(),
                packet.getPort());
            socket.send(okgo);

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
            DatagramPacket error = new DatagramPacket(replyStr.getBytes(),
                replyStr.getBytes().length, packet.getAddress(),
                packet.getPort());
            socket.send(error);
            if (!validFile)
            {  
                System.out.println("File name invalid!!");
                return;
            }
            final FileInputStream fin = new FileInputStream(file);
            DatagramPacket filePack;

            System.out.println("Start transfer..");
            new TransferProtocolServer(socket, packet.getAddress(),
                packet.getPort(),
                (long) Math.ceil(file.length() * 1.0 / Chunk.DATA_BYTES),
                new IChunkSupplier(){
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
                });
            System.out.println("Finished transfer");
            fin.close();
            return; 
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
