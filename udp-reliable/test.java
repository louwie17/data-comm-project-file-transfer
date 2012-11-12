import java.util.Scanner;

public class test
{

    public static void main(String[] args)
    {
        Scanner s = new Scanner(System.in);
        System.out.print("Test Line: ");
        String input = s.nextLine();

        byte[] bytes = input.getBytes();
        System.out.println("bytes length: " + bytes.length);
        Chunk server = new Chunk(bytes, 0);

        byte[] bytesReceived = server.getPacket();
    
        Chunk client = new Chunk(bytesReceived);

        System.out.println("Check CRC: " + client.checkCRC());
        System.out.println("Sequence num: " + client.getSequenceNumber());

    }

}
