import java.util.Arrays;

/**
 * KernelMessage class.
 * Represents a message passed between user processes in the simulated OS.
 * Contains metadata (sender/target PIDs, type) and a message payload (byte array).
 */
public class KernelMessage {
    public int senderPid;
    public int targetPid;
    public int messageType;
    public byte[] message;

    /**
     * Copy constructor.
     * Creates a new KernelMessage identical to another one.
     * Performs a deep copy of the message array to prevent shared references.
     * @param segments the KernelMessage to copy
     * @return void
     */
    KernelMessage(KernelMessage segments) {
        this.senderPid = segments.senderPid;
        this.targetPid = segments.targetPid;
        this.messageType = segments.messageType;
        this.message = Arrays.copyOf(segments.message, segments.message.length);
    }

    /**
     * Full constructor.
     * Initializes a new message with specified sender, target, type, and data.
     * @param senderPid process ID of the sender
     * @param targetPid process ID of the receiver
     * @param messageType integer code identifying the message type
     * @param message byte array containing the message content
     * @return void
     */
    public KernelMessage(int senderPid, int targetPid, int messageType, byte[] message) {
        this.senderPid = senderPid;
        this.targetPid = targetPid;
        this.messageType = messageType;
        this.message = Arrays.copyOf(message, message.length);
    }

    KernelMessage() {
    }

    /**
     * Returns a string representation of the message for debugging.
     * Includes sender, target, type, and message content.
     * @return String representation of this message
     */
    @Override
    public String toString() {
        return "KernelMessage [senderPid=" + senderPid + ", targetPid=" + targetPid + ", messageType=" + messageType + ", message=" + Arrays.toString(message) + "]";
    }
}
