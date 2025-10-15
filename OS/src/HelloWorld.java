/**
 * HelloWorld process.
 * A simple test program that repeatedly prints "Hello World".
 * It demonstrates cooperative multitasking by calling cooperate()
 * and sleeps briefly to slow down the output.
 */
public class HelloWorld extends UserlandProcess{
    // Run forever
    @Override
    public void main() {
        while(true){
            System.out.println("Hello World");
            cooperate();
        }
    }
}
