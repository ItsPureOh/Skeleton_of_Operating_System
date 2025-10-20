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
            OS.SendMessage(segmentsSent);
            System.out.println("PONG Sent Message Successfully");
            KernelMessage recv = OS.WaitForMessage();
            System.out.println("PONG Received Message Successfully");
            if (segmentsSent.targetPid <= 0) {
                segmentsSent.targetPid = OS.GetPidByName("TestMessagePing");
                if (segmentsSent.targetPid <= 0) { cooperate(); continue; }
            }
            System.out.println(new String(recv.message, StandardCharsets.UTF_8));
            cooperate();
            /*
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
             */
        }
    }
}
