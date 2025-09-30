import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FakeFileSystem implements Device {
    RandomAccessFile files[] = new RandomAccessFile[10];

    @Override
    public int Open(String s) {
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
    public void Close(int id) {
        try {
            files[id].close();
            files[id] = null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] Read(int id, int size) {
        byte [] result = new byte[size];
        //precondition check
        if (files[id] == null) {
            throw new IllegalArgumentException("File not opened");
        }

        try {
            files[id].read(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public void Seek(int id, int to) {
        //precondition check
        if (files[id] == null) {
            throw new IllegalArgumentException("File not opened");
        }

        try {
            files[id].seek(to);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int Write(int id, byte[] data) {
        //precondition check
        if (files[id] == null) {
            throw new IllegalArgumentException("File not opened");
        }

        try {
            files[id].write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}
