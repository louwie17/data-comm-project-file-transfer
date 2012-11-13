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
                if (!scan.hasNextLine())
                    break;
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
                final Long fileSize = Long.parseLong(pack);
                final FileOutputStream fos = new FileOutputStream(newFile);
                final long receivedLength = received.getLength();
                
                System.out.println("Expected file size: " +
                    fileSize);
                
                new TransferProtocolClient(socket, InetAddress.getByName(ip),
                        PORT,
                        (long) Math.ceil(fileSize * 1.0 / Chunk.DATA_BYTES), 
                        new IChunkHandler()
                        {
                            private long have = 0;
                            private long total = fileSize;
                            
                            public void receiveData(Chunk data)
                            {
                                try
                                {
                                    //System.out.println(data);
                                    long need = total - have;
                                    if (need < 0)
                                    {
                                        System.out.println("Shouldn't " +
                                            "be getting more bytes.");
                                        System.exit(1);
                                    }
                                    if (need < Chunk.DATA_BYTES)
                                    {
                                        fos.write(data.getData(), 0, 
                                            (int) need);
                                        /*System.out.println("I Writing " +
                                            need + " bytes.");*/
                                    }
                                    else
                                    {
                                        fos.write(data.getData(), 0, 
                                            data.getData().length);
                                        /*System.out.println("E Writing " +
                                            data.getData().length +
                                            " bytes.");*/
                                    }
                                    have += data.getData().length;
                                    System.out.print(
                                        (long) (have * 100. / total) + 
                                        "%" +"\r");
                                }
                                catch (IOException e)
                                {
                                    System.out.println("IOException while " +
                                        "writing to file.");
                                    System.exit(1);
                                }

/*                                totalLeft -= receivedLength;
                                double percentage = ((double) total / 
                                    (double) fileSize) * 100;
                                System.out.print((int) percentage +
                                        "%" +"\r");*/
 
                            }

                            public void finish()
                            {
                                try
                                {
                                    fos.close();
                                }
                                catch (IOException e)
                                {
                                    System.out.println("IOException while " +
                                        "closing file.");
                                    System.exit(1);
                                }
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
