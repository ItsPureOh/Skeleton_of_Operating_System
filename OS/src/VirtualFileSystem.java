public class VirtualFileSystem implements Device{
    private final int MaximumFiles = 10;
    private Device[] devices = new Device[MaximumFiles];
    private int[] deviceId = new int[MaximumFiles];

    @Override
    public int open(String s) {
        String [] result = s.split("\\s+", 2);
        for(int i = 0; i < MaximumFiles; i++){
            // empty slot
            if (devices[i] == null){
                if (result[0].equals("random")) {
                    RandomDevice file = new RandomDevice();
                    if (result.length == 2){
                        file.open(result[1]);
                    }
                    else{
                        file.open(null);
                    }
                    devices[i] = ;
                    return i;
                }
                else if (result[0].equals("file")) {
                    FakeFileSystem file = new FakeFileSystem();
                    file.open(result[1]);
                    return i;
                }
                else{
                    throw new IllegalArgumentException("Invalid file format");
                }
            }
        }
        return -1;
    }

    @Override
    public void close(int id) {

    }

    @Override
    public byte[] read(int id, int size) {
        return new byte[0];
    }

    @Override
    public void seek(int id, int to) {

    }

    @Override
    public int write(int id, byte[] data) {
        return 0;
    }
}
