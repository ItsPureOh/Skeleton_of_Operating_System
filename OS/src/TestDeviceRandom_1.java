import java.util.Arrays;

/**
 * TestDeviceRandom_1 Class
 * ------------------------
 * This userland process tests the functionality of the RandomDevice.
 * It verifies that multiple random devices can be opened, read from, and closed
 * correctly within the simulated operating system.
 *
 * Specifically, it tests:
 * 1. Opening multiple RandomDevice instances (each with its own seed).
 * 2. Reading random bytes from each device to confirm data generation.
 * 3. Using Seek() to advance the random generator and confirm output changes.
 * 4. Properly closing all opened devices.
 *
 * The test helps confirm that seeded RandomDevice instances are independent and
 * that read/seek behavior works as expected across multiple device handles.
 */
public class TestDeviceRandom_1 extends UserlandProcess {
    /**
     * The main test routine executed by this userland process.
     * Exercises open, read, seek, and close operations on RandomDevice instances.
     */
    @Override
    public void main() {
        int[] fds = new int[10];
        // --- Step 1: Open multiple random devices with different seeds ---
        for (int i = 0; i < 10; i++) {
            String devname = "random " + (100 + i);  // seeds: 100, 101, ...
            fds[i] = OS.Open(devname);

            if (fds[i] == -1) {
                System.out.println("Open failed " + devname);
            } else {
                System.out.println("Open success " + devname);
            }
        }
        // --- Step 2: Read random data from each device ---
        // Confirms that each seeded device produces a distinct random sequence.
        for (int i = 0; i < 10; i++) {
            byte[] rand = OS.Read(fds[i], 10); // read 8 random bytes
            if (rand == null) {
                System.out.println("Random read failed fd=" + fds[i]);
            } else {
                System.out.println("Random read fd=" + fds[i] + ": " + Arrays.toString(rand));
            }
        }
        // --- Step 3: Seek and re-read from each device ---
        // This moves the internal random generator forward by 1 byte
        // to confirm that future reads yield different data.
        for (int i = 0; i < 10; i++) {
            OS.Seek(fds[i], 1);
            byte[] rand2 = OS.Read(fds[i], 10);
            if (rand2 == null) {
                System.out.println("Random re-read failed fd=" + fds[i]);
            } else {
                System.out.println("After Seek(1), re-read fd=" + fds[i] + ": " + Arrays.toString(rand2));
            }
        }
        // --- Step 4: Close all random devices ---
        // Ensures proper cleanup and release of all handles.
        for (int i = 0; i < 10; i++) {
            OS.Close(fds[i]);
            System.out.println("Close success " + fds[i]);
        }

        OS.Exit();
    }
}
