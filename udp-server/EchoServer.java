import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

public class EchoServer
{
    public static final int PORT = 4444;
    public static final int BUFFER = 1024;

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
            File file = new File(request);
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
            FileInputStream fin = new FileInputStream(file);
            DatagramPacket filePack;

            int size = 0;
            byte[] buffer = new byte[BUFFER];
            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.order(ByteOrder.BIG_ENDIAN);

            System.out.println("Start transfer..");
            int total = 0;    
            while ((size = fin.read(buffer)) > 0)
            {
                total += size;
                filePack = new DatagramPacket(buffer, buffer.length,
                    packet.getAddress(), packet.getPort());
                socket.send(filePack);
                try
                {
                    Thread.sleep(0, 3);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            System.out.println("Finished transfer");
            fin.close();
            String done = "END_FILE";
            filePack = new DatagramPacket(done.getBytes(),
                done.getBytes().length,packet.getAddress(),
                packet.getPort());
            socket.send(filePack);
            return; 
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
