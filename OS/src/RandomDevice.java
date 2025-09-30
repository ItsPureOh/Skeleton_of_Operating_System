import java.util.Arrays;
import java.util.Random;

public class RandomDevice implements Device {
    private Random[] devices = new Random[10];

    @Override
    public int Open(String s) {
        // find empty spot
        for (int i = 0; i < devices.length; i++) {
            // supplied string for Open is null or empty
            if (s == null || s.isEmpty()){
                devices[i] = new Random();
            }
            // Converting string to the seed
            else{
                int seed = Integer.parseInt(s);
                devices[i] = new Random(seed);
            }
        }
        return 0;
    }

    @Override
    public void Close(int id) {
        devices[id] = null;
    }

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

    @Override
    public void Seek(int id, int to) {
        //precondition check
        if (devices[id] == null){
            throw new IllegalArgumentException("Device not open at id: " + id);
        }

        // just generate and discard 'to' random bytes
        byte[] dummy = new byte[to];
        devices[id].nextBytes(dummy);
        System.out.println(Arrays.toString(dummy));
    }

    @Override
    public int Write(int id, byte[] data) {
        // Write will return 0 length and do nothing
        return 0;
    }
}
