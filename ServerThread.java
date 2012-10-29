import java.net.*;
import java.io.*;

import java.util.Scanner;

public class ServerThread extends Thread
{
    private Socket socket = null;

    public ServerThread(Socket aSocket)
    {
        super("ServerThread");
        socket = aSocket;
        System.out.println("Starting new thread...");
    }

    public void run()
    {
        System.out.println("Accepted connection : " + socket);
        try
        {
            Scanner scan = new Scanner(socket.getInputStream());
            // get request
            String filename = scan.nextLine();
            System.out.println("Filename: " + filename);
            File file = new File(filename);

            // send response
            PrintWriter out = null;
            out = new PrintWriter(socket.getOutputStream(), true);
            boolean ok = false;
            if (!file.exists())
                out.println("Error: file doesn't exist.");
            else if (!file.isFile())
                out.println("Error: not a file.");
            else if (!file.canRead())
                out.println("Error: can't read from file.");
            else
            {
                out.println("OKGO");
                ok = true;
            }

            if (!ok)
                return;

            // setup streams
            FileInputStream fin = new FileInputStream(filename);
            BufferedInputStream bin = new BufferedInputStream(fin);
            BufferedOutputStream os = new BufferedOutputStream(
                socket.getOutputStream());

            // write file to socket
            System.out.println("Sending Files...");
            int count;
            byte[] buffer = new byte[8192];
            while ((count = fin.read(buffer)) > 0)
                os.write(buffer, 0, count);

            // cleanup
            bin.close();
            os.close();
            fin.close();

            System.out.println("File transfer complete.");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
