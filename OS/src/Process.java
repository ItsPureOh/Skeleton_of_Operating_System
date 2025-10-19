import java.util.concurrent.Semaphore;

/**
 * Base class for all processes (kernel- and user-land).
 * Implements cooperative multitasking via a semaphore gate and a quantum flag.
 */
public abstract class Process implements Runnable{
    // set the fixed time for process to run
    private Boolean quantumExpired = false;
    private Boolean finsihed = false;
    private Semaphore semaphore = new Semaphore(0);

    public Process() {
        //members
        Thread thread = new Thread(this);
        thread.start();
    }

    /** Request the process to yield at the next cooperate() check (timer “interrupt”). */
    public void requestStop() {
        this.quantumExpired = true;
    }

    public abstract void main();

    /** @return true if this process is currently blocked (not allowed to run). */
    public boolean isStopped() {
        return this.semaphore.availablePermits() == 0;
    }

    /** @return true if the underlying thread has terminated. */
    public boolean isDone() {
        return this.finsihed;
    }

    /** Allow this process to run (release one permit so the thread can proceed). */
    public void start() {
        this.semaphore.release();
    }

    /** Block/suspend this process (consume all permits until 0). */
    public void stop() {
        this.semaphore.acquireUninterruptibly();
    }

    /**
     * Thread body: waits at the gate until start() is called, then executes main().
     * NEVER call run() directly; call start() to schedule the process.
     */
    public void run() { // called by the JVM thread system — NEVER CALL THIS YOURSELF
        semaphore.acquireUninterruptibly(); // block & waiting for permission
        main();                             // execute the userland process main()
        finsihed = true;                    // mark process as done
        OS.switchProcess();                 // ask kernel to pick next process
    }

    /**
     * Cooperative yield point. Call this regularly inside long-running loops.
     * If the scheduler/timer has marked our quantum as expired, reset the flag
     * and ask the OS to switch to another runnable process.
     */
    public void cooperate() {
        if (this.quantumExpired) {
            this.quantumExpired = false;
            OS.switchProcess();
        }
    }
}
