import java.util.Arrays;

public class KernelMessage {
    public int senderPid;
    public int targetPid;
    public int messageType;
    public byte[] message;

    KernelMessage(KernelMessage segments) {
        this.senderPid = segments.senderPid;
        this.targetPid = segments.targetPid;
        this.messageType = segments.messageType;
        this.message = segments.message;
    }

    KernelMessage() {

    }

    @Override
    public String toString() {
        return "KernelMessage [senderPid=" + senderPid + ", targetPid=" + targetPid + ", messageType=" + messageType + ", message=" + Arrays.toString(message) + "]";
    }
}
