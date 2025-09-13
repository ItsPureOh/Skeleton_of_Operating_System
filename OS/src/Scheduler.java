import java.time.Clock;
import java.util.LinkedList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Scheduler {
    // Timer simulates the hardware timer chip. It fires regularly to expire quantums.
    private Timer timer = new Timer(true);
    // Reference to the process that is currently running.
    public PCB currentRunning;
    //random
    Random rand = new Random();
    // new list for sleeping processes
    private LinkedList<PCB> sleepingQueue = new LinkedList<>();
    // priority queue list
    private LinkedList<PCB> realtimeProcess = new LinkedList<>();
    private LinkedList<PCB> interactiveProcess = new LinkedList<>();
    private LinkedList<PCB> backgroundProcess = new LinkedList<>();


    public Scheduler() {
        // Timer runs every 250ms (quantum length).
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (currentRunning != null) {
                    // Flag the process to stop at next cooperate()
                    currentRunning.timeout = true;
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
        // add process to it's corresponding list
        if (p == OS.PriorityType.realtime){
            realtimeProcess.addLast(pcb);
        }
        else if (p == OS.PriorityType.interactive){
            interactiveProcess.addLast(pcb);
        }
        else{
            backgroundProcess.addLast(pcb);
        }

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
        PCB next = null;
        currentRunning = null;

        // check timeout for current process
        if (cur != null) {
            Demote(cur);
        }
        if (cur != null && !cur.isDone()){
            Requeue(cur);
        }

        // check sleep process should be wakening
        SleepingCheck();

        // Pull processes off the queue until we find one that isn’t finished.
        // 6/10 we will run a real-time process, 3/10 we will run an interactive process (if there is one) otherwise 1/10 we will run a background process.
        next = ProbabilisticProcessPicking();
        currentRunning = next;
    }

    public void Sleep(int milliseconds){
        // set the wakeupTime to process
        long wakeupTime = System.currentTimeMillis() + milliseconds;
        currentRunning.setWakeupTime(wakeupTime);
        // add that sleeping process to Sleeping Process List
        sleepingQueue.addLast(currentRunning);
        //testing
        System.out.println("process: " + currentRunning.pid + "sleeping now");
        // set the current Running process to null
        currentRunning = null;
    }

    private void Demote(PCB currentRunningProcess){
        if (currentRunningProcess.timeout){
            currentRunningProcess.timeout = false;
            currentRunningProcess.timeoutFrequency++;
            if (currentRunningProcess.timeoutFrequency >= 5){
                if (currentRunningProcess.getPriority() == OS.PriorityType.realtime){
                    currentRunningProcess.setPriority(OS.PriorityType.interactive);
                }
                else if (currentRunningProcess.getPriority() == OS.PriorityType.interactive){
                    currentRunningProcess.setPriority(OS.PriorityType.background);
                }
                currentRunningProcess.timeoutFrequency = 0;
            }
        }
        else {
            currentRunningProcess.timeoutFrequency = 0;
        }
    }

    private void Requeue(PCB currentRunningProcess){
        /*
        Nothing is currently running (we are at startup). We just don’t put null on our list.
        The user process is done() – we just don’t add it to the list.
        If there*was a running process, and it’s not finished, put it back into the queue.
         */
        // put process back to corresponding queue list
        if (currentRunningProcess.getPriority() == OS.PriorityType.realtime){
            realtimeProcess.addLast(currentRunningProcess);
        }
        else if (currentRunningProcess.getPriority() == OS.PriorityType.interactive){
            interactiveProcess.addLast(currentRunningProcess);
        }
        else {
            backgroundProcess.addLast(currentRunningProcess);
        }
    }

    private void SleepingCheck(){
        // check sleep process should be wakening
        long now = System.currentTimeMillis();
        for (int i = 0; i < sleepingQueue.size(); i++) {
            PCB p = sleepingQueue.get(i);
            if (p.getWakeupTime() <= now) {
                // move to the correct ready queue
                if (p.getPriority() == OS.PriorityType.realtime) {
                    realtimeProcess.addLast(p);
                } else if (p.getPriority() == OS.PriorityType.interactive) {
                    interactiveProcess.addLast(p);
                } else {
                    backgroundProcess.addLast(p);
                }
                // remove from sleepingQueue
                sleepingQueue.remove(i);
                i--; // adjust index since list shrinks
            }
        }
    }

    private PCB ProbabilisticProcessPicking(){
        PCB next = null;
        // Pull processes off the queue until we find one that isn’t finished.
        // 6/10 we will run a real-time process, 3/10 we will run an interactive process (if there is one) otherwise 1/10 we will run a background process.
        if (!realtimeProcess.isEmpty()) {
            int r = rand.nextInt(10); // 0–9
            if (r < 1 && !backgroundProcess.isEmpty()) {
                next = backgroundProcess.removeFirst();   // 10%
            } else if (r >= 1 && r < 4 && !interactiveProcess.isEmpty()) {
                next = interactiveProcess.removeFirst();  // 30%
            } else {
                // default: realtime (covers r=4–9, or fallback if others empty)
                next = realtimeProcess.removeFirst();
            }
        }
        else if (!interactiveProcess.isEmpty()) {
            int r = rand.nextInt(4); // 0–3
            if (r < 1 && !backgroundProcess.isEmpty()) {
                next = backgroundProcess.removeFirst();   // 25%
            } else {
                next = interactiveProcess.removeFirst();  // 75% or fallback
            }
        }
        else if (!backgroundProcess.isEmpty()) {
            next = backgroundProcess.removeFirst();
        }
        else {
            System.out.println("Error, No Process Exists");
            System.exit(0);
        }
        return next;
    }
}
