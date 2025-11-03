import java.util.HashMap;
import java.util.Random;

/**
 * Kernel Class
 * -------------
 * Core of the simulated operating system.
 * Handles process scheduling, system calls, and message passing.
 * Works closely with the Scheduler and Virtual File System (VFS).
 */
public class Kernel extends Process implements Device  {
    // Handles process queues & context switching
    private Scheduler scheduler = new Scheduler(this);
    // Simulated file system for I/O
    private VirtualFileSystem vfs = new VirtualFileSystem();
    // keep tracking of the free memory
    private boolean[] freePage = new boolean[1024];
    // size of single page
    final private int sizeOfPage = 1024;

    public Kernel() {
    }
    /**
     * Main kernel loop.
     * Continuously handles system calls from userland processes
     * and switches execution between them.
     * @return void
     */
    public void main() {
        while (true) { // kernel runs forever
            // Dispatch based on the current system call from a user process
            switch (OS.currentCall) {
                // extract parameters and create a new process
                case CreateProcess -> OS.retVal = CreateProcess((UserlandProcess) OS.parameters.get(0), (OS.PriorityType) OS.parameters.get(1));

                // context switch request
                case SwitchProcess -> SwitchProcess();

                // Priority scheduler calls
                case Sleep -> Sleep((int) OS.parameters.get(0));
                case GetPID -> OS.retVal = GetPid();
                case Exit -> Exit();

                // Devices
                case Open -> OS.retVal = Open((String)OS.parameters.get(0));
                case Close -> Close((int)OS.parameters.get(0));
                case Read -> OS.retVal = Read((int)OS.parameters.get(0), (int)OS.parameters.get(1));
                case Seek -> Seek((int)OS.parameters.get(0), (int)OS.parameters.get(1));
                case Write -> OS.retVal = Write((int)OS.parameters.get(0), (byte[]) OS.parameters.get(1));

                // Messages
                case GetPIDByName -> OS.retVal = GetPidByName((String)OS.parameters.get(0));
                case SendMessage -> SendMessage((KernelMessage) OS.parameters.get(0));
                case WaitForMessage -> OS.retVal = WaitForMessage();
                /*
                // Memory
                case GetMapping ->
                case AllocateMemory ->
                case FreeMemory ->
                 */
            }
            // TODO: Now that we have done the work asked of us, start some process then go to sleep.
            // Ensure something is runnable before proceeding
            while (scheduler.currentRunning == null) {
                SwitchProcess();
            }

            // Start the chosen process
            getCurrentRunning().start();

            // Call stop() on myself(kernel), so that there is only one process is running
            this.stop();
        }
    }

    /**
     * Performs a context switch using the scheduler.
     * Selects and sets the next runnable process.
     * @return void
     */
    private void SwitchProcess() {
        scheduler.SwitchProcess();
    }

    /**
     * Creates a new process and adds it to the scheduler.
     * @param up the userland process to create
     * @param priority the priority level (realtime, interactive, background)
     * @return int the PID of the created process
     */
    private int CreateProcess(UserlandProcess up, OS.PriorityType priority) {
        return scheduler.CreateProcess(up, priority);
    }

    /**
     * Gets the PCB (process control block) of the currently running process.
     * @return PCB the current running process
     */
    public PCB getCurrentRunning(){
        return scheduler.currentRunning;
    }

    /**
     * Puts the current process to sleep for a specified duration.
     * Then triggers a context switch to another process.
     * @param mills duration to sleep, in milliseconds
     * @return void
     */
    private void Sleep(int mills) {
        scheduler.Sleep(mills);
        scheduler.SwitchProcess();
    }

