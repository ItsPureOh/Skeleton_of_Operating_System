/**
 * TestDeviceFile_1
 * ----------------
 * This process is designed to test basic file operations (Open, Write, Read, and Close)
 * within the simulated Operating System’s Virtual File System.
 *
 * Test sequence:
 *   1. Creates (opens) 10 files named "file 0.txt" to "file 9.txt".
 *   2. Writes a "Hello Device" message into each file.
 *   3. Reads the data back from each file to verify successful write operations.
 *   4. Closes all open file descriptors.
 *
 * Expected Output:
 *   - Each file should show "Hello Device: X" where X is the file index.
 *   - The console should confirm successful open, write, read, and close operations.
 *
 * Purpose:
 *   This test validates correct functionality of:
 *     - File creation/opening
 *     - File descriptor handling
 *     - Sequential write/read correctness
 *     - Proper file closing and cleanup
 */
public class TestDeviceFile_1 extends UserlandProcess {
    @Override
    public void main() {
        int[] fds = new int[10];    // store file descriptors for 10 files
        int Datasize = 0;           // to store size of written data for read verification

        // ============================
        // 1. Test for Open()
        // ============================
        for (int i = 0; i < 10; i++) {
            String filename = "file " + Integer.toString(i) + ".txt";   // create filenames dynamically
            fds[i] = OS.Open(filename);                                 // attempt to open/create file
            if (fds[i] == -1) {
                System.out.println("Open failed " + filename);
            } else {
                System.out.println("Open success " + filename);
            }
        }
        // ============================
        // 2. Test for Write()
        // ============================
        for (int i = 0; i < 10; i++) {
            String content = ("Hello Device: " + Integer.toString(i));  // content unique per file
            if (OS.Write(fds[i], content.getBytes()) < 0){              // write data
                System.out.println("Write failed " + content);
            }
            Datasize = content.getBytes().length;                       // track message length for read size
            System.out.println("Write success " + Integer.toString(i));
        }
        // ============================
        // 3. Test for Read()
        // ============================
        for (int i = 0; i < 10; i++) {
            OS.Seek(i, 0);       // reset file pointer to start before reading
            byte result[] = OS.Read(fds[i], 32);    // read up to 32 bytes from file
            System.out.println(new String(result));     // display file content
        }
        // ============================
        // 4. Test for Close()
        // ============================
        for (int i = 0; i < 10; i++) {
            OS.Close(fds[i]);
            System.out.println("Close File " + Integer.toString(i) + ".txt" + " Successfully");
        }
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        OS.Exit();
    }
}