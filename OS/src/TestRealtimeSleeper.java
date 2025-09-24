/**
 * TestRealtimeSleeper process.
 * A realtime test program that voluntarily sleeps to demonstrate
 * that cooperative behavior prevents demotion. It yields frequently
 * and sleeps long enough to avoid being preempted.
 */
public class TestRealtimeSleeper extends UserlandProcess {
    @Override
    public void main() {
        // Loop many times to observe scheduler behavior
        for (int i = 0; i < 300; i++) {
            // Print progress with PID
            System.out.println("Realtime sleeper step " + i +
                    " (pid=" + OS.GetPID() + ")");
            OS.Sleep(300);   // voluntary sleep → prevents demotion
            cooperate();     // yield explicitly too
        }
        System.out.println("Realtime sleeper finished (pid=" + OS.GetPID() + ")");
    }
}
