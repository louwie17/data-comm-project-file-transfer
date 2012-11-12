import java.nio.ByteBuffer;
import java.util.zip.Checksum;
import java.util.zip.CRC32;

public class Chunk
{
    // First 4 bytes is sequence number last 9 bytes are the checksum/CRC
    // the rest is the actual data
    private byte[] data;

    // this one is server side
    public Chunk(byte[] bytes, int num)
    {
        if (bytes.length == 1011)
        {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            buffer.putInt(num);
            buffer.put(bytes);

            byte[] sum = buffer.array();
            Checksum crc = new CRC32();
            crc.update(sum, 0, 1015);
            buffer.putLong(crc.getValue());

            data = buffer.array();
        }
        else
            System.out.println("wrong size");
        //creates the data byte array with sequence number and checksum
    }

    // this one is client side
    public Chunk(byte[] bytes)
    {
        data = bytes;
    }

    // for the server side
    public byte[] getPacket()
    {
        return data;
    }

    // Check the CRC on the client side
    public boolean checkCRC()
    {
        ByteBuffer bb = ByteBuffer.allocate(1024);
        bb.put(data);
        
        Checksum crc = new CRC32();
        crc.update(data, 0, 1015);

        System.out.println(bb.getLong(1015));
        System.out.println(crc.getValue());
        return bb.getLong(1015) == crc.getValue();
    }

    // client side
    public int getSequenceNumber()
    {
        ByteBuffer bb = ByteBuffer.allocate(1024);
        bb.put(data);
        return bb.getInt(0);
    }

    // for the client side
    public byte[] getData()
    {
        ByteBuffer bb = ByteBuffer.allocate(1015);
        bb.get(data, 1, 1016);
        return bb.array();    
    }
}
