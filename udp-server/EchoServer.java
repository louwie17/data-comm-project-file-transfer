import java.net.*;
import java.util.*;
import java.io.*;
import java.nio.*;

public class EchoServer {

  public static final int PORT = 4444;
  public static final int BUFFER = 512;

  public static void main(String args[] ) throws Exception
  {
    DatagramSocket socket = new DatagramSocket(PORT);
    DatagramPacket packet = new DatagramPacket(new byte[BUFFER], BUFFER);
    boolean ok = false;
    System.out.println("Listening on port: " + PORT);
    while (true)
    {
      socket.receive(packet);
      String received = new String(packet.getData(), 0, packet.getLength());
      if (received.equals("hi"))
      {
        System.out.println("Connected to:  " + packet.getAddress() +
            " at port: " + packet.getPort()); 
        String reply = "what?";
        DatagramPacket repPacket = new DatagramPacket(reply.getBytes(),
            reply.getBytes().length, packet.getAddress(), packet.getPort());
        socket.send(repPacket);
        run(socket, packet);
      }
      
    }

  }

  public static void run(DatagramSocket sock, DatagramPacket pack) 
    throws Exception
  {
    DatagramSocket socket = sock;
    DatagramPacket packet = pack;
    boolean validFile = false;
    boolean ok = false;

    while (true)
    {
      socket.receive(packet);
      String request = new String(packet.getData(), 0, packet.getLength());
      if (request.equals("GetFile") && ok == false)
      {
        String ready = "OKGO";
        DatagramPacket okgo = new DatagramPacket(ready.getBytes(),
            ready.getBytes().length, packet.getAddress(),
            packet.getPort());
        socket.send(okgo);
        ok = true;
      }
      System.out.println(ok + request);
      if (ok == true )
      {
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
          replyStr = "Valid";
          validFile = true;
        }
        DatagramPacket error = new DatagramPacket(replyStr.getBytes(),
            replyStr.getBytes().length, packet.getAddress(),
            packet.getPort());
        socket.send(error);
        if (!validFile)  
          return;

        FileInputStream fin = new FileInputStream(file);
        DatagramPacket filePack;

        int size = 0;
        byte[] buffer = new byte[BUFFER];
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.BIG_ENDIAN);
        
        
        System.out.println("Start transfer..");        
        while ((size = fin.read(buffer)) > 0)
        {
          filePack = new DatagramPacket(buffer, buffer.length,
              packet.getAddress(), packet.getPort());
          socket.send(filePack);
        }
        System.out.println("Finished transfer");
        String done = "END_FILE";
        filePack = new DatagramPacket(done.getBytes(),
            done.getBytes().length,packet.getAddress(),
            packet.getPort());
        socket.send(filePack);
      }
    }
  }

}
