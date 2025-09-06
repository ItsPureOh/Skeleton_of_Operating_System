public class Init extends UserlandProcess{
    @Override
    public void main() {
        OS.CreateProcess(new HelloWorld());
        OS.CreateProcess(new GoodbyeWorld());

        while (true){
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            cooperate();
        }
    }
}
