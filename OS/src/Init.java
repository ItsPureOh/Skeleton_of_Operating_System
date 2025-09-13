public class Init extends UserlandProcess{
    @Override
    public void main() {
        // create these two process as test case
        //OS.CreateProcess(new HelloWorld(), OS.PriorityType.interactive);
        //OS.CreateProcess(new GoodbyeWorld(), OS.PriorityType.interactive);
        OS.CreateProcess(new IdleProcess(), OS.PriorityType.background);
        OS.CreateProcess(new TestRealtimeSleep(), OS.PriorityType.realtime);
        OS.CreateProcess(new TestInteractive(), OS.PriorityType.interactive);
        OS.CreateProcess(new TestBackground(), OS.PriorityType.background);
        OS.Exit();
    }
}
