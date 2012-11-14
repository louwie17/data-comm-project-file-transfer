/**
 * An interface for supplying data to the reliable protocol.
 */
public interface IChunkSupplier
{
    /**
     * Supplies data to the protocol.  Returns the next chunk of data or null
     * if no more data exists.
     * @return the next chunk of data or null
     */
    public Chunk nextChunk();
}
