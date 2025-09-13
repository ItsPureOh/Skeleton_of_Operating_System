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
