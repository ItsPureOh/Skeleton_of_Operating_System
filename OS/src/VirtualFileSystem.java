public class VirtualFileSystem implements Device{
    private final int MaximumFiles = 10;
    private Device[] devices = new Device[MaximumFiles];
    private int[] deviceId = new int[MaximumFiles];
    private RandomDevice fileRandom = new RandomDevice();
    private FakeFileSystem fileFake = new FakeFileSystem();

    @Override
    public int open(String s) {
        String [] result = s.split("\\s+", 2);

        for(int i = 0; i < MaximumFiles; i++){
            // empty slot
            if (devices[i] == null){
                if (result[0].equals("random")) {
                    if (result.length == 2){
                        deviceId[i] = fileRandom.open(result[1]);
                    }
                    else{
                        deviceId[i] = fileRandom.open(null);
                    }
                    devices[i] = fileRandom;
                    return i;
                }
                else if (result[0].equals("file")) {
                    // precondition check
                    if (result.length < 2){
                        throw new IllegalArgumentException("Need File's Name Please!");
                    }
                    deviceId[i] = fileFake.open(result[1]);
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
    public void close(int id) {
        if (devices[id] != null){
            devices[id].close(deviceId[id]);
            devices[id] = null;
            deviceId[id] = -1;
        }
    }

    @Override
    public byte[] read(int id, int size) {
        return devices[id].read(deviceId[id], size);
    }

    @Override
    public void seek(int id, int to) {
        devices[id].seek(deviceId[id], to);
    }

    @Override
    public int write(int id, byte[] data) {
        return devices[id].write(deviceId[id], data);
    }
}
