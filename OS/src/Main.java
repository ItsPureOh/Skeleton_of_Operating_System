public class Main {
    static UserlandProcess init = new UserlandProcess() {
        @Override
        public void main() {
            HelloWorld helloWorld = new HelloWorld();
            GoodbyeWorld goodbyeWorld = new GoodbyeWorld();
            helloWorld.main();
            goodbyeWorld.main();
        }
    };

    public static void main(String[] args) {
        OS.Startup(init);
    }
}
