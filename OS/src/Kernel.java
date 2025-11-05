import java.util.Arrays;
import java.util.Random;

/**
 * Kernel Class
 * -------------
 * Core of the simulated operating system.
 * Handles process scheduling, system calls, and message passing.
 * Works closely with the Scheduler and Virtual File System (VFS).
 */
public class Kernel extends Process implements Device  {
    // Manages process queues and performs context switching between processes
    private Scheduler scheduler = new Scheduler(this);

    // Simulated virtual file system used for handling file I/O operations
    private VirtualFileSystem vfs = new VirtualFileSystem();

    // Tracks the allocation status of physical memory page, true = allocated, false = free
    private boolean[] freePage = new boolean[1024];

    // Defines the size (in bytes) of a single memory page
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

                // Memory
                case GetMapping -> GetMapping((int)OS.parameters.get(0));
                case AllocateMemory -> OS.retVal = AllocateMemory((int)OS.parameters.get(0));
                case FreeMemory -> OS.retVal = FreeMemory((int)OS.parameters.get(0), (int)OS.parameters.get(1));
            }
            // TODO: Now that we have done the work asked of us, start some process then go to sleep.
            // Ensure something is runnable before proceeding
            while (scheduler.currentRunning == null) {
                SwitchProcess();
            }

            // Start the chosen process
            GetCurrentRunningProcess().start();

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
        ClearTLB(Hardware.tlb);
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
    public PCB GetCurrentRunningProcess(){
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
        CleanUpDevice(GetCurrentRunningProcess());
        // remove it from the hashmap in scheduler
        scheduler.RemoveCurrentProcessFromTheMap();
        // remove the queue message from the process list
        scheduler.ClearMessageInQueue();
        // remove the process from the waiting message map
        scheduler.RemoveCurrentProcessFromWaitingProcessMap();
        // remove the process from the queue
        scheduler.currentRunning = null;
        // free the memory
        FreeAllMemory(GetCurrentRunningProcess());

        //schedule should choose something else to run
        SwitchProcess();
    }

    /**
     * Returns the PID of the currently running process.
     * @return int the PID of the current process
     */
    private int GetPid() {
        return GetCurrentRunningProcess().pid;
    }

    /**
     * Sends a message from the current process to another process.
     * Adds the message to the target process’s queue and requeues it if waiting.
     * @param km the message to send
     * @return void
     */
    private void SendMessage(KernelMessage km) {
        PCB p = GetCurrentRunningProcess();
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
        if (scheduler.CheckWaitingProcess(copyMessage.targetPid) != null) {
            scheduler.RemoveWaitingProcess(copyMessage.targetPid);
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
        PCB me = GetCurrentRunningProcess();

        // fast path
        if (me.messageQueue != null && !me.messageQueue.isEmpty()) {
            return me.messageQueue.remove();
        }

        // block until a message arrives
        while (true) {
            System.out.println("No Message, Gets Into Wating Queue");
            // mark as waiting and ensure it's NOT in a ready queue
            scheduler.PutCurrentProcessInTheWaitingMap();
            scheduler.RemoveFromPriorityQueue(me);
            System.out.println("END");
            scheduler.PrintQueues();
            // yield so someone else can run and deliver the message
            SwitchProcess();

            // when scheduled again, re-check our inbox
            me = GetCurrentRunningProcess();
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

    /**
     * Handles a TLB miss by retrieving the mapping between a virtual page
     * and its corresponding physical page from the current process's
     * page table (virtualMemoryMappingTable).
     *
     * If the requested virtual page is invalid (unmapped), this function
     * reports a segmentation fault and terminates the process.
     *
     * Once a valid mapping is found, a random TLB slot is chosen and updated
     * with the new virtual-to-physical page mapping.
     *
     * @param virtualPage the virtual page number that caused the TLB miss
     */
    private void GetMapping(int virtualPage) {
        PCB p = GetCurrentRunningProcess();
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

    /**
     * Allocates a block of virtual memory for the currently running process.
     * Determines how many pages are required, finds a suitable range of
     * consecutive virtual pages, and maps them to available physical pages.
     *
     * The function updates both the process's virtual memory mapping table
     * and the global physical page tracking array.
     *
     * @param size the requested allocation size in bytes
     * @return the starting virtual address of the allocated block,
     *         or -1 if allocation fails
     */
    private int AllocateMemory(int size) {
        int numberOfPages = size / sizeOfPage;
        int result, end;
        PCB p = GetCurrentRunningProcess();

        // terminate if physical page not enough
        if (AvailablePhysicalPages(freePage) < numberOfPages) {
            throw new RuntimeException("Free physical page is too small");
        }

        // find the right hole in the virtual memory mapping table
        // record the start of the virtual memory index in the array (virtualMemoryMappingTable)
        int start = AllocateMemoryInVirtualPage(p, numberOfPages);
        if (start == -1) {return -1;}

        result = start;
        end = start + numberOfPages;

        for (int i = 0; i < freePage.length; i++){
            if (!freePage[i]){
                // assigning the virtual memory index to that physical memory page (changing the boolean array value)
                p.virtualMemoryMappingTable[start] = i;
                freePage[i] = true;
                start++;
            }

            // if all virtual page assigned a physical page
            if (start == end){
                return result * sizeOfPage;
            }
        }

        System.out.println("Allocated Memory Not Found");
        return -1;
    }

    /**
     * Frees a block of memory previously allocated to the current process.
     * Releases both the virtual-to-physical mappings in the process's page table
     * and the corresponding physical pages in the system.
     *
     * @param pointer the starting virtual address of the block to free
     * @param size    the size of the block to free in bytes
     * @return true if the operation completes successfully
     */
    private boolean FreeMemory(int pointer, int size) {
        // number of page needed free
        int numberOfPages = size / sizeOfPage;
        // beginning of the memory address (virtual)
        int start = pointer / sizeOfPage;
        // get process
        PCB p = GetCurrentRunningProcess();
        //
        int physicalPage;

        // free the virtual table
        // free the boolean array of physical memory address
        for (int i = 0; i < numberOfPages; i++) {
            physicalPage = p.virtualMemoryMappingTable[start + i];
            p.virtualMemoryMappingTable[start + i] = -1;
            freePage[physicalPage] = false;
        }
        return true;
    }

    /**
     * Frees all memory associated with the given process.
     * Resets both the process's virtual memory mapping table and the global
     * physical memory tracking array, effectively releasing all memory pages.
     *
     * @param currentlyRunning the PCB of the process whose memory should be freed
     */
    private void FreeAllMemory(PCB currentlyRunning) {
        if (currentlyRunning != null) {
            Arrays.fill(currentlyRunning.virtualMemoryMappingTable, -1);
            Arrays.fill(freePage, false);
        }
    }

    /**
     * Cleans up all open device handles for a given process.
     * @param process the process to clean up
     */
    public void CleanUpDevice(PCB process){
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
        PCB process = GetCurrentRunningProcess();
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
        vfs.Close(GetCurrentRunningProcess().vfsID[id]);
        GetCurrentRunningProcess().vfsID[id] = -1;
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
        return vfs.Read(GetCurrentRunningProcess().vfsID[id], size);
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
        vfs.Seek(GetCurrentRunningProcess().vfsID[id], to);
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
        return vfs.Write(GetCurrentRunningProcess().vfsID[id], data);
    }

    /**
     * Finds a contiguous sequence of empty virtual pages in the given process's
     * virtual memory mapping table that is large enough to satisfy a memory allocation request.
     *
     * The function scans the process's page table, counting consecutive entries
     * that are marked as -1 (indicating unused pages). Once the required number
     * of pages is found, it returns the starting index of that range.
     *
     * @param currentlyRunning the PCB of the process requesting memory
     * @param numberOfPages the number of contiguous virtual pages needed
     * @return the starting index of the free range, or -1 if no suitable range exists
     */
    private int AllocateMemoryInVirtualPage(PCB currentlyRunning, int numberOfPages) {
        int emptyPage = 0;
        for (int i = 0; i < currentlyRunning.virtualMemoryMappingTable.length; i++) {
            if (currentlyRunning.virtualMemoryMappingTable[i] == -1) {
                emptyPage++;
            }
            else{
                emptyPage = 0;
            }
            if (emptyPage == numberOfPages) {
                return i - (numberOfPages - 1);
            }
        }
        return -1;
    }

    /**
     * Clears all entries in the provided TLB array by setting
     * every value to zero.
     *
     * This is typically used during system initialization or
     * when switching between processes to prevent stale address mappings.
     *
     * @param tlb the Translation Lookaside Buffer to clear
     */
    private void ClearTLB(int[][] tlb){
        for (int i = 0; i < tlb.length; i++) {
            for (int j = 0; j < tlb[i].length; j++) {
                tlb[i][j] = 0;
            }
        }
    }

    /**
     * Counts the number of available (unused) physical pages in the system.
     * Each entry in the freePage array represents one physical page, where
     * false means the page is free and true means it is currently allocated.
     *
     * @param freePage the boolean array tracking physical page availability
     * @return the total number of free physical pages
     */
    public int AvailablePhysicalPages(boolean[] freePage) {
        int count = 0;
        for (boolean b : freePage) {
            if (!b) count++;
        }
        return count;
    }
}