import java.nio.charset.StandardCharsets;

/**
 * MessengerB process.
 * -------------------
 * Companion to MessengerA. Waits for messages from A and replies
 * with acknowledgment messages, verifying proper scheduling and message delivery.
 */
public class TestMessage_MessageB extends UserlandProcess {

    /**
     * Waits for messages from MessengerA, replies with acknowledgments,
     * and demonstrates multiple message handling in a loop.
     * @return void
     */
    @Override
    public void main() {
        System.out.println("MessengerB started");

        KernelMessage reply = new KernelMessage();
        reply.messageType = 201;

        while (true) {
            // Wait for message from A
            KernelMessage recv = OS.WaitForMessage();
            String receivedText = new String(recv.message, StandardCharsets.UTF_8);
            System.out.println("B received: " + receivedText);

            // Reply back to A
            reply.targetPid = recv.senderPid;
            String ack = "ACK for " + receivedText;
            reply.message = ack.getBytes(StandardCharsets.UTF_8);
            OS.SendMessage(reply);

            cooperate();
        }
    }
}
