public class DeviceTest extends UserlandProcess {
    @Override
    public void main() {
        int fd = OS.Open("file testfile");
        if (fd == -1) {
            System.out.println("Open failed");
            OS.Exit();
            return;
        }

        byte[] message = "hello".getBytes();
        OS.Write(fd, message);

        OS.Seek(fd, 0);
        byte[] data = OS.Read(fd, message.length);

        System.out.println("Read back: " + new String(data));

        OS.Close(fd);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        OS.Exit();
    }
}