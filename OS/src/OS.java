import java.util.ArrayList;
import java.util.List;

/**
 * OS class.
 * The main interface between userland processes and the kernel.
 * Provides system call wrappers that trigger kernel actions such as
 * process creation, scheduling, message passing, and I/O operations.
 */
public class OS {
    private static Kernel ki; // The one and only one instance of the kernel.

    public static List<Object> parameters = new ArrayList<>();
    public static Object retVal;

    public enum CallType {SwitchProcess,SendMessage, Open, Close, Read, Seek, Write,
        GetMapping, CreateProcess, Sleep, GetPID, AllocateMemory, FreeMemory, GetPIDByName, WaitForMessage, Exit}
    public static CallType currentCall;

    /**
     * Starts the kernel and handles process synchronization.
     * If a process is currently running, it is stopped before the kernel runs.
     * Otherwise, waits until the kernel sets a return value.
     * @return void
     */
    private static void startTheKernel() {
        // start the kernel
        // waiting in the semaphore queue until previous process release


        //if the scheduler (you might need an accessor here) has a currentRunning, call stop() on it.
        // start up phase: cur == null
        PCB cur = ki.getCurrentRunning();

        // start the kernel
        // waiting in the semaphore queue until previous process release
        ki.start();
        // if currently running a process, call stop on it in order to run the kernel
        if (cur != null) {
            cur.stop();
        }
        /*
        If there is no current process running, create a loop in OS that calls Thread.sleep(10)
            until the return value is set by the kernel.
        */
        else{
            while (retVal == null) {
                System.out.println("Waiting for current running...");
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public enum PriorityType {realtime, interactive, background}

    public static void switchProcess() {
        parameters.clear();
        currentCall = CallType.SwitchProcess;
        startTheKernel();
    }

    public static void Startup(UserlandProcess init) {
        ki = new Kernel();
        CreateProcess(init, PriorityType.interactive);
        CreateProcess(new IdleProcess(), PriorityType.background);
    }

    // For assignment 1, you can ignore the priority. We will use that in assignment 2
    // this is the function that User Mode Calling to create a new process
    public static int CreateProcess(UserlandProcess up, PriorityType priority) {
        parameters.clear();
        parameters.add(up);
        parameters.add(priority);
        currentCall = CallType.CreateProcess;
        startTheKernel();
        return (int) retVal;
    }

    public static int GetPID() {
        parameters.clear();
        currentCall = CallType.GetPID;
        startTheKernel();
        return (int) retVal;
    }

    public static void Exit() {
        parameters.clear();
        currentCall = CallType.Exit;
        startTheKernel();
    }

    public static void Sleep(int mills) {
        parameters.clear();
        parameters.add(mills);
        currentCall = CallType.Sleep;
        startTheKernel();
    }

    // Devices
    public static int Open(String s) {
        parameters.clear();
        parameters.add(s);
        currentCall = CallType.Open;
        startTheKernel();
        return (int) retVal;
    }

    public static void Close(int id) {
        parameters.clear();
        parameters.add(id);
        currentCall = CallType.Close;
        startTheKernel();
    }

    public static byte[] Read(int id, int size) {
        parameters.clear();
        parameters.add(id);
        parameters.add(size);
        currentCall = CallType.Read;
        startTheKernel();
        return (byte[]) retVal;
    }

    public static void Seek(int id, int to) {
        parameters.clear();
        parameters.add(id);
        parameters.add(to);
        currentCall = CallType.Seek;
        startTheKernel();
    }

    public static int Write(int id, byte[] data) {
        parameters.clear();
        parameters.add(id);
        parameters.add(data);
        currentCall = CallType.Write;
        startTheKernel();
        return (int) retVal;
    }

    // Messages
    public static void SendMessage(KernelMessage km) {
        parameters.clear();
        parameters.add(km);
        currentCall = CallType.SendMessage;
        startTheKernel();
    }

    public static KernelMessage WaitForMessage() {
        parameters.clear();
        currentCall = CallType.WaitForMessage;
        startTheKernel();
        return (KernelMessage)retVal;
    }

    public static int GetPidByName(String name) {
        parameters.clear();
        parameters.add(name);
        currentCall = CallType.GetPIDByName;
        startTheKernel();
        return (int)retVal;
    }

    // Memory
    public static void GetMapping(int virtualPage) {
        parameters.clear();
        parameters.add(virtualPage);
        currentCall = CallType.GetMapping;
        startTheKernel();
    }

    public static int AllocateMemory(int size ) {
        if (size % 1024 != 0){
            throw new RuntimeException("AllocateMemory Error");
        }
        parameters.clear();
        parameters.add(size);
        currentCall = CallType.AllocateMemory;
        startTheKernel();
        return (int)retVal; // Change this
    }

    public static boolean FreeMemory(int pointer, int size) {
        if (size % 1024 != 0 || pointer % 1024 != 0){
            throw new RuntimeException("FreeMemory Error");
        }
        parameters.clear();
        parameters.add(pointer);
        parameters.add(size);
        currentCall = CallType.FreeMemory;
        startTheKernel();
        return (boolean)retVal;
    }
}
