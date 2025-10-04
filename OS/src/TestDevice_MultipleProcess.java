public class TestDevice_MultipleProcess extends UserlandProcess{
    @Override
    public void main() {
        int[] fds = new int[10];
        int size;

        // --- Open 10 files ---
        for (int i = 0; i < 10; i++) {
            String filename = "file " + i + ".txt";
            fds[i] = OS.Open(filename);
            if (fds[i] == -1) {
                System.out.println("Open failed " + filename);
            } else {
                System.out.println("Open success " + filename);
            }
        }
        // --- Append "Goodbye Device: i" to each file ---
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
