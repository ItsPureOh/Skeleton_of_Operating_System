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
