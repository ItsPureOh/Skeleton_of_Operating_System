import java.util.Arrays;

public class TestDeviceRandom_1 extends UserlandProcess {
    @Override
    public void main() {
        int[] fds = new int[10];
        // --- Test Random Devices ---
        for (int i = 0; i < 10; i++) {
            String devname = "random " + (100 + i);  // seeds: 100, 101
            fds[i] = OS.Open(devname);
            if (fds[i] == -1) {
                System.out.println("Open failed " + devname);
            } else {
                System.out.println("Open success " + devname);
            }
        }

        // Read Random Devices (cannot Write to them normally)
        for (int i = 0; i < 10; i++) {
            byte[] rand = OS.Read(fds[i], 10); // read 8 random bytes
            if (rand == null) {
                System.out.println("Random read failed fd=" + fds[i]);
            } else {
                System.out.println("Random read fd=" + fds[i] + ": " + Arrays.toString(rand));
            }
        }

        // Seek back to start and re-read
        for (int i = 0; i < 10; i++) {
            OS.Seek(fds[i], 1);
            byte[] rand2 = OS.Read(fds[i], 10);
            if (rand2 == null) {
                System.out.println("Random re-read failed fd=" + fds[i]);
            } else {
                System.out.println("After Seek(1), re-read fd=" + fds[i] + ": " + Arrays.toString(rand2));
            }
        }

        for (int i = 0; i < 10; i++) {
            OS.Close(fds[i]);
            System.out.println("Close success " + fds[i]);
        }

        OS.Exit();
    }
}
