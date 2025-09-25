import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

public class FakeFileSystem implements Device {
    RandomAccessFile files[] = new RandomAccessFile[10];

    @Override
    public int open(String s) {
        if (s == null || s.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty");
        }
        for (int i = 0; i < files.length; i++) {
            if (files[i] == null) {
                try {
                    files[i] = new RandomAccessFile(s, "rw");
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                return i;
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
