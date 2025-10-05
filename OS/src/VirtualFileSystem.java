/**
 * VirtualFileSystem Class
 * -----------------------
 * The VirtualFileSystem (VFS) acts as a unified interface for managing multiple
 * device types within the operating system. It implements the Device interface
 * and dynamically delegates requests to specific device implementations such as:
 *
 * - RandomDevice  → generates random data streams
 * - FakeFileSystem → simulates file-based read/write access
 *
 * The VFS maintains arrays that map logical file descriptors to the appropriate
 * underlying device and its internal device ID.
 *
 * Responsibilities:
 * - Parse the device string (e.g., "random 123" or "file test.txt")
 * - Create and track device instances
 * - Route all I/O operations (Open, Read, Write, Seek, Close) to the correct device
 *
 */
public class VirtualFileSystem implements Device{
    private final int MaximumFiles = 10;                    // Maximum number of files/devices that can be opened simultaneously
    private Device[] devices = new Device[MaximumFiles];    // References to the actual device instances currently in use.
    private int[] deviceId = new int[MaximumFiles];         // Maps VFS slot IDs to device-specific internal IDs.
    private RandomDevice fileRandom = new RandomDevice();   // Random device handler for "random" type devices.
    private FakeFileSystem fileFake = new FakeFileSystem(); // Fake file system handler for "file" type devices.

    /**
     * Opens a device or file through the virtual file system.
     * Supports two prefixes:
     * - "random" → opens a RandomDevice (optionally seeded)
     * - "file" → opens a FakeFileSystem file
     *
     * @param s a string describing the target (e.g., "random 100" or "file example.txt")
     * @return index of the opened VFS slot, or -1 if no slot available
     * @throws IllegalArgumentException if format is invalid or parameters are missing
     */
    @Override
    public int Open(String s) {
        // Split command into device type and optional parameter (seed or filename)
        String [] result = s.split("\\s+", 2);

        for(int i = 0; i < MaximumFiles; i++){
            // Find first available slot
            if (devices[i] == null){
                // Open RandomDevice with optional seed
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
                    // Validate filename presence
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
        // No free slot found
        return -1;
    }

    /**
     * Closes an open device and frees its slot.
     *
     * @param id VFS slot ID to close
     */
    @Override
    public void Close(int id) {
        if (devices[id] != null){
            devices[id].Close(deviceId[id]);
            devices[id] = null;
            deviceId[id] = -1;
        }
    }

    /**
     * Reads data from the specified VFS device slot.
     *
     * @param id VFS slot ID
     * @param size number of bytes to read
     * @return byte array containing the read data
     */
    @Override
    public byte[] Read(int id, int size) {
        return devices[id].Read(deviceId[id], size);
    }

    /**
     * Moves the read/write pointer for the specified device.
     *
     * @param id VFS slot ID
     * @param to target position in bytes
     */
    @Override
    public void Seek(int id, int to) {
        devices[id].Seek(deviceId[id], to);
    }

    /**
     * Writes data to the specified device.
     *
     * @param id VFS slot ID
     * @param data byte array of data to write
     * @return number of bytes written
     */
    @Override
    public int Write(int id, byte[] data) {
        return devices[id].Write(deviceId[id], data);
    }
}
