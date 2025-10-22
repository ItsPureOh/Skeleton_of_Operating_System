import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * TestMessagePing process.
 * ------------------------
 * A simple test program that demonstrates inter-process communication
 * using the OS message system. This process repeatedly sends "PING"
 * messages to a corresponding Pong process and waits for replies.
 *
 * Acts as one half of a ping-pong message exchange test between
 * TestMessagePing and TestMessagePong.
 */
public class TestMessagePing extends UserlandProcess{

    /**
     * Main execution entry point for the Ping process.
     * Steps:
     *  - Resolves the PID of the Pong process by name.
     *  - Wraps "PING" message into a KernelMessage object.
     *  - Repeatedly sends the message, waits for a reply, and prints results.
     *  - Demonstrates cooperative multitasking using cooperate().
     * @return void
     */
    @Override
    public void main() {
        System.out.println("I'm Ping Process");

        // Prepare a message to send to Pong
        KernelMessage segmentsSent = new KernelMessage();
        String message = "PING - ";
        segmentsSent.targetPid = OS.GetPidByName("TestMessagePong");
        segmentsSent.messageType = 99;
        segmentsSent.message = (message + segmentsSent.messageType).getBytes(StandardCharsets.UTF_8);

        // Infinite ping-pong communication loop
        while (true) {
            // Send message to Pong
            OS.SendMessage(segmentsSent);
            System.out.println("PING send Message Successfully");

            // Yield CPU to allow Pong to run
            cooperate();

            // Wait for Pong’s reply
            KernelMessage recv = OS.WaitForMessage();
            System.out.println("PING received Message Successfully");
            System.out.println(new String(recv.message, StandardCharsets.UTF_8));

            // Sleep briefly to slow output for readability
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
