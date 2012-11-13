public interface IChunkSupplier
{
    // gives next chunk or null if none left
    public Chunk nextChunk();
}
