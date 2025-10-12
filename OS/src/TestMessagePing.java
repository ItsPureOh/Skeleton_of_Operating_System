import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TestMessagePing extends UserlandProcess{

    @Override
    public void main() {
        System.out.println("I'm Ping Process");
        int i = 0;
        String message = "Ping, Type: ";

        while (true) {
            // sending process
            KernelMessage segmentsSent = new KernelMessage();
            segmentsSent.targetPid = OS.GetPidByName("TestMessagePong");
            segmentsSent.messageType = i;
            segmentsSent.message = (message + segmentsSent.messageType).getBytes(StandardCharsets.UTF_8);
            OS.SendMessage(segmentsSent);

            // receiving process
            KernelMessage segmentsReceived = OS.WaitForMessage();
            System.out.println(Arrays.toString(segmentsReceived.message));
        }
    }
}
