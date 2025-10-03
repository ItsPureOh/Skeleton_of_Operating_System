public class TestDeviceFile_1 extends UserlandProcess {
    @Override
    public void main() {
        int[] fds = new int[10];
        int size = 0;


        // Create 10 files - Test for Open()
        for (int i = 0; i < 10; i++) {
            String filename = "file " + Integer.toString(i) + ".txt";
            fds[i] = OS.Open(filename);
            if (fds[i] == -1) {
                System.out.println("Open failed " + filename);
            } else {
                System.out.println("Open success " + filename);
            }
        }

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Test for Write
        for (int i = 0; i < 10; i++) {
            String content = ("Hello Device: " + Integer.toString(i));
            if (OS.Write(fds[i], content.getBytes()) < 0){
                System.out.println("Write failed " + content);
            }
            size = content.getBytes().length;
            System.out.println("Write success " + Integer.toString(i));
        }

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        // Test for Read()
        for (int i = 0; i < 10; i++) {
            OS.Seek(i, 0);
            byte result[] = OS.Read(fds[i], size);
            System.out.println(new String(result));
        }

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Test for Close()
        for (int i = 0; i < 10; i++) {
            OS.Close(fds[i]);
            System.out.println("Close File " + Integer.toString(i) + ".txt" + " Successfully");
        }

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}