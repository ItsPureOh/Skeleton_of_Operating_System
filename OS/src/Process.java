import java.util.concurrent.Semaphore;

public abstract class Process implements Runnable{
    Semaphore semaphore = new Semaphore(1);
    Thread thread = new Thread(this);
    Boolean quantum;

    public Process() {
    }

    public void requestStop() {
        quantum = false;
    }

    public abstract void main();

    public boolean isStopped() {
        int temp = semaphore.availablePermits();
        return temp == 0;
    }

    public boolean isDone() {
        return !thread.isAlive();
    }

    public void start() {
        semaphore.release();
        thread.start();
    }

    public void stop() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        thread.interrupt();
    }

    public void run() { // This is called by the Thread - NEVER CALL THIS!!!
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        main();
    }

    public void cooperate() {
        if (quantum) {
            quantum = false;
            OS.switchProcess();
        }
    }
}
