import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
/**
 * FakeFileSystem Class
 * --------------------
 * This class provides a simple simulation of a file system by implementing the Device interface.
 * It allows files to be opened, read, written, closed, and seeked using a fixed-size array of
 * RandomAccessFile objects.
 *
 * Each file handle is associated with an index in the files[] array (0–9). The class performs
 * basic precondition checks to ensure that operations are only performed on opened files.
 */
public class FakeFileSystem implements Device {
    RandomAccessFile files[] = new RandomAccessFile[10];    // Array to hold references to up to 10 open files.

    /**
     * Opens a file for reading and writing, and stores it in the first available slot.
     *
     * @param s the path of the file to open.
     * @return the index (file ID) of the opened file, or -1 if all slots are occupied.
     * @throws IllegalArgumentException if the provided file name is null or empty.
     * @throws RuntimeException if the file cannot be found or opened.
     */
    @Override
    public int Open(String s) {
        if (s == null || s.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty");
        }
        // Find the first empty slot in the file array
        for (int i = 0; i < files.length; i++) {
            if (files[i] == null) {
                try {
                    // Open file in read-write mode
                    files[i] = new RandomAccessFile(s, "rw");
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                return i;
            }
        }
        // All file slots are full
        return -1;
    }

    /**
     * Closes the file associated with the given ID and frees its slot.
     *
     * @param id the ID of the file to close.
     * @throws RuntimeException if an I/O error occurs during closing.
     */
    @Override
    public void Close(int id) {
        try {
            files[id].close();
            files[id] = null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads a specified number of bytes from the file.
     *
     * @param id the ID of the file to read from.
     * @param size the number of bytes to read.
     * @return a byte array containing the read data.
     * @throws IllegalArgumentException if the file has not been opened.
     * @throws RuntimeException if an I/O error occurs during reading.
     */
    @Override
    public byte[] Read(int id, int size) {
        byte [] result = new byte[size];
        //precondition check
        if (files[id] == null) {
            throw new IllegalArgumentException("File not opened");
        }

        try {
            // Read bytes into result array
            files[id].read(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * Moves the file pointer to a specific position in the file.
     *
     * @param id the ID of the file to seek within.
     * @param to the byte offset to move to.
     * @throws IllegalArgumentException if the file has not been opened.
     * @throws RuntimeException if an I/O error occurs during seeking.
     */
    @Override
    public void Seek(int id, int to) {
        //precondition check
        if (files[id] == null) {
            throw new IllegalArgumentException("File not opened");
        }
        try {
            // Move pointer to specified position
            files[id].seek(to);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes a byte array to the specified file.
     *
     * @param id the ID of the file to write to.
     * @param data the byte array containing data to write.
     * @return the number of bytes written (equals data length).
     * @throws IllegalArgumentException if the file has not been opened.
     * @throws RuntimeException if an I/O error occurs during writing.
     */
    @Override
    public int Write(int id, byte[] data) {
        //precondition check
        if (files[id] == null) {
            throw new IllegalArgumentException("File not opened");
        }
        try {
            // Write byte data to file
            files[id].write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Return number of bytes written
        return data.length;
    }
}
