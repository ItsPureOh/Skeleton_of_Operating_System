/**
 * TestPriorities process.
 * Simulates a process with a given label that runs 20 steps,
 * yielding frequently to exercise the scheduler.
 */
public class TestPriorities extends UserlandProcess{
    private final String label;         // label to identify the process in output
    public TestPriorities(String label) {
        this.label = label;
    }
    @Override
    public void main() {
        for (int i = 0; i < 20; i++) {
            cooperate(); // yield
            try {
                Thread.sleep(50); // make output easier to follow
            } catch (InterruptedException e) {}
        }
        // Done
        System.out.println(label + " finished (pid=" + OS.GetPID() + ")");
    }
}
