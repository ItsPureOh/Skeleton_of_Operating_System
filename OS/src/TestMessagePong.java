import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * TestMessagePong process.
 * ------------------------
 * The companion process to TestMessagePing.
 * This program repeatedly receives "PING" messages from the Ping process,
 * replies with a "PONG" message, and demonstrates message-based synchronization
 * between two userland processes.
 *
 * Together, TestMessagePing and TestMessagePong form the “ping-pong” test,
 * verifying that message passing and process scheduling function correctly.
 */
public class TestMessagePong extends UserlandProcess {
    /**
     * Main execution entry point for the Pong process.
     * Steps:
     *  - Resolves the PID of the Ping process by name.
     *  - Wraps "PONG" message into a KernelMessage object.
     *  - Repeatedly sends and receives messages in a ping-pong loop.
     *  - Uses cooperate() to yield CPU time for fair scheduling.
     * @return void
     */
    @Override
    public void main() {
        // Prepare a message to send to Ping
        KernelMessage segmentsSent = new KernelMessage();
        String message = "PONG - ";
        segmentsSent.targetPid = OS.GetPidByName("TestMessagePing");
        segmentsSent.messageType = 100;
        segmentsSent.message = (message + segmentsSent.messageType).getBytes(StandardCharsets.UTF_8);
        // Infinite ping-pong communication loop
        while (true) {
            // Send message to Ping
            OS.SendMessage(segmentsSent);
            System.out.println("PONG Sent Message Successfully");

            // Wait for incoming message from Ping
            KernelMessage recv = OS.WaitForMessage();
            System.out.println("PONG Received Message Successfully");

            // Ensure target PID is still valid; reacquire if needed
            if (segmentsSent.targetPid <= 0) {
                segmentsSent.targetPid = OS.GetPidByName("TestMessagePing");
                if (segmentsSent.targetPid <= 0) { cooperate(); continue; }
            }
            System.out.println(new String(recv.message, StandardCharsets.UTF_8));

            // Yield CPU to Ping
            cooperate();

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
