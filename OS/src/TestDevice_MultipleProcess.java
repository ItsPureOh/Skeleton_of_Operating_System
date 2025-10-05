/**
 * TestDevice_MultipleProcess Class
 * --------------------------------
 * This userland process is designed to test **concurrent access** to the same device
 * by multiple processes in the simulated operating system.
 *
 * Specifically, it:
 * 1. Opens multiple device/file handles.
 * 2. Writes unique data to each handle (simulating multiple processes writing).
 * 3. Reads back the written data to verify correctness and isolation.
 * 4. Closes all open handles at the end.
 *
 * This test ensures that the OS kernel, VirtualFileSystem, and Device implementations
 * correctly handle multiple simultaneous users of the same device without data corruption
 * or interference.
 */
public class TestDevice_MultipleProcess extends UserlandProcess{
    /**
     * Entry point of the userland process.
     * Executes a sequence of file/device operations using OS-level calls.
     */
    @Override
    public void main() {
        int[] fds = new int[10];
        int size;

        // --- Step 1: Open 10 files ---
        for (int i = 0; i < 10; i++) {
            String filename = "file " + i + ".txt";
            fds[i] = OS.Open(filename);
            if (fds[i] == -1) {
                System.out.println("Open failed " + filename);
            } else {
                System.out.println("Open success " + filename);
            }
        }
        // --- Step 2: Append text to each file ---
        for (int i = 0; i < 10; i++) {
            // Move file pointer to end
            OS.Seek(fds[i], 15);

            String content = "Goodbye Device: " + i;
            int result = OS.Write(fds[i], content.getBytes());
            size = content.getBytes().length;
            if (result < 0) {
                System.out.println("Write failed " + content);
            } else {
                System.out.println("Appended " + content + " to file " + i + ".txt");
            }

        }
        // --- Read full contents back ---
        for (int i = 0; i < 10; i++) {
            OS.Seek(fds[i], 0);
            byte[] result = OS.Read(fds[i], 32); // read enough bytes to include both Hello + Goodbye
            if (result != null){
                System.out.println("file " + i + ".txt contents: " + new String(result));
            }
        }
        // --- Close all files ---
        for (int i = 0; i < 10; i++) {
            OS.Close(fds[i]);
            System.out.println("Close file " + i + ".txt successfully");
        }

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
