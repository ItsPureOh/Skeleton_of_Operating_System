import java.util.concurrent.Semaphore;

public abstract class Process implements Runnable{
    //members
    Thread thread = new Thread(this);
    Semaphore semaphore = new Semaphore(0);
    Boolean quantumExpired = false;

    public Process() {
        thread.start();
    }

    public void requestStop() {
        quantumExpired = true;
    }

    public abstract void main();

    public boolean isStopped() {
        return semaphore.availablePermits() == 0;
    }

    public boolean isDone() {
        return !thread.isAlive();
    }

    public void start() {
        semaphore.release();
    }

    public void stop() {
        semaphore.acquireUninterruptibly();
    }

    public void run() { // This is called by the Thread - NEVER CALL THIS!!!
        semaphore.acquireUninterruptibly();
        main();
    }

    public void cooperate() {
        if (quantumExpired) {
            quantumExpired = false;
            OS.switchProcess();
        }
    }
}
