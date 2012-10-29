import java.io.IOException;
import java.net.ServerSocket;
import java.net.DatagramSocket;
import java.net.DatagramPacket;

public class MainServer
{
    public static final int PORT = 4444;
    public static DatagramPacket inPacket;
    private static final byte[] buffer = new byte[4096];
    public static DatagramSocket serverSocket = null;

    public static void main(String[] args) throws IOException
    {
        try
        {
            inPacket = new DatagramPacket(buffer, buffer.length);
            serverSocket = new DatagramSocket(PORT);
            System.out.println("Listening on port: " + PORT + ".");
        }
        catch (IOException e)
        {
            System.err.println("Could not listen on port: " + PORT + ".");
            System.exit(1);
        }
        while (true)
        {
          serverSocket.receive(inPacket);
          new ServerThread(serverSocket, inPacket).start();
        }
    }

}
