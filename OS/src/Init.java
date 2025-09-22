public class Init extends UserlandProcess{
    @Override
    public void main() {
        // Testing
        //OS.CreateProcess(new IdleProcess(), OS.PriorityType.background);
        OS.CreateProcess(new TestPriorities(), OS.PriorityType.background);
        OS.CreateProcess(new TestPriorities(), OS.PriorityType.interactive);
        OS.CreateProcess(new TestPriorities(), OS.PriorityType.realtime);

        // Stoping the Init process
        OS.Exit();
    }
}
