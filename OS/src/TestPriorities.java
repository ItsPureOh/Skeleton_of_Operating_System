/**
 * Test process to simulate process work.
 */
public class TestPriorities extends UserlandProcess{
    private final String label;

    public TestPriorities(String label) {
        this.label = label;
    }

    @Override
    public void main() {
        for (int i = 0; i < 20; i++) {
            System.out.println(label + " running step " + i + " (pid=" + OS.GetPID() + ")");
            cooperate(); // yield frequently
            try {
                Thread.sleep(50); // make output easier to follow
            } catch (InterruptedException e) {}
        }
        System.out.println(label + " finished (pid=" + OS.GetPID() + ")");
    }
}
