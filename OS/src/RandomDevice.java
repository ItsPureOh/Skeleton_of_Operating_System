import java.util.Arrays;
import java.util.Random;

/**
 * RandomDevice Class
 * ------------------
 * Simulates a simple random-number-generating device.
 * Implements the Device interface and provides deterministic or non-deterministic
 * random byte streams based on a given seed string.
 *
 * Each device handle corresponds to one Random instance stored in an internal array.
 * - If the Open() parameter string is empty or null, a non-deterministic Random is created.
 * - If the Open() parameter is a numeric string, it is used as a seed for reproducible results.
 */
public class RandomDevice implements Device {
    private Random[] devices = new Random[10];

    /**
     * Opens a new random device.
     * If the provided string is null or empty, creates a new Random without a seed.
     * Otherwise, parses the string as a seed to create a deterministic generator.
     *
     * @param s optional seed string; if null or empty, uses a default Random.
     * @return index of the opened Random device, or -1 if none available.
     */
    @Override
    public int Open(String s) {
        // find empty spot
        for (int i = 0; i < devices.length; i++) {
            // supplied string for Open is null or empty
            if (s == null || s.isEmpty()){
                devices[i] = new Random();
                return i;
            }
            // Converting string to the seed
            else{
                int seed = Integer.parseInt(s);
                devices[i] = new Random(seed);
                System.out.println("Random device opened with seed " + seed);
                return i;
            }
        }
        return -1;
    }

    /**
     * Closes the specified random device by clearing its slot.
     *
     * @param id index of the random device to close
     */
    @Override
    public void Close(int id) {
        devices[id] = null;
    }

    /**
     * Reads random bytes from the specified device.
     * Fills and returns a byte array of the requested size.
     *
     * @param id index of the device
     * @param size number of bytes to generate
     * @return byte array filled with random values
     * @throws IllegalArgumentException if the device is not open
     */
    @Override
    public byte[] Read(int id, int size) {
        //precondition check
        if (devices[id] == null){
            throw new IllegalArgumentException("Device not open at id: " + id);
        }

        //Read will create/fill an array with random values.
        byte[] result = new byte[size];
        devices[id].nextBytes(result);
        return result;
    }

    /**
     * Advances the random generator by generating and discarding a number of bytes.
     * Used to simulate seeking within the random stream.
     *
     * @param id index of the device
     * @param to number of bytes to skip
     * @throws IllegalArgumentException if the device is not open
     */
    @Override
    public void Seek(int id, int to) {
        //precondition check
        if (devices[id] == null){
            throw new IllegalArgumentException("Device not open at id: " + id);
        }

        // just generate and discard 'to' random bytes
        byte[] dummy = new byte[to];
        devices[id].nextBytes(dummy);
    }

    /**
     * Write operation is unsupported for RandomDevice.
     * It performs no action and always returns 0.
     *
     * @param id index of the device
     * @param data unused
     * @return 0 (no bytes written)
     */
    @Override
    public int Write(int id, byte[] data) {
        // Write will return 0 length and do nothing
        return 0;
    }
}
