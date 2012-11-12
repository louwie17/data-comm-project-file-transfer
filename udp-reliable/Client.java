import java.io.*;
import java.net.*;
import java.util.*;

public class Client
{
    private static final int BUFFER = 1024;
    private static final int PORT = 4444;

    public static void main( String args[] )
    {
        try
        {
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
                    System.out.println("Could not connect.");
                    continue;
                }
                System.out.println("send request"+connect.getAddress());
                try {
                    received = new DatagramPacket(buffer, buffer.length);
                    socket.receive(received);
                }
                catch (SocketTimeoutException e)
                {
                    System.out.println("Server took too long to respond.");
                    continue;
                }
                String pack = new String(
                    received.getData(), 0, received.getLength());
                if (pack.equals("OKGO"))
                {
                    System.out.println("File to transfer: ");
                    filename = scan.nextLine();
                    if (filename.equals("quit"))
                        break;
                    System.out.println("Good to go.");
                    DatagramPacket fileName = new DatagramPacket(
                        filename.getBytes(), filename.getBytes().length,
                        InetAddress.getByName(ip), PORT);
                    socket.send(fileName);
                }
                else
                    continue;

                socket.receive(received);
                pack = new String(received.getData(),0,received.getLength());
                if (pack.startsWith("Error"))
                    continue;
                String[] splitName = filename.split("/");
                File newFile = new File("local_" + 
                    splitName[splitName.length - 1]);
                if (newFile.exists())
                {
                    System.out.println("Error: can't write to disk");
                    continue;
                }

                newFile.createNewFile();
                Long fileSize = Long.parseLong(pack);
                final FileOutputStream fos = new FileOutputStream(newFile);
                long total = 0;
                long totalLeft = fileSize;
                
                new TransferProtocolClient(socket, 
                        Math.ceil(fileSize *1.0 / 1011), 
                        new IChunkHandler()
                        {
                            public void receiveData(Chunk data)
                            {
                                if (received.getLength() >= totalLeft )
                                {
                                    fos.write(data.getData(), 0, 
                                        (int) totalLeft);
                                    total += (long) totalLeft;
                                }
                                else
                                {
                                    fos.write(data.getData(), 0, 
                                        data.getData().getLength());
                                    total += (long) received.getLength();
                                }

                                totalLeft -= received.getLength();
                                double percentage = ((double) total / 
                                    (double) fileSize) * 100;
                                System.out.print((int) percentage +
                                        "%" +"\r");
 
                            }

                            public void finish()
                            {
                                fos.close();
                            }
                        }); 
        
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();

        }
    }
}
