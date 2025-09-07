public class GoodbyeWorld extends UserlandProcess{
    //Test Program
    @Override
    public void main() {
        // print String then call the kernel
        while(true){
            System.out.println("Goodbye world");
            cooperate();

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
