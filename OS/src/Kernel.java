public class Kernel extends Process  {
    Scheduler scheduler = new Scheduler();
    public Kernel() {
    }

    @Override
    public void main() {
            while (true) { // Warning on infinite loop is OK...
                switch (OS.currentCall) { // get a job from OS, do it
                    case CreateProcess ->  // Note how we get parameters from OS and set the return value
                            OS.retVal = CreateProcess((UserlandProcess) OS.parameters.get(0),
                                    (OS.PriorityType) OS.parameters.get(1));
                    case SwitchProcess -> SwitchProcess();

                    // Priority Schduler
                    case Sleep -> Sleep((int) OS.parameters.get(0));
                    case GetPID -> OS.retVal = GetPid();
                    case Exit -> Exit();
                    /*
                    // Devices
                    case Open ->
                    case Close ->
                    case Read ->
                    case Seek ->
                    case Write ->
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

    private void SwitchProcess() {
        scheduler.SwitchProcess();
    }

    // For assignment 1, you can ignore the priority. We will use that in assignment 2
    // privileged implementation that build PCB, puts it on the scheduler's queue and return PID
    // enqueue the process in scheduler
    private int CreateProcess(UserlandProcess up, OS.PriorityType priority) {
        scheduler.CreateProcess(up, priority);
        return scheduler.currentRunning.pid;
    }

    // Accessor get current running process
    public PCB getCurrentRunning(){
        return scheduler.currentRunning;
    }

    private void Sleep(int mills) {
    }

    private void Exit() {
    }

    private int GetPid() {
        return 0; // change this
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