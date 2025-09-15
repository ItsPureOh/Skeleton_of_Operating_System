/**
 * Test process to simulate a realtime task that repeatedly sleeps.
 * Runs 5 iterations, each time printing a message, then sleeping
 * for 500ms before yielding back to the scheduler.
 */
public class TestRealtimeSleep extends UserlandProcess {
    @Override
    public void main() {
        for (int i = 0; i < 5; i++) {
            System.out.println("Realtime sleeper iteration " + i + "Process Start");
            OS.Sleep(500); // voluntarily sleeps
            cooperate();
        }
        System.out.println("Realtime sleeper finished.");
    }
}
