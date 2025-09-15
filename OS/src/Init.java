public class Init extends UserlandProcess{
    @Override
    public void main() {
        // Testing
        OS.CreateProcess(new IdleProcess(), OS.PriorityType.background);
        OS.CreateProcess(new TestRealtimeSleep(), OS.PriorityType.realtime);
        OS.CreateProcess(new TestInteractive(), OS.PriorityType.interactive);
        OS.CreateProcess(new TestBackground(), OS.PriorityType.background);

        // Stoping the Init process
        OS.Exit();
    }
}
