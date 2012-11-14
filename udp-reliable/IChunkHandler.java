/**
 * An interface for receiving data from the reliable protocol.
 */
public interface IChunkHandler
{
    /**
     * Handles a new chunk of data from the protocol.
     * @param data the chunk containing the payload
     */
    public void receiveData(Chunk data);

    /**
     * Called when the protocol reports end-of-file.
     */
    public void finish();
}
