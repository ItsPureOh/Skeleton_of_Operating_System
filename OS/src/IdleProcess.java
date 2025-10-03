public class IdleProcess extends UserlandProcess {
    @Override
    public void main() {
        while (true) {
            try {
                System.out.println("idle");
                cooperate();
                Thread.sleep(300);
            } catch (Exception e) { }
        }
    }
}
