/**
 * Init process.
 * This is the first userland process started by the OS.
 * It creates several test processes with different priorities
 * to exercise the scheduler, then terminates itself.
 */
public class Init extends UserlandProcess{
    @Override
    public void main() {
        /*
        // Create three TestPriorities processes with different priorities
        OS.CreateProcess(new TestPriorities("rt"), OS.PriorityType.realtime);
        OS.CreateProcess(new TestPriorities("bg"), OS.PriorityType.background);
        OS.CreateProcess(new TestPriorities("ia"), OS.PriorityType.interactive);
         */
        /*
        // Create additional realtime processes for testing demotion/sleep behavior
        OS.CreateProcess(new TestRealtimeBusy(), OS.PriorityType.realtime);
        OS.CreateProcess(new TestRealtimeSleeper(), OS.PriorityType.realtime);
         */
        /*
        // Create Testcase for that after process awaken, it put that back to correct queue
        OS.CreateProcess(new TestRealtimeSleeper(), OS.PriorityType.realtime);
        OS.CreateProcess(new TestRealtimeSleeper(), OS.PriorityType.interactive);
        OS.CreateProcess(new TestRealtimeSleeper(), OS.PriorityType.background);
         */
        //OS.CreateProcess(new TestDeviceFile_1(), OS.PriorityType.realtime);
        OS.CreateProcess(new TestDeviceRandom_1(), OS.PriorityType.realtime);
        // Stoping the Init process
        OS.Exit();  // unschedule Init, scheduler picks the next process
    }
}
