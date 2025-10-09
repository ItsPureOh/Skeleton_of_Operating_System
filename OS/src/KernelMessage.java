import java.util.Arrays;

public class KernelMessage {
    public int senderPid;
    public final int targetPid;
    public final int messageType;
    public final byte[] message;

    KernelMessage(KernelMessage segments) {
        this.targetPid = segments.targetPid;
        this.messageType = segments.messageType;
        this.message = segments.message;
    }

    @Override
    public String toString() {
        return "KernelMessage [senderPid=" + senderPid + ", targetPid=" + targetPid + ", messageType=" + messageType + ", message=" + Arrays.toString(message) + "]";
    }
}
