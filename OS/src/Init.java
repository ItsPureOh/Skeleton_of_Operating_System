public class Init extends UserlandProcess{
    @Override
    public void main() {
        // create these two process as test case
        OS.CreateProcess(new HelloWorld(), OS.PriorityType.interactive);
        OS.CreateProcess(new GoodbyeWorld(), OS.PriorityType.interactive);

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
