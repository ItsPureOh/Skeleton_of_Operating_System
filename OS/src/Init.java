public class Init extends UserlandProcess{
    @Override
    public void main() {
        System.out.println("Init Process");
        OS.CreateProcess(new HelloWorld());
        OS.CreateProcess(new GoodbyeWorld());
    }
}
