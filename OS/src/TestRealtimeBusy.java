/**
 * TestRealtimeBusy process.
 * A realtime test program designed to run for a long time so the scheduler
 * can trigger demotion after repeated timeouts. It cooperates and sleeps
 * briefly to make the output easier to follow.
 */
public class TestRealtimeBusy extends UserlandProcess {
    @Override
    public void main() {
        // Loop long enough to see demotion happen
        for (int i = 0; i < 30; i++) { // big busy loop
            // Print progress with PID info
            System.out.println("Realtime busy still running, i=" + i +
                    " (pid=" + OS.GetPID() + ")");
            cooperate();
            try {
                Thread.sleep(50); // pause for readability
            } catch (InterruptedException e) {}
        }
        System.out.println("Realtime busy finished (pid=" + OS.GetPID() + ")");
    }
}
