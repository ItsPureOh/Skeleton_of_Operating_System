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

        /*
        // Tests For Device Assignment
        // Multiple Processes access same device, appending new text to the same files open below
        OS.CreateProcess(new TestDevice_MultipleProcess(), OS.PriorityType.realtime);
        // Check all functionality of the device with the name
        OS.CreateProcess(new TestDeviceFile_1(), OS.PriorityType.realtime);
        // Check all functionality of the device with the random seeds
        OS.CreateProcess(new TestDeviceRandom_1(), OS.PriorityType.realtime);

         */


        //Test for Message Assignment
        OS.CreateProcess(new TestMessagePing(), OS.PriorityType.realtime);
        OS.CreateProcess(new TestMessagePong(), OS.PriorityType.realtime);
        OS.CreateProcess(new HelloWorld(), OS.PriorityType.realtime);
        OS.CreateProcess(new GoodbyeWorld(), OS.PriorityType.realtime);

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Stoping the Init process
        OS.Exit();  // unscheduled Init, scheduler picks the next process
    }
}
