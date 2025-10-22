import java.sql.SQLOutput;
import java.time.Clock;
import java.util.*;

/**
 * Scheduler class.
 * ----------------
 * Manages process scheduling, priority queues, sleeping processes,
 * waiting message processes, and periodic quantum expiration via a timer.
 * Handles switching, demotion, sleeping, and probabilistic scheduling.
 */
public class Scheduler {
    private Timer timer = new Timer(true);          // Timer simulates the hardware timer chip. It fires regularly to expire quantums.
    public PCB currentRunning;                               // Reference to the process that is currently running.
    Random rand = new Random();
    //final private LinkedList<PCB> sleepingQueue = new LinkedList<>(); // new list for sleeping processes
    private final PriorityQueue<PCB> sleepingQueue = new PriorityQueue<>(Comparator.comparingLong(pcb -> pcb.wakeupTime));
    // priority queue list
    final private LinkedList<PCB> realtimeProcess = new LinkedList<>();
    final private LinkedList<PCB> interactiveProcess = new LinkedList<>();
    final private LinkedList<PCB> backgroundProcess = new LinkedList<>();

    // waiting message queue list
    final private HashMap<Integer, PCB> waitingProcess = new HashMap<>();

    //kernel ref.
    private final Kernel ki;

    // hashMap that contains process table with PID
    public HashMap<Integer, PCB> processMap = new HashMap<>();

