import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TestMessagePing extends UserlandProcess{

    @Override
    public void main() {
        System.out.println("I'm Ping Process");
        // wrap message into the message object
        KernelMessage segmentsSent = new KernelMessage();
        String message = "PING - ";
        segmentsSent.targetPid = OS.GetPidByName("TestMessagePong");
        segmentsSent.messageType = 99;
        segmentsSent.message = (message + segmentsSent.messageType).getBytes(StandardCharsets.UTF_8);
        while (true) {
            // send sends
            OS.SendMessage(segmentsSent);
            /*
            // receiving process
            KernelMessage segmentsReceived = OS.WaitForMessage();
            if (segmentsReceived != null) {
                System.out.println(new String(segmentsReceived.message, StandardCharsets.UTF_8));
            }
             */
            cooperate();
        }
    }
}
