import java.util.*;

/**
 * Scheduler class.
 * ----------------
 * Manages process scheduling, priority queues, sleeping processes,
 * waiting message processes, and periodic quantum expiration via a timer.
 * Handles switching, demotion, sleeping, and probabilistic scheduling.
 */
public class Scheduler {
    // Hardware timer simulation used to trigger periodic interrupts.
    // The timer fires at regular intervals to signal quantum expiration.
    private Timer timer = new Timer(true);

    // Reference to the process currently executing on the CPU.
    public PCB currentRunning;

    // Random generator used for scheduling or test operations (e.g., randomized replacement).
    private final Random rand = new Random();

    // Priority queue that holds sleeping processes, ordered by their wake-up time.
    // The process with the earliest wake-up time is placed at the head of the queue.
    private final PriorityQueue<PCB> sleepingQueue = new PriorityQueue<>(Comparator.comparingLong(pcb -> pcb.wakeupTime));

    // Process queues by priority level.
    // Each queue represents a scheduling category.
    private final LinkedList<PCB> realtimeProcess = new LinkedList<>();     // Highest-priority tasks
    private final LinkedList<PCB> interactiveProcess = new LinkedList<>();  // User-facing or interactive tasks
    private final LinkedList<PCB> backgroundProcess = new LinkedList<>();   // Low-priority or background tasks

    // Map of processes that are currently waiting for messages or synchronization events.
    // The key is the PID of the waiting process, and the value is its PCB.
    private final HashMap<Integer, PCB> waitingProcess = new HashMap<>();

    // Reference to the main Kernel instance, allowing access to kernel-level operations.
    private final Kernel ki;

    // Process table mapping process IDs (PID) to their corresponding PCBs.
    // Used for process lookup, management, and scheduling.
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
            ki.CleanUpDevice(currentRunningProcess);
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
        return next;
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

    /**
     * Removes the currently running process from the global process map.
     * This is typically called when a process terminates or is being replaced.
     */
    public void RemoveCurrentProcessFromTheMap(){
        processMap.remove(currentRunning.pid);
    }

    /**
     * Moves the currently running process into the waiting process map.
     * Marks the process as stopped so that it no longer executes until
     * the condition it is waiting for is satisfied.
     */
    public void PutCurrentProcessInTheWaitingMap(){
        currentRunning.requestStop();
        waitingProcess.put(currentRunning.pid, currentRunning);

    }

    /**
     * Checks whether a process with the specified PID exists in the waiting map.
     *
     * @param pid the process ID to check
     * @return the PCB if the process is waiting; null otherwise
     */
    public PCB CheckWaitingProcess(int pid){
        if (waitingProcess.containsKey(pid)){
            return waitingProcess.get(pid);
        }
        return null;
    }

    /**
     * Removes a process with the specified PID from the waiting map and returns it.
     * Used when the process’s waiting condition is fulfilled.
     *
     * @param pid the process ID to remove
     * @return the removed PCB if found; null otherwise
     */
    public PCB RemoveWaitingProcess(int pid){
        if (waitingProcess.containsKey(pid)){
            return waitingProcess.remove(pid);
        }
        return null;
    }

    /**
     * Clears all pending messages in the currently running process’s message queue.
     * Prevents message buildup when the process is being reset or terminated.
     */
    public void ClearMessageInQueue(){
        if (currentRunning.messageQueue == null){
            return;
        }
        currentRunning.messageQueue.clear();
    }

    /**
     * Removes the specified process from all scheduling queues it may belong to,
     * including realtime, interactive, background, and sleeping queues.
     *
     * @param process the process to remove from the queues
     */
    public void RemoveFromPriorityQueue(PCB process){
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

    /**
     * Removes the currently running process from the waiting process map.
     * This is typically called when the process resumes execution after waiting.
     */
    public void RemoveCurrentProcessFromWaitingProcessMap(){
        waitingProcess.remove(currentRunning.pid);
    }

    public PCB GetRandomProcesss(){
        // get total number of processes
        int size = processMap.size();
        PCB randomPCB;

        // check if victim process has physical memory
        do{
            int randomIndex = new Random().nextInt(size);
            randomPCB = processMap.values().toArray(new PCB[0])[randomIndex];
        } while (!PhysicalMemoryAvailabilityCheck(randomPCB));

        return randomPCB;
    }

    private boolean PhysicalMemoryAvailabilityCheck(PCB process){
        for(int i = 0; i < process.virtualMemoryMappingTable.length; i++){
            if (process.virtualMemoryMappingTable[i] != null &&
                    process.virtualMemoryMappingTable[i].physicalPage >= 0){
                return true;
            }
        }
        return false;
    }

    public int GetVictimPage(PCB process){
        int randomIndex;
        while (true){
            randomIndex = new Random().nextInt(process.virtualMemoryMappingTable.length);
            VirtualToPhysicalMapping mapping = process.virtualMemoryMappingTable[randomIndex];
            // check if this virtual page is a valid victim
            if (mapping != null && mapping.physicalPage >= 0) {
                return randomIndex;
            }
        }
    }
}
