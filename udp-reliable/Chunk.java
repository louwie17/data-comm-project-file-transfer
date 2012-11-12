import java.nio.ByteBuffer;
import java.util.zip.Checksum;
import java.util.zip.CRC32;

public class Chunk
{
    // First byte is sequence number last 9 bytes are the checksum/CRC
    // the rest is the actual data
    private byte[] data;

    // this one is server side
    public Chunk(byte[] bytes, int num)
    {
        if (bytes.length == 1015)
        {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            buffer.putInt(num);
            buffer.put(bytes);

            Checksum crc = new CRC32();
            crc.update(bytes, 0, bytes.length);

            buffer.putLong(crc.getValue());

            this.data = buffer.array();
        }
        //creates the data byte array with sequence number and checksum
    }

    // this one is client side
    public Chunk(byte[] bytes)
    {
        this.data = bytes;
    }

    // for the server side
    public byte[] getPacket()
    {
        return data;
    }

    // Check the CRC on the client side
    public boolean checkCRC()
    {
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.get(data, 1016, data.length);

        Checksum crc = new CRC32();
        crc.update(data, 1, 1015);

        return bb.getLong() == crc.getValue();
    }

    // client side
    public int getSequenceNumber()
    {
        ByteBuffer bb = ByteBuffer.allocate(1);
        bb.get(data, 0, 1);
        return bb.getInt();
    }

    // for the client side
    public byte[] getData()
    {
        ByteBuffer bb = ByteBuffer.allocate(1015);
        bb.get(data, 1, 1016);
        return bb.array();    
    }
}
