import java.net.*;
import java.util.*;
import java.io.*;


public class EchoClient
{

  private static final int BUFFER = 1024;
  private static final int PORT = 4444;


  public static void main( String args[] ) throws Exception {
    
    DatagramSocket socket = new DatagramSocket();
    socket.setSoTimeout(5000);
    
    Scanner scan = new Scanner(System.in);
    String ip;
    String filename = null;
    int bytesRead = 0;



    while (true)
    {
      System.out.print("Server IP: ");
      ip = scan.nextLine();
      if (ip.equals("quit"))
        System.exit(0);
      DatagramPacket connect = null;
      DatagramPacket received = null; 
      String cmd = "";
      byte[] buffer = new byte[BUFFER];
      try {
        cmd = "GET_FILE";
        connect = new DatagramPacket(cmd.getBytes(), 
            cmd.getBytes().length, 
            InetAddress.getByName(ip), PORT);
        socket.send(connect);
      }
      catch (IOException e)
      {
        System.out.println("could not connect.");
        continue;
      }
      System.out.println("send request"+connect.getAddress());
      try {
        received = new DatagramPacket(buffer, buffer.length);
        socket.receive(received);
      }
      catch (SocketTimeoutException e)
      {
        System.out.println("server took to long");
        continue;
      }
      String pack = new String(received.getData(), 0, received.getLength());
      if (pack.equals("OKGO"))
      {
          System.out.println("File to transfer: ");
          filename = scan.nextLine();
          if (filename.equals("quit"))
            break;
          System.out.println("good to go");
          DatagramPacket fileName = new DatagramPacket(filename.getBytes(),
              filename.getBytes().length, InetAddress.getByName(ip),
              PORT);
          socket.send(fileName);
        }
        else
          continue;

        socket.receive(received);
        pack = new String(received.getData(),0,received.getLength());
        System.out.println(pack);
        if (pack.startsWith("Error"))
        {
          continue;
        }
        String[] splitName = filename.split("/");
        File newFile = new File("local_" + 
            splitName[splitName.length -1]);
        if (newFile.exists())
        {
          System.out.println("Error: can't write to disk");
          continue;
        }

        newFile.createNewFile();
        Long fileSize = Long.parseLong(pack);
        FileOutputStream fos = new FileOutputStream(newFile);
        long total = 0;
        long totalLeft = fileSize;
        while (true)
        {
          socket.receive(received);
          if (total >= fileSize || received.getLength() <= 9)
          {
            System.out.println(total >= fileSize);
            cmd = new String(received.getData(), 0,
                received.getLength());
            if (cmd.equals("END_FILE") || total >= fileSize)
            {
              System.out.println("received file:"+received.getLength());
              break;
            }
          }
          total += (long) received.getLength();
         System.out.println(total + " filesize: " + Long.parseLong(pack));
        if (((received.getLength() / totalLeft)) >= 1)
        { 
          System.out.println(totalLeft + " left gotten: " + received.getLength());
          fos.write(received.getData(), 0, (int) totalLeft);
          
        }
        else
          fos.write(received.getData(), 0, received.getLength());
        totalLeft -= received.getLength();
        }
    }
  }
}
