import java.net.*;
import java.util.*;
import java.io.*;


public class EchoClient
{

  private static final int BUFFER_SIZE = 512;
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
      //socket.connect(InetAddress.getByName(ip), PORT);
      byte[] buffer = new byte[BUFFER_SIZE];
      String cmd = "hi";
      DatagramPacket connect = new DatagramPacket(cmd.getBytes(), 
          cmd.getBytes().length, 
          InetAddress.getByName(ip), PORT);
      socket.send(connect);
      System.out.println("send request"+connect.getAddress());
      DatagramPacket received = new DatagramPacket(buffer, buffer.length);
      System.out.println("waiting for reply"); 
      socket.receive(received);
      String pack = new String(received.getData(), 0, received.getLength());
      System.out.println(pack); 
      if (pack.equals("what?"))
      {
          System.out.println("Connected to: " + received.getAddress());
      
          System.out.println("File to transfer: ");
          filename = scan.nextLine();
          if (filename.equals("quit"))
            break;
          String getf = "GetFile";
          DatagramPacket askFile = new DatagramPacket(getf.getBytes(),
              getf.getBytes().length, received.getAddress(), 
              received.getPort());
          socket.send(askFile);
          socket.receive(received);
          pack = new String(received.getData(),0,received.getLength());
          if (pack.equals("OKGO"))
          {
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
          if (!pack.equals("Valid"))
          {
            System.out.println(pack);
            continue;
          }
          System.out.println(pack);
          String[] splitName = filename.split("/");
          File newFile = new File("lcaol_" + 
              splitName[splitName.length -1]);
          if (newFile.exists())
          {
            System.out.println("Error: can't write to disk");
            continue;
          }

          newFile.createNewFile();

          FileOutputStream fos = new FileOutputStream(newFile);

          while (true)
          {
            socket.receive(received);
            if (received.getLength() <= 10)
            {
              cmd = new String(received.getData(), 0,
                  received.getLength());
              if (cmd.equals("END_FILE"))
              {
                System.out.println("received file");
                break;
              }
            }  
            fos.write(received.getData(), 0, received.getLength());
          }
      }
    }

    /**DatagramPacket packet = new DatagramPacket(buffer, buffer.length, 
        InetAddress.getByName(args[0]),PORT);
    */
    /**socket.send(packet);
    Date timeSent = new Date();
    socket.receive(packet);
    Date timeReceived = new Date();

    System.out.println("" + (timeReceived.getTime()-timeSent.getTime()) + 
        " ms " + new String(packet.getData(),0,packet.getLength()));
    */
  }
}
    }  
            fos.write(received.getData(), 0, received.getLength());
          }
      }
    }

    /**DatagramPacket packet = new DatagramPacket(buffer, buffer.length, 
        InetAddress.getByName(args[0]),PORT);
    */
    /**socket.send(packet);
    Date timeSent = new Date();
    socket.receive(packet);
    Date timeReceived = 