import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TestMessagePong extends UserlandProcess {

    @Override
    public void main() {
        // wrap message into the message object
        KernelMessage segmentsSent = new KernelMessage();
        String message = "PONG - ";
        segmentsSent.targetPid = OS.GetPidByName("TestMessagePing");
        segmentsSent.messageType = 100;
        segmentsSent.message = (message + segmentsSent.messageType).getBytes(StandardCharsets.UTF_8);
        while (true) {
            // send message object to Pong
            OS.SendMessage(segmentsSent);

            // receiving process
            KernelMessage segmentsReceived = OS.WaitForMessage();
            //actually get the message
            if (segmentsReceived != null) {
                System.out.println(new String(segmentsReceived.message, StandardCharsets.UTF_8));
            }

            cooperate();
        }
    }
}
