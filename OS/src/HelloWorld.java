public class HelloWorld extends UserlandProcess{
    // print String then call the kernel
    @Override
    public void main() {
        while(true){
            System.out.println("Hello World");
            cooperate();

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