    /**
     * Terminates the current process and performs cleanup.
     * Removes it from all scheduler queues and maps, and switches to another process.
     * @return void
     */
    private void Exit() {
        while(scheduler.currentRunning == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        // unscheduled the current process so that it never gets run again
        System.out.println("The Process is Terminated: " + scheduler.currentRunning.pid);
        // clean Up the device
        cleanUpDevice(getCurrentRunning());
        // remove it from the hashmap in scheduler
        scheduler.removeCurrentProcessFromTheMap();
        // remove the queue message from the process list
        scheduler.clearMessageInQueue();
        // remove the process from the waiting message map
        scheduler.removeCurrentProcessFromWaitingProcessMap();
        // remove the process from the queue
        scheduler.currentRunning = null;

        //schedule should choose something else to run
        SwitchProcess();
    }

    /**
     * Returns the PID of the currently running process.
     * @return int the PID of the current process
     */
    private int GetPid() {
        return getCurrentRunning().pid;
    }

    /**
     * Sends a message from the current process to another process.
     * Adds the message to the target process’s queue and requeues it if waiting.
     * @param km the message to send
     * @return void
     */
    private void SendMessage(KernelMessage km) {
        PCB p = getCurrentRunning();
        KernelMessage copyMessage = new KernelMessage(km);
        // copy message object
        copyMessage.senderPid = p.pid;
        // get the target Process instance
        PCB targetPCB = scheduler.processMap.get(copyMessage.targetPid);

        // if target process are not in the process Map, throw it
        if (targetPCB == null) {
            throw new RuntimeException("Target PCB not found in scheduler");
        }
        // add message to the target process's message queue
        targetPCB.messageQueue.add(copyMessage);
        System.out.println("Sending Message From " + copyMessage.senderPid + " to " + targetPCB.pid);

        // if this PCB is waiting for a message (see below), restore it to its proper runnable queue
        if (scheduler.checkWaitingProcess(copyMessage.targetPid) != null) {
            scheduler.removeWaitingProcess(copyMessage.targetPid);
            scheduler.Requeue(targetPCB);
        }

        SwitchProcess();
    }

    /**
     * Waits for a message in the current process’s inbox.
     * If none exists, moves the process to the waiting map until a message arrives.
     * @return KernelMessage the received message
     */
    private KernelMessage WaitForMessage() {
        PCB me = getCurrentRunning();

        // fast path
        if (me.messageQueue != null && !me.messageQueue.isEmpty()) {
            return me.messageQueue.remove();
        }

        // block until a message arrives
        while (true) {
            System.out.println("No Message, Gets Into Wating Queue");
            // mark as waiting and ensure it's NOT in a ready queue
            scheduler.putCurrentProcessInTheWaitingMap();
            scheduler.removeFromPriorityQueue(me);
            System.out.println("END");
            scheduler.PrintQueues();
            // yield so someone else can run and deliver the message
            SwitchProcess();

            // when scheduled again, re-check our inbox
            me = getCurrentRunning();
            if (me.messageQueue != null && !me.messageQueue.isEmpty()) {
                return me.messageQueue.remove();
            }
        }
    }

    /**
     * Looks up a process by its name.
     * @param name the process name
     * @return int the PID if found, or -1 if not found
     */
    private int GetPidByName(String name) {
        for (PCB pcb : scheduler.processMap.values()) {
            if (pcb.getName().equals(name)) {
                return pcb.pid;
            }
        }
        return -1;
    }

    private void GetMapping(int virtualPage) {
        PCB p = getCurrentRunning();
        int physicalPage = p.virtualMemoryMappingTable[virtualPage];
        if (physicalPage == -1) {
            System.out.println("Segment Fault");
            Exit();
            return;
        }
        int num = new Random().nextInt(2);
        Hardware.tlb[num][0] = virtualPage;
        Hardware.tlb[num][1] = physicalPage;
    }

    private int AllocateMemory(int size) {
        // how many page do we need
        int numberOfPages = size / sizeOfPage;
        int emptyNumberOfSlots = 0;
        PCB p = getCurrentRunning();
        for (int i = 0; i < freePage.length; i++) {
            // check for empty slot
            if (!freePage[i]) {
                emptyNumberOfSlots++;
            }
            else {
                emptyNumberOfSlots = 0;
            }
            // if there is enough space for allocation
            if (emptyNumberOfSlots == numberOfPages) {
                // find the start index of the memory
                int start = i - (numberOfPages - 1);
                // mark the allocated space in used.
                for (int j = start; j < start + numberOfPages; j++) {
                    // mark page in used
                    freePage[j] = true;
                }
                // return the beginning of this allocated memory block
                return start * sizeOfPage;
            }
        }
        // return -1 if there is no enough space for allocation
        System.out.println("Allocated Memory Not Found");
        return -1;
    }

    private boolean FreeMemory(int pointer, int size) {
        // number of page needed free
        int numberOfPages = size / sizeOfPage;
        // beginning of the memory address (virtual)
        int start = pointer / sizeOfPage;
        // mark boolean array of page as free
        for (int i = start; i < start + numberOfPages; i++) {
            freePage[i] = false;
        }


        return true;
    }

    private void FreeAllMemory(PCB currentlyRunning) {
    }

    /**
     * Cleans up all open device handles for a given process.
     * @param process the process to clean up
     */
    public void cleanUpDevice(PCB process){
        for (int i = 0; i < process.vfsID.length; i++){
            // if slot is not empty
            if(process.vfsID[i] != -1){
                vfs.Close(process.vfsID[i]);
                process.vfsID[i] = -1;
            }
        }
    }

    /**
     * Opens a file or device via the virtual file system and associates it with the process.
     * @param s file or device name
     * @return index in the process’s VFS ID array, or -1 on failure
     */
    @Override
    public int Open(String s) {
        PCB process = getCurrentRunning();
        for (int i = 0; i < process.vfsID.length; i++) {
            if (process.vfsID[i] == -1){
                //Then call vfs.open. If the result is -1, fail.
                int temp = vfs.Open(s);
                if (temp == -1){
                    return -1;  // open failed
                }
                // Otherwise, put the id from vfs into the PCB’s array and return that array index.
                else{
                    process.vfsID[i] = temp;
                    return i;       // return index in process table
                }
            }
        }
        return -1;   // no free slots
    }

    /**
     * Closes a file or device handle for the current process.
     * @param id index in the process’s VFS ID array
     */
    @Override
    public void Close(int id) {
        if (id < 0){
            throw new IllegalArgumentException("Close index is negative");
        }
        vfs.Close(getCurrentRunning().vfsID[id]);
        getCurrentRunning().vfsID[id] = -1;
    }

    /**
     * Reads bytes from a file or device.
     * @param id VFS ID index
     * @param size number of bytes to read
     * @return byte array of data read
     */
    @Override
    public byte[] Read(int id, int size) {
        if (id < 0){
            throw new IllegalArgumentException("Read index is negative");
        }
        return vfs.Read(getCurrentRunning().vfsID[id], size);
    }

    /**
     * Moves the read/write pointer within a file or device.
     * @param id VFS ID index
     * @param to new position in bytes
     */
    @Override
    public void Seek(int id, int to) {
        if (id < 0){
            throw new IllegalArgumentException("Seek index is negative");
        }
        vfs.Seek(getCurrentRunning().vfsID[id], to);
    }

    /**
     * Writes data to a file or device.
     * @param id VFS ID index
     * @param data byte array of data to write
     * @return number of bytes written
     */
    @Override
    public int Write(int id, byte[] data) {
        if (id < 0){
            throw new IllegalArgumentException("Write index is negative");
        }
        return vfs.Write(getCurrentRunning().vfsID[id], data);
    }
}