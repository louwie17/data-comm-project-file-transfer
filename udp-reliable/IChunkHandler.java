public interface IChunkHandler
{
    public void receiveData(Chunk data);
    public void finish();
}
