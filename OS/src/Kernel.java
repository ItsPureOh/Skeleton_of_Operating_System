import java.util.HashMap;

/**
 * Kernel Class
 * -------------
 * This class represents the core of a simulated operating system kernel.
 * It extends the Process class and implements the Device interface, handling both
 * process scheduling and device management through a Virtual File System (VFS).
 *
 * The Kernel manages system calls from user processes, dispatching them
 * to the appropriate handlers (e.g., CreateProcess, Sleep, Read, Write, etc.).
 * It uses the Scheduler to manage CPU time among processes and interacts
 * with the VirtualFileSystem for I/O operations.
 */
public class Kernel extends Process implements Device  {
    private Scheduler scheduler = new Scheduler(this);
    private VirtualFileSystem vfs = new VirtualFileSystem();
    public Kernel() {
    }
    /**
     * The main kernel loop.
     * Continuously processes system calls and performs scheduling.
     * Runs indefinitely as long as the OS is active.
     */
    public void main() {
            while (true) { // kernel runs forever
                // Dispatch based on the current system call from a user process
                switch (OS.currentCall) {
                    // extract parameters and create a new process
                    case CreateProcess ->{
                            OS.retVal = CreateProcess((UserlandProcess) OS.parameters.get(0),
                                    (OS.PriorityType) OS.parameters.get(1));
                    }
                    // context switch request
                    case SwitchProcess -> {
                        SwitchProcess();
                    }
                    // Priority scheduler calls
                    case Sleep -> {
                        Sleep((int) OS.parameters.get(0));
                    }
                    case GetPID -> OS.retVal = GetPid();
                    case Exit -> Exit();

                    // Devices
                    case Open -> OS.retVal = Open((String)OS.parameters.get(0));
                    case Close -> {
                        Close((int)OS.parameters.get(0));
                        OS.retVal = 0;
                    }
                    case Read -> OS.retVal = Read((int)OS.parameters.get(0), (int)OS.parameters.get(1));
                    case Seek -> {
                        Seek((int)OS.parameters.get(0), (int)OS.parameters.get(1));
                        OS.retVal = 0;
                    }
                    case Write -> OS.retVal = Write((int)OS.parameters.get(0), (byte[]) OS.parameters.get(1));

                    /*
                    // Messages
                    case GetPIDByName ->
                    case SendMessage ->
                    case WaitForMessage ->
                    // Memory
                    case GetMapping ->
                    case AllocateMemory ->
                    case FreeMemory ->
                     */
                }
                // TODO: Now that we have done the work asked of us, start some process then go to sleep.
                // call start() on the next process to run, make sure kernel is running right now
                if (scheduler.currentRunning != null) {
                    scheduler.currentRunning.start();
                }
                else{
                    scheduler.SwitchProcess();
                }
                // Call stop() on myself(kernel), so that there is only one process is running
                this.stop();
                //System.out.println("Kernel stopped");
            }
    }

    /**
     * Invokes the scheduler to switch from the current process
     * to the next runnable process.
     * @return void
     */
    private void SwitchProcess() {
        scheduler.SwitchProcess();
    }

    // For assignment 1, you can ignore the priority. We will use that in assignment 2
    /**
     * Creates a new process, enqueues it in the scheduler, and returns its PID.
     * @param up the userland process to create
     * @param priority the priority level of the process
     * @return int the PID of the created process
     */
    private int CreateProcess(UserlandProcess up, OS.PriorityType priority) {
        scheduler.CreateProcess(up, priority);
        return scheduler.currentRunning.pid;
    }

    /**
     * Returns the PCB of the currently running process.
     * @return PCB the current running process
     */
    public PCB getCurrentRunning(){
        return scheduler.currentRunning;
    }

    /**
     * Puts the current process to sleep for the given time in milliseconds,
     * then switches to another process.
     * @param mills the sleep duration in milliseconds
     * @return void
     */
    private void Sleep(int mills) {
        scheduler.Sleep(mills);
        scheduler.SwitchProcess();
    }

    /**
     * Terminates the current process and switches to the next runnable process.
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
        cleanUpDevice(scheduler.currentRunning);
        // remove it from the hashmap in scheduler
        scheduler.removeCurrentProcessFromTheMap();
        // remove the process from the queue
        scheduler.currentRunning = null;

        //schedule should choose something else to run
        OS.switchProcess();
    }
    /**
     * Returns the PID of the currently running process.
     * @return int the PID of the current process
     */
    private int GetPid() {
        return scheduler.currentRunning.pid;
    }

    /*
    private int Open(String s) {
        return 0;
    }

    private void Close(int id) {
    }

    private byte[] Read(int id, int size) {
        return null; // change this
    }

    private void Seek(int id, int to) {
    }

    private int Write(int id, byte[] data) {
        return 0; // change this
    }
     */

    private void SendMessage(KernelMessage km) {
        KernelMessage copyMessage = new KernelMessage(km);
        copyMessage.senderPid = scheduler.currentRunning.pid;
        PCB targetPCB = scheduler.processMap.get(copyMessage.targetPid);
        // pre-check target PCB exist
        if (targetPCB == null) {
            throw new RuntimeException("Target PCB not found in scheduler");
        }
        targetPCB.messageQueue.add(copyMessage);

        // if this PCB is waiting for a message (see below), restore it to its proper runnable queue
        if (scheduler.checkWaitingProcess(km.targetPid) != null) {
            scheduler.removeWaitingProcess(km.targetPid);
            scheduler.Requeue(targetPCB);

        }
    }

    // current
    private KernelMessage WaitForMessage() {
        // if current Process has message in queue
        if (!getCurrentRunning().messageQueue.isEmpty()) {
            return getCurrentRunning().messageQueue.remove();
        }
        // de-schedule ourselves (similar to what we did for Sleep() ) and add ourselves to a new data structure to hold processes that are waiting.
        scheduler.putCurrentProcessInTheWaitingMap();

        // when process comeback from the waiting list
        if (!getCurrentRunning().messageQueue.isEmpty()) {
            return getCurrentRunning().messageQueue.remove();
        }
        else{
            throw new RuntimeException("No message queue available");
        }
    }

    private int GetPidByName(String name) {
        for (PCB pcb : scheduler.processMap.values()) {
            if (pcb.getName().equals(name)) {
                return pcb.pid;
            }
        }
        return -1;
    }

    private void GetMapping(int virtualPage) {
    }

    private int AllocateMemory(int size) {
        return 0; // change this
    }

    private boolean FreeMemory(int pointer, int size) {
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
        PCB currentProcess = getCurrentRunning();
        for (int i = 0; i < currentProcess.vfsID.length; i++) {
            if (currentProcess.vfsID[i] == -1){
                //Then call vfs.open. If the result is -1, fail.
                int temp = vfs.Open(s);
                if (temp == -1){
                    return -1;  // open failed
                }
                // Otherwise, put the id from vfs into the PCB’s array and return that array index.
                else{
                    currentProcess.vfsID[i] = temp;
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