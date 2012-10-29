import java.net.*;
import java.io.*;

import java.util.Scanner;

public class Client
{
    private static final int BUFFER_SIZE = 8192;
    private static final int PORT = 4444;

    public static void main(String [] args)
    {
        Scanner scan = new Scanner(System.in);
        String ip;
        String filename = null;
        int bytesRead = 0;
        Socket socket = null;

        while (true)
        {
            // connect to server
            System.out.print("Server IP: ");
            ip = scan.nextLine();
            if (ip.equals("quit"))
                System.exit(0);
            try
            {
                socket = new Socket(ip, PORT);
            }
            catch (IOException e)
            {
                System.out.println("Could not connect.");
                continue;
            }

            System.out.println("Connected...");
            try
            {
                // request file from server
                System.out.print("File to transfer: ");
                filename = scan.nextLine();
                if (filename.equals("quit"))
                    break;
                PrintWriter out = null;
                out = new PrintWriter(socket.getOutputStream(), true);
                out.println(filename);

                Scanner serverScan = new Scanner(socket.getInputStream());
                String response = serverScan.nextLine();
                if (!response.equals("OKGO"))
                {
                    System.out.println(response);
                    continue;
                }

                // setup streams
                InputStream is = socket.getInputStream();
                String[] splitName = filename.split("/");
                File newFile = new File("local_" +
                    splitName[splitName.length - 1]);
                if (newFile.exists()) // || !newFile.canWrite())
                {
                    System.out.println("Error: can't write to file.");
                    continue;
                }
                newFile.createNewFile();

                FileOutputStream fos = new FileOutputStream(newFile);
                BufferedOutputStream bos = new BufferedOutputStream(fos);

                // read file from server
                int count = 0;
                byte[] buffer = new byte[8192];
                while ((count = is.read(buffer)) > 0)
                {
                    bytesRead += count;
                    fos.write(buffer, 0, count);
                    System.out.print(filename + ": " + bytesRead + "\r");
                }

                // cleanup
                fos.flush();
                bos.close();
                //socket.close();

                System.out.println("File transfer complete.");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
