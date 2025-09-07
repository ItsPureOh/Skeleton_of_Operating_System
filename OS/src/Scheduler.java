import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class Scheduler {
    // Queue of all processes waiting to run.
    private LinkedList<PCB> processQueue = new LinkedList<>();
    // Timer simulates the hardware timer chip. It fires regularly to expire quantums.
    private Timer timer = new Timer(true);
    // Reference to the process that is currently running.
    public PCB currentRunning;

    public Scheduler() {
        // Timer runs every 250ms (quantum length).
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (currentRunning != null) {
                    // Flag the process to stop at next cooperate()
                    currentRunning.requestStop();
                }
            }
        }, 250, 250);
    }

    /**
     * Creates a new process and adds it to the scheduler queue.
     * @param up The userland process to wrap in a PCB.
     * @param p The priority (not used in Assignment 1, but needed for later).
     * @return The process ID (pid) of the new process.
     */
    public int CreateProcess(UserlandProcess up, OS.PriorityType p){
        // Wrap the user process in a PCB (gives it a PID, etc.)
        PCB pcb = new PCB(up, p);
        processQueue.addLast(pcb);
        // if nothing else is running, call switchProcess
        if (currentRunning == null) {
            SwitchProcess();
        }
        return pcb.pid;
    }

    /**
     * Switches from the current process to the next one in the queue.
     * Implements round-robin scheduling:
     * - If the current process is still alive, it is moved to the back of the queue.
     * - Dead processes are skipped and not re-added.
     * - The next process in the queue becomes the current running process.
     */
    public void SwitchProcess(){
        PCB cur = currentRunning;
        currentRunning = null;

        // Nothing is currently running (we are at startup). We just don’t put null on our list.
        // The user process is done() – we just don’t add it to the list.
        // If there*was a running process and it’s not finished, put it back into the queue.
        if (cur != null && !cur.isDone()){
            processQueue.addLast(cur);
        }
        // Pull processes off the queue until we find one that isn’t finished.
        while (!processQueue.isEmpty()){
            PCB next = processQueue.removeFirst();
            if (!next.isDone()){
                currentRunning = next;
                break;
            }
        }
    }
}
