public class TestBackground extends UserlandProcess{
    @Override
    public void main() {
        for (int i = 0; i < 6; i++) {
            System.out.println("Background process running: step " + i);
            OS.Sleep(300); // simulate slower work
            cooperate();
        }
        System.out.println("Background process finished.");
    }
}
