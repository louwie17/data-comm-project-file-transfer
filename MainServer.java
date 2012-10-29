import java.io.IOException;
import java.net.ServerSocket;

public class MainServer
{
    public static final int PORT = 4444;

    public static void main(String[] args) throws IOException
    {
        try
        {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Listening on port: " + PORT + ".");
            while (true)
                new ServerThread(serverSocket.accept()).start();
        }
        catch (IOException e)
        {
            System.err.println("Could not listen on port: " + PORT + ".");
            System.exit(1);
        }
    }
}