    /**
     * Constructor.
     * Initializes the scheduler and starts a timer that expires every 250ms.
     * Each tick marks the current process as timed out.
     * @param k reference to the kernel instance
     * @return void
     */
    public Scheduler(Kernel k) {
        // Timer runs every 250ms (quantum length).
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (currentRunning != null) {
                    // Flag the process to stop at next cooperate()
                    currentRunning.timeout = true;

                    currentRunning.requestStop();
                }
            }
        }, 0, 250);
        ki = k;
    }

    /**
     * Creates and registers a new process.
     * Wraps the userland process in a PCB, enqueues it based on priority,
     * stores it in the process map, and ensures a process is running.
     * @param up the userland process to create
     * @param p the priority type (realtime, interactive, background)
     * @return int the PID of the created process
     */
    public int CreateProcess(UserlandProcess up, OS.PriorityType p){
        // Wrap the user process in a PCB (gives it a PID, etc.)
        PCB pcb = new PCB(up, p);

        // enqueues it in the appropriate priority queue
        if (p == OS.PriorityType.realtime){
            realtimeProcess.addLast(pcb);
            System.out.println("Created process " + pcb.pid + " (realtime)");
        }
        else if (p == OS.PriorityType.interactive){
            interactiveProcess.addLast(pcb);
            System.out.println("Created process " + pcb.pid + " (interactive)");
        }
        else{
            backgroundProcess.addLast(pcb);
            System.out.println("Created process " + pcb.pid + " (background)");
        }

        // if nothing else is running, call switchProcess
        if (currentRunning == null) {
            SwitchProcess();
        }

        //store PCB in hashMap
        processMap.put(pcb.pid, pcb);

        //System.out.println("Process: " + pcb.pid + "Created");
        return pcb.pid;
    }

    /**
     * Switches CPU control from the current process to the next.
     * Handles demotion, requeueing, waking sleepers, and process selection.
     * @return void
     */
    public void SwitchProcess(){
        PCB cur = currentRunning;
        PCB next = null;
        //currentRunning = null;
        // Check if the current process timed out and apply demotion if needed
        // the current process is not finished, requeue it based on priority
        if (cur != null && !cur.isDone()) {
            Demote(cur);
            if (!waitingProcess.containsKey(cur.pid)) {
                Requeue(cur);
            }
        }
        // Wake up sleeping processes whose timers have expired
        SleepingCheck();

        // Select the next process to run (probabilistic choice by priority)
        do {
            next = ProbabilisticProcessPicking();
            // remove the prccess if we find there is a finished Process in the queue
            if (next.isDone()) {
                processMap.remove(next.pid);
            }
        } while (next == null || next.isDone());

        /*
        System.out.println("Switching from " +
                (cur == null ? "kernel" : cur.pid + "(" +
                        (cur.getPriority() == OS.PriorityType.realtime ? "realtime" :
                                cur.getPriority() == OS.PriorityType.interactive ? "interactive" :
                                        "background") + ")") +
                " to " +
                (next == null ? "none" : next.pid + "(" +
                        (next.getPriority() == OS.PriorityType.realtime ? "realtime" :
                                next.getPriority() == OS.PriorityType.interactive ? "interactive" :
                                        "background") + ")"));

         */
        PrintQueues();

        // assign the next process as running
        currentRunning = next;
    }

    /**
     * Puts the currently running process to sleep for the given duration.
     *
     * Steps:
     * - Compute the absolute wake-up time.
     * - Store this in the PCB.
     * - Move the process into the sleeping queue.
     * - Clear the current running slot so the scheduler can pick another process.
     *
     * @param milliseconds the duration the process should sleep, in ms
     */
    public void Sleep(int milliseconds){
        // Calculate when the process should wake up
        long wakeupTime = System.currentTimeMillis() + milliseconds;
        currentRunning.setWakeupTime(wakeupTime);
        // add that sleeping process to Sleeping Process List
        sleepingQueue.add(currentRunning);
        //testing
        System.out.println("process: " + currentRunning.pid + "sleeping now");
        // Clear currentRunning so the scheduler can select another process
        currentRunning = null;
    }

    /**
     * Checks if the given process should be demoted due to repeated timeouts.
     * - Increments the timeout counter if the process timed out.
     * - Demotes from realtime → interactive or interactive → background after 5 consecutive timeouts.
     * - Resets the counter if the process yielded voluntarily.
     *
     * @param currentRunningProcess the process being checked for demotion
     */
    private void Demote(PCB currentRunningProcess){
        if (currentRunningProcess.timeout){
            currentRunningProcess.timeout = false;
            currentRunningProcess.timeoutFrequency++;

            // Demote priority if process timed out 5 times in a row
            if (currentRunningProcess.timeoutFrequency >= 5){
                if (currentRunningProcess.getPriority() == OS.PriorityType.realtime){
                    currentRunningProcess.setPriority(OS.PriorityType.interactive);
                }
                else if (currentRunningProcess.getPriority() == OS.PriorityType.interactive){
                    currentRunningProcess.setPriority(OS.PriorityType.background);
                }
                currentRunningProcess.timeoutFrequency = 0; // reset after demotion
            }
        }
        else {
            currentRunningProcess.timeoutFrequency = 0; // reset if process yielded voluntarily
        }
    }

    /**
     * Places the given process back into the appropriate ready queue
     * based on its current priority. Processes that are finished
     * are not requeued (handled by caller).
     *
     * @param  currentRunningProcess the process to requeue
     */
    public void Requeue(PCB currentRunningProcess){

        if (currentRunningProcess == null){
            return;
        }
        if (currentRunningProcess.isDone()){
            // clean up the space if process is finished
            ki.cleanUpDevice(currentRunningProcess);
            // remove that process from the map
            processMap.remove(currentRunningProcess.pid);
            return;
        }

        // >>> KEY GUARD: don't requeue if this PID is waiting-for-message
        if(!waitingProcess.isEmpty()){
            if (waitingProcess.containsKey(currentRunningProcess.pid)) return;
        }
        /*
        Nothing is currently running (we are at startup). We just don’t put null on our list.
        The user process is done() – we just don’t add it to the list.
        If there*was a running process, and it’s not finished, put it back into the queue.
         */
        // Add process to the correct queue by priority
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

    /**
     * Moves any processes whose sleep time has expired
     * from the sleeping queue back into their appropriate
     * ready queues.
     *
     * @return void
     */
    private void SleepingCheck(){
        long now = System.currentTimeMillis();
        /*
        // check sleep process should be wakening
        for (int i = 0; i < sleepingQueue.size(); i++) {
            PCB p = sleepingQueue.get(i);
            // If process wake-up time has passed, requeue it
            if (p.getWakeupTime() <= now) {
                // move to the correct queue
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

         */
        while (!sleepingQueue.isEmpty() && sleepingQueue.peek().getWakeupTime() <= now) {
            PCB p = sleepingQueue.poll();

            // If process wake-up time has passed, requeue it
            if (p.getWakeupTime() <= now) {
                // move to the correct queue
                if (p.getPriority() == OS.PriorityType.realtime) {
                    realtimeProcess.addLast(p);
                } else if (p.getPriority() == OS.PriorityType.interactive) {
                    interactiveProcess.addLast(p);
                } else {
                    backgroundProcess.addLast(p);
                }
            }
            System.out.println("Process " + p.pid + " woke up and requeued.");
        }
    }

    /**
     * Selects the next process to run using probabilistic scheduling.
     * - If realtime processes exist: 60% realtime, 30% interactive (if available),
     *   10% background (if available).
     * - If only interactive/background exist: 75% interactive, 25% background.
     * - If only background exists: always background.
     * - If no processes exist: prints error and exits.
     *
     * @return PCB the next process chosen to run
     */
    private PCB ProbabilisticProcessPicking(){
        PCB next = null;

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
        /*
        else {
            // Debug
            System.out.println("Error, No Process Exists");
            System.exit(0);
        }
         */
        return next;
    }

    public PCB getCurrentRunningProcess(){
        return currentRunning;
    }

    public int getPid(PCB currentRunningProcess){
        return currentRunningProcess.pid;
    }

    /**
     * Prints the current contents of all process queues for debugging.
     */
    public void PrintQueues() {
        System.out.print("Realtime Queue: ");
        for (PCB p : realtimeProcess) System.out.print(p.pid + " ");
        System.out.println();

        System.out.print("Interactive Queue: ");
        for (PCB p : interactiveProcess) System.out.print(p.pid + " ");
        System.out.println();

        System.out.print("Background Queue: ");
        for (PCB p : backgroundProcess) System.out.print(p.pid + " ");
        System.out.println();

        System.out.print("Sleeping Queue: ");
        for (PCB p : sleepingQueue) System.out.print(p.pid + " ");
        System.out.println();

        System.out.print("Waiting Map: ");
        for (PCB p : waitingProcess.values()) {
            int inbox = (p.messageQueue == null) ? 0 : p.messageQueue.size();
            System.out.print(p.pid + "[inbox=" + inbox + "] ");
        }
        System.out.println("\n");
    }

    public void removeCurrentProcessFromTheMap(){
        processMap.remove(currentRunning.pid);
    }

    public void putCurrentProcessInTheWaitingMap(){
        currentRunning.requestStop();
        waitingProcess.put(currentRunning.pid, currentRunning);

    }

    public PCB checkWaitingProcess(int pid){
        if (waitingProcess.containsKey(pid)){
            return waitingProcess.get(pid);
        }
        return null;
    }

    public PCB removeWaitingProcess(int pid){
        if (waitingProcess.containsKey(pid)){
            return waitingProcess.remove(pid);
        }
        return null;
    }

    public void clearMessageInQueue(){
        if (currentRunning.messageQueue == null){
            return;
        }
        currentRunning.messageQueue.clear();
    }

    public void removeFromPriorityQueue(PCB process){
        if (process.getPriority() == OS.PriorityType.realtime){
            realtimeProcess.remove(process);
        }
        else if (process.getPriority() == OS.PriorityType.interactive){
            interactiveProcess.remove(process);
        }
        else if (process.getPriority() == OS.PriorityType.background){
            backgroundProcess.remove(process);
        }
        sleepingQueue.remove(process);
    }

    public void removeCurrentProcessFromWaitingProcessMap(){
        waitingProcess.remove(currentRunning.pid);
    }

}
