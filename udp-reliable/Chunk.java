import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.Checksum;
import java.util.zip.CRC32;

/**
 * Represents a chunk of data which can be transmitted as a packet.  It has a
 * sequence number, the payload, and a checksum.
 */
public class Chunk
{
    public static final int SEQUENCE_NUMBER_BYTES = 4;
    public static final int DATA_BYTES = 8180;
    public static final int CRC_BYTES = 8;
    public static final int TOTAL_BYTES = 8192;

    private byte[] data;

    // ** Server Side Functions **

    /**
     * Creates a new chunk of data filled with bytes and a sequence number.
     * @param bytes the byte array of data
     * @param num the sequence number to append
     * @throws IllegalArgumentException if
     * bytes.length != Chunk.getDataByteSize()
     */
    public Chunk(byte[] bytes, int num)
    {
        int size = getSequenceNumberByteSize() + bytes.length +
            getCRCByteSize();
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putInt(num);
        buffer.put(bytes);

        byte[] sum = buffer.array();
        Checksum crc = new CRC32();
        crc.update(sum, 0, getSequenceNumberByteSize() + bytes.length);
        buffer.putLong(crc.getValue());
        data = buffer.array();
    }

    /**
     * Returns a new byte array for the packet (which contains sequence
     * number, the payload, and a checksum.
     * @return a new byte array
     */
    public byte[] getPacket()
    {
        return Arrays.copyOf(data, data.length);
    }


    // ** Client Side **
    /**
     * Creates a new chunk from an existing packet so that the payload can be
     * pulled out.  The checksum can be checked and the sequence number
     * retrieved also.
     * @param bytes the byte array of data
     */
    public Chunk(byte[] bytes)
    {
        data = Arrays.copyOf(bytes, bytes.length);
    }

    /**
     * Performs a CRC check to ensure the packet is valid.
     * @return whether the packet is valid
     */
    public boolean checkCRC()
    {        
        ByteBuffer bb = ByteBuffer.allocate(data.length);
        bb.put(data);
        Checksum crc = new CRC32();
        crc.update(data, 0, data.length - getCRCByteSize());
        return crc.getValue() == bb.getLong(data.length - getCRCByteSize());
    }

    /**
     * Returns the sequence number.
     * @return the sequence number
     */
    public int getSequenceNumber()
    {
        ByteBuffer bb = ByteBuffer.allocate(getTotalByteSize());
        bb.put(data);
        return bb.getInt(0);
    }

    /**
     * Returns the data payload.
     * @return a byte array containing the data payload
     */
    public byte[] getData()
    {
        ByteBuffer bb = ByteBuffer.allocate(getDataByteSize());
        bb.put(data, getSequenceNumberByteSize(), getDataByteSize());
        return bb.array();    
    }

    // ** Debug Functions **
    /**
     * Returns a string representation of the chunk.
     * @return a string representation of the chunk
     */
    public String toString()
    {
        return "[Chunk (Seq=" + getSequenceNumber() + ")" + " " +
            dataAsString(getData());
    }

    /**
     * Returns a string representing the payload as a string of integers.
     * @return a string representing the payload
     */
    private String dataAsString(byte[] bytes)
    {
        String ret = "";
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        bb.put(bytes);
        for (int i = 0; i < bytes.length; i++)
            ret += bb.get(i);
        return ret;
    }

    // ** Misc Functions **
    /**
     * Returns the number of bytes used for sequence number.
     * @return the number of bytes used for sequence number
     */
    protected int getSequenceNumberByteSize()
    {
        return SEQUENCE_NUMBER_BYTES;
    }

    /**
     * Returns the number of bytes used for data.
     * @return the number of bytes used for data
     */
    protected int getDataByteSize()
    {
        return DATA_BYTES;
    }

    /**
     * Returns the number of bytes used for CRC.
     * @return the number of bytes used for CRC
     */
    protected int getCRCByteSize()
    {
        return CRC_BYTES;
    }

    /**
     * Returns the number of bytes used in total.
     * @return the number of bytes used in total
     */
    protected int getTotalByteSize()
    {
        return TOTAL_BYTES;
    }
}
