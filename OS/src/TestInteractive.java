/**
 * Test process to simulate an interactive task.
 * Prints progress for 8 steps, voluntarily sleeps
 * for short intervals, and cooperates with the scheduler.
 */
public class TestInteractive extends UserlandProcess{
    @Override
    public void main() {
        for (int i = 0; i < 8; i++) {
            System.out.println("Interactive process running: step " + i);
            OS.Sleep(100); // voluntarily sleeps
            cooperate();
        }
        System.out.println("Interactive process finished.");
    }
}
