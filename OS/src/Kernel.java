public class Kernel extends Process  {
    private Scheduler scheduler = new Scheduler();
    public Kernel() {
    }

    @Override
    public void main() {
            while (true) { // kernel runs forever
                // Dispatch based on the current system call from a user process
                switch (OS.currentCall) {
                    // extract parameters and create a new process
                    case CreateProcess ->
                            OS.retVal = CreateProcess((UserlandProcess) OS.parameters.get(0),
                                    (OS.PriorityType) OS.parameters.get(1));
                    // context switch request
                    case SwitchProcess -> SwitchProcess();

                    // Priority scheduler calls
                    case Sleep -> Sleep((int) OS.parameters.get(0));
                    case GetPID -> OS.retVal = GetPid();
                    case Exit -> Exit();

                    // Devices
                    case Open ->
                    case Close ->
                    case Read ->
                    case Seek ->
                    case Write ->
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
                    System.out.println("Kernel is not running");
                }
                // Call stop() on myself(kernel), so that there is only one process is running
                this.stop();
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
        // unscheduled the current process so that it never gets run again
        if (scheduler.currentRunning != null) {
            System.out.println("The Process is Terminated: " + scheduler.currentRunning.pid);
            scheduler.currentRunning = null;
        }
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

    private int Open(String s) {
        return 0; // change this
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

    private void SendMessage(/*KernelMessage km*/) {
    }

    private KernelMessage WaitForMessage() {
        return null;
    }

    private int GetPidByName(String name) {
        return 0; // change this
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

}