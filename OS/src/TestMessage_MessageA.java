import java.nio.charset.StandardCharsets;

/**
 * MessengerA process.
 * -------------------
 * Secondary message-passing test that validates multiple concurrent
 * SendMessage / WaitForMessage interactions.
 * Works alongside MessengerB to exchange numbered messages back and forth.
 */
public class TestMessage_MessageA extends UserlandProcess {
    /**
     * Sends numbered messages to MessengerB and waits for responses.
     * Demonstrates correct message queue handling across concurrent user processes.
     * @return void
     */
    @Override
    public void main() {
        System.out.println("MessengerA started");

        int count = 1;
        KernelMessage msg = new KernelMessage();
        msg.targetPid = OS.GetPidByName("TestMessage_MessageB");
        msg.messageType = 200;

        // Run a finite exchange for clarity
        while (count <= 5) {
            String data = "Message " + count + " from A";
            msg.message = data.getBytes(StandardCharsets.UTF_8);

            // Send message to B
            OS.SendMessage(msg);
            System.out.println("A → B: " + data);

            // Wait for reply
            KernelMessage reply = OS.WaitForMessage();
            System.out.println("A received reply: " +
                    new String(reply.message, StandardCharsets.UTF_8));

            count++;
            cooperate();
        }

        System.out.println("MessengerA finished exchanges.");
    }
}
