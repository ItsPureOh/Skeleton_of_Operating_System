/**
 * GoodbyeWorld process.
 * A simple test program that repeatedly prints "Goodbye world".
 * It cooperates with the scheduler (via cooperate())
 * and sleeps briefly between prints to simulate work and
 * make output easier to follow.
 */
public class GoodbyeWorld extends UserlandProcess{
    @Override
    public void main() {
        // Run forever
        while(true){
            System.out.println("Goodbye world");    // print message
            cooperate();
            try {
                Thread.sleep(50);       // pause to slow down output
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
