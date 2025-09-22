public class IdleProcess extends UserlandProcess {
    @Override
    public void main() {
        while (true) {
            try {
                cooperate();
                System.out.println("Idle process Sleeping");
                Thread.sleep(50);

            } catch (Exception e) { }
        }
    }
}
