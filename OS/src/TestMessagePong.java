import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TestMessagePong extends UserlandProcess {

    @Override
    public void main() {
        System.out.println("I'm Pong Process");
        int i = 0;
        String message = "Pong, Type: ";

        while (true) {
            // sending process
            KernelMessage segmentsSent = new KernelMessage();
            segmentsSent.targetPid = OS.GetPidByName("TestMessagePing");
            segmentsSent.messageType = i;
            segmentsSent.message = (message + segmentsSent.messageType).getBytes(StandardCharsets.UTF_8);
            OS.SendMessage(segmentsSent);

            // receiving process
            KernelMessage segmentsReceived = OS.WaitForMessage();
            if (segmentsReceived != null) {
                System.out.println(new String(segmentsReceived.message, StandardCharsets.UTF_8));
            }
            i++;
            cooperate();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
