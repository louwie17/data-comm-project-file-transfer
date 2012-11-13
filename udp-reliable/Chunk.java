import java.nio.ByteBuffer;
import java.util.zip.Checksum;
import java.util.zip.CRC32;

public class Chunk
{
    /* Packet:
           4 bytes for sequence number
        1011 bytes of data
           8 bytes of checksum
        ----------
        1024 bytes total
     */
    public static final int SEQUENCE_NUMBER_BYTES = 4;
    public static final int DATA_BYTES = 1011;
    public static final int CRC_BYTES = 8;
    public static final int TOTAL_BYTES = 1024;
    
    private byte[] data;

    // this one is server side
    public Chunk(byte[] bytes, int num)
    {
        if (bytes.length != DATA_BYTES)
            throw new IllegalArgumentException("Invalid number of data " + 
                " bytes.  Expected " + DATA_BYTES + ", but received " + 
                bytes.length);
        ByteBuffer buffer = ByteBuffer.allocate(TOTAL_BYTES);
        buffer.putInt(num);
        buffer.put(bytes);

        byte[] sum = buffer.array();
        Checksum crc = new CRC32();
        crc.update(sum, 0, SEQUENCE_NUMBER_BYTES + DATA_BYTES);
        buffer.putLong(crc.getValue());
        data = buffer.array();
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
        ByteBuffer bb = ByteBuffer.allocate(TOTAL_BYTES);
        bb.put(data);
        
        Checksum crc = new CRC32();
        crc.update(data, 0, SEQUENCE_NUMBER_BYTES + DATA_BYTES);

//        System.out.println(bb.getLong(SEQUENCE_NUMBER_BYTES + DATA_BYTES));
//        System.out.println(crc.getValue());
        return crc.getValue() == bb.getLong(SEQUENCE_NUMBER_BYTES +
            DATA_BYTES);
    }

    // client side
    public int getSequenceNumber()
    {
        ByteBuffer bb = ByteBuffer.allocate(TOTAL_BYTES);
        bb.put(data);
        return bb.getInt(0);
    }

    // for the client side
    public byte[] getData()
    {
        ByteBuffer bb = ByteBuffer.allocate(DATA_BYTES);
        bb.put(data, SEQUENCE_NUMBER_BYTES, DATA_BYTES);
        return bb.array();    
    }
    
    /*public String toString()
    {
        return "[Chunk (Seq=" + getSequenceNumber() + ")" + " " +
            dataAsString(getData());
    }*/
    
    // temp public
    public String dataAsString(byte[] bytes)
    {
        String ret = "";
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        bb.put(bytes);
        for (int i = 0; i < bytes.length; i++)
            ret += bb.get(i);
        return ret;
    }
}



