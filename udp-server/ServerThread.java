import java.net.*;
import java.io.*;
import java.nio.*;
import java.util.Scanner;

public class ServerThread extends Thread
{
    private DatagramSocket socket = null;
    private DatagramPacket packet = null;

    public ServerThread(DatagramSocket socket, DatagramPacket packet)
    {
        this.packet = packet;
        this.socket = socket;
        System.out.println("Starting new thread...");
    }

    public void run()
    {
        System.out.println("Accepted connection : " + socket);
        try
        {
            String cmd = new String(packet.getData(), 0, packet.getLength());
            System.out.println("file  " + cmd);
            // get request
            File file = new File(cmd);

            // send response
            boolean ok = false;
            if (!file.exists())
                System.out.println("Error: file doesn't exist.");
            else if (!file.isFile())
                System.out.println("Error: not a file.");
            else if (!file.canRead())
                System.out.println("Error: can't read from file.");
            else
            {
                System.out.println("OKGO");
                ok = true;
            }

            if (!ok)
                return;

            // setup streams
            FileInputStream fin = new FileInputStream(cmd);
            DatagramPacket pack;


            // write file to socket
            System.out.println("Sending Files...");
            int size = 0;
            byte[] buffer = new byte[8192];
            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.order(ByteOrder.BIG_ENDIAN);

            while (true) {
              size = fin.read(buffer);
              System.out.println("Size = " + size);

              pack = new DatagramPacket(buffer, buffer.length, 
                  this.packet.getAddress(), packet.getPort());
              socket.send(pack);
              if (size == -1)
                break;

            }
            
            pack = new DatagramPacket(cmd.getBytes(), cmd.getBytes().length,
                this.packet.getAddress(), packet.getPort());
            socket.send(pack);
            // cleanup
            fin.close();

            System.out.println("File transfer complete.");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
