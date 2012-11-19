
/**
 * Represents an ack segment.
 */
public class ACK extends Chunk
{
    public static final int SEQUENCE_NUMBER_BYTES = 4;
    public static final int DATA_BYTES = 1;
    public static final int CRC_BYTES = 8;
    public static final int TOTAL_BYTES = 13;

    /** Magic code for testing if this is an ack segment. */
    private static final byte ACK_CODE = 3;


    // ** Server Side Functions **
    /**
     * Creates a new ack from an existing packet so that the payload can be
     * pulled out.  The checksum can be checked and the sequence number
     * retrieved also.
     * @param bytes the byte array of data
     */
    public ACK(byte[] bytes)
    {
        super(bytes);
    }

    /**
     * Checks whether the ack has the correct magic code.
     * @return whether the ack has the correct magic code
     */
    public boolean checkCode()
    {
        byte[] code = getData();
        if (code == null || code.length < 1 || code[0] != ACK_CODE)
            return false;
        return true;
    }

    // ** Client Side **
    /**
     * Creates a new ack for a given sequence number.
     * @param num the sequence number to append
     */
    public ACK(int num)
    {
        super(new byte[]{ACK_CODE}, num);
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
