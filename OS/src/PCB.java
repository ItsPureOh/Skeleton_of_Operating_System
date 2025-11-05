import java.util.Arrays;
import java.util.LinkedList;

/**
 * PCB (Process Control Block)
 * ---------------------------
 * Stores all metadata and control information about a process, including:
 * - Process ID (PID)
 * - Priority
 * - Reference to the userland process object
 * - Sleep/wakeup time
 * - Timeout information (for demotion)
 * - VFS handle table
 * - Message queue for inter-process communication
 */
public class PCB { // Process Control Block
    // Static counter used to assign unique process IDs (PIDs)
    private static int nextPid = 1;

    // Unique process identifier for this instance
    public int pid;

    // Scheduling priority assigned to this process
    private OS.PriorityType priority;

    // Reference to the user-level process object associated with this kernel process
    private final UserlandProcess process;

    // System time (in milliseconds) when this process should wake up
    // after a sleep or delay, and return to the ready queue
    public long wakeupTime;

    // Counter for how many times this process has reached a timeout condition
    public int timeoutFrequency = 0;

    // Indicates whether the process is currently in a timeout (sleeping or waiting) state
    public boolean timeout = false;

    // Array of Virtual File System (VFS) handles associated with this process
    public int[] vfsID = new int[10];

    // Human-readable name of the process (used for debugging or process management)
    public final String nameOfProcess;

    // Message queue used for interprocess communication (IPC)
    // Stores incoming KernelMessage objects sent by other processes
    public LinkedList<KernelMessage> messageQueue = new LinkedList<>();

    // Virtual memory mapping table for this process
    // Each index represents a virtual page number, and the value is the corresponding physical page number
    // A value of -1 indicates that the virtual page is not currently mapped
    public int[] virtualMemoryMappingTable = new int[100];

    /**
     * Constructor.
     * Initializes a new process control block with a unique PID,
     * its priority, and userland process reference.
     * @param up the userland process this PCB represents
     * @param priority the priority type of the process
     * @return void
     */
    PCB(UserlandProcess up, OS.PriorityType priority) {
        // assigning a new pid to a new process created
        this.pid = nextPid;
        nextPid++;
        this.process = up;
        this.priority = priority;
        this.nameOfProcess = up.getClass().getSimpleName();
        Arrays.fill(vfsID, -1);                             // Initialize all VFS handles to -1 (unused)
        Arrays.fill(virtualMemoryMappingTable, -1);         // Initialized TLB entries to -1
    }

    /**
     * Returns the name of the userland process.
     * @return String the process name
     */
    public String getName() { return nameOfProcess; }

    /**
     * Returns the process priority.
     * @return OS.PriorityType the priority type
     */
    OS.PriorityType getPriority() {
        return priority;
    }

    /**
     * Requests the process to stop at the next safe checkpoint.
     * @return void
     */
    public void requestStop() {
        process.requestStop();
    }

    /**
     * Stops the userland process completely.
     * Waits in a loop until the process confirms it is stopped.
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

    /**
     * Checks if the userland process has finished execution.
     * @return boolean true if done, false otherwise
     */
    public boolean isDone() { /* calls userlandprocess’ isDone() */
        return process.isDone();
    }

    /**
     * Starts the associated userland process.
     * @return void
     */
    void start() {
        // calls userlandprocess’ start()
        process.start();
    }

    /**
     * Changes the process’s priority level.
     * @param newPriority the new priority type
     * @return void
     */
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
