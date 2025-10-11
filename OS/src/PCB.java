import java.util.LinkedList;

/**
 * PCB (Process Control Block).
 * Holds metadata and control for a single process:
 * - PID
 * - priority
 * - reference to the userland process
 * - sleep/wakeup time
 * - timeout information for demotion
 */
public class PCB { // Process Control Block
    private static int nextPid = 1;
    public int pid;
    private OS.PriorityType priority;
    private final UserlandProcess process;
    private long wakeupTime;    // time (in milliseconds) when this process should wake up from sleep and return to the ready queue.
    public int timeoutFrequency = 0;
    public boolean timeout = false;
    public int [] vfsID = new int [10];         // Array of Virtual File System (VFS) handles associated with this process.
    public final String nameOfProcess;
    public LinkedList<KernelMessage> messageQueue;

    // Constructor assigns PID and stores priority/process reference
    PCB(UserlandProcess up, OS.PriorityType priority) {
        // assigning a new pid to a new process created
        this.pid = nextPid;
        // increment the pid for next process in the future
        nextPid++;
        this.process = up;
        this.priority = priority;
        this.nameOfProcess = up.getClass().getSimpleName();

        // Initialize all VFS handles to -1 (unused)
        for (int i = 0; i < vfsID.length; i++) {
            vfsID[i] = -1;
        }
    }

    public String getName() { return nameOfProcess; }

    OS.PriorityType getPriority() {
        return priority;
    }

    public void requestStop() {
        process.requestStop();
    }

    /**
     * Stops the associated userland process.
     * Calls its stop method and waits in a loop until
     * the process confirms it has fully stopped.
     * @return void
     */
    public void stop() {
        /* calls user land process’ stop. Loops with Thread.sleep() until the up.isStopped() is true.  */
        process.stop();
        while(!process.isStopped()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean isDone() { /* calls userlandprocess’ isDone() */
        return process.isDone();
    }

    void start() {
        // calls userlandprocess’ start()
        process.start();
    }

    public void setPriority(OS.PriorityType newPriority) {
        priority = newPriority;
    }

    /**
     * Sets the absolute system time (in milliseconds) when this process
     * should wake up from sleep.
     * @param newWakeupTime the wake-up time in milliseconds since epoch
     * @return void
     */
    public void setWakeupTime(long newWakeupTime) {
        this.wakeupTime = newWakeupTime;
    }

    /**
     * Returns the absolute system time (in milliseconds) when this process
     * is scheduled to wake up.
     * @return long the wake-up time in milliseconds since epoch
     */
    public long getWakeupTime() {
        return wakeupTime;
    }
}
