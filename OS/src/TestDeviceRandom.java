import java.util.Random;

public class TestDeviceRandom extends UserlandProcess {
    @Override
    public void main() {
        int[] fds = new int[10];
        int size = 0;
        Random rand = new Random();


        // Create 10 files - Test for Open()
        for (int i = 0; i < 10; i++) {
            String filename = "random " + rand.nextInt(101) + 100;
            fds[i] = OS.Open(filename);
            if (fds[i] < 0) {
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
            OS.Write(fds[i], content.getBytes());
            size = content.getBytes().length;
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
            System.out.println("Close File " + Integer.toString(fds[i]) + ".txt" + "Successfully");
        }

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        OS.Exit();
    }
}
