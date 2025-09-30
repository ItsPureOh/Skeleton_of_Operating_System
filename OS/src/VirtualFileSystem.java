public class VirtualFileSystem implements Device{
    private final int MaximumFiles = 10;
    private Device[] devices = new Device[MaximumFiles];
    private int[] deviceId = new int[MaximumFiles];
    private RandomDevice fileRandom = new RandomDevice();
    private FakeFileSystem fileFake = new FakeFileSystem();

    @Override
    public int Open(String s) {
        String [] result = s.split("\\s+", 2);

        for(int i = 0; i < MaximumFiles; i++){
            // empty slot
            if (devices[i] == null){
                if (result[0].equals("random")) {
                    if (result.length == 2){
                        deviceId[i] = fileRandom.Open(result[1]);
                    }
                    else{
                        deviceId[i] = fileRandom.Open(null);
                    }
                    devices[i] = fileRandom;
                    return i;
                }
                else if (result[0].equals("file")) {
                    // precondition check
                    if (result.length < 2){
                        throw new IllegalArgumentException("Need File's Name Please!");
                    }
                    deviceId[i] = fileFake.Open(result[1]);
                    devices[i] = fileFake;
                    return i;
                }
                else{
                    throw new IllegalArgumentException("Invalid file format");
                }
            }
        }
        // if there is no empty slot
        return -1;
    }

    @Override
    public void Close(int id) {
        if (devices[id] != null){
            devices[id].Close(deviceId[id]);
            devices[id] = null;
            deviceId[id] = -1;
        }
    }

    @Override
    public byte[] Read(int id, int size) {
        return devices[id].Read(deviceId[id], size);
    }

    @Override
    public void Seek(int id, int to) {
        devices[id].Seek(deviceId[id], to);
    }

    @Override
    public int Write(int id, byte[] data) {
        return devices[id].Write(deviceId[id], data);
    }
}
