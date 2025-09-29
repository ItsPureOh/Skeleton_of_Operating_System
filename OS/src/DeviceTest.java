public class DeviceTest extends UserlandProcess {
    private final String name;

    public DeviceTest(String name) {
        this.name = name;
    }

    @Override
    public void main() {
        System.out.println(name + " starting (pid=" + OS.GetPID() + ")");

        // Open a file
        int fd = OS.Open("shared.txt");
        System.out.println(name + " opened shared.txt with fd=" + fd);

        // Write something
        OS.Write(fd, (name + " says hello!\n").getBytes());
        System.out.println(name + " wrote message.");

        // Sleep a bit to let scheduler switch
        OS.Sleep(200);
        cooperate();

        // Write again
        OS.Write(fd, (name + " still alive!\n").getBytes());
        System.out.println(name + " wrote second message.");

        // Close file
        OS.Close(fd);
        System.out.println(name + " closed shared.txt.");

        System.out.println(name + " finished (pid=" + OS.GetPID() + ")");
    }
}
