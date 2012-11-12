import java.util.Scanner;
import java.lang.StringBuffer;
import java.util.Arrays;

public class test
{

    public static void main(String[] args)
    {
        Scanner s = new Scanner(System.in);
        System.out.print("Enter Char: ");
        String input = s.nextLine();        
        StringBuffer test = new StringBuffer(1011);

        while (test.length() < 1011)
        {
            test.append(input);
        }
        byte[] bytes = test.toString().getBytes();

        System.out.println("bytes length: " + bytes.length);
        Chunk server = new Chunk(bytes, 0);

        byte[] bytesReceived = server.getPacket();
        System.out.println("packet Length: " + bytesReceived.length); 
        Chunk client = new Chunk(bytesReceived);
        System.out.println(Arrays.equals(server.getPacket(), 
                    client.getPacket()));

        System.out.println("Check CRC: " + client.checkCRC());
        System.out.println("Sequence num: " + client.getSequenceNumber());

    }

}
