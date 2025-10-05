/**
 * Device Interface
 * ----------------
 * This interface defines the basic operations for interacting with a generic device.
 * It abstracts the key functionalities such as opening, closing, reading, writing,
 * and seeking within a device stream.
 *
 * Implementing classes should provide concrete definitions for these operations
 * based on the specific type of device (e.g., file, network socket, serial port, etc.).
 */
public interface Device {
    /**
     * Opens the device using a provided string identifier (e.g., file path or port name).
     *
     * @param s the string identifier for the device.
     * @return an integer ID representing the opened device handle.
     */
    int Open(String s);
    /**
     * Closes the device associated with the given ID.
     *
     * @param id the device ID to close.
     */
    void Close(int id);
    /**
     * Reads a specified number of bytes from the device.
     *
     * @param id the device ID to read from.
     * @param size the number of bytes to read.
     * @return a byte array containing the data read from the device.
     */
    byte[] Read(int id, int size);
    /**
     * Moves the read/write pointer of the device to a specific position.
     *
     * @param id the device ID.
     * @param to the position to move the pointer to.
     */
    void Seek(int id, int to);
    /**
     * Writes data to the device.
     *
     * @param id the device ID to write to.
     * @param data the byte array containing data to be written.
     * @return the number of bytes successfully written.
     */
    int Write(int id, byte[] data);
}
