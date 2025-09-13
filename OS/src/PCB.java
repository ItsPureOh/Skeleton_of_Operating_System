public class PCB { // Process Control Block
    private static int nextPid = 1;
    public int pid;
    private OS.PriorityType priority;
    // User land process
    private UserlandProcess process;
    private long wakeupTime;
    public int timeoutFrequency = 0;
    public boolean timeout = false;

    PCB(UserlandProcess up, OS.PriorityType priority) {
        // assigning a new pid to a new process created
        this.pid = nextPid;
        // increment the pid for next process in the future
        nextPid++;
        this.process = up;
    }

    public String getName() {
        return null;
    }

    OS.PriorityType getPriority() {
        return priority;
    }

    public void requestStop() {
        process.requestStop();
    }

    public void stop() {
        /* calls userlandprocess’ stop. Loops with Thread.sleep() until the up.isStopped() is true.  */
        process.stop();

        while(!process.isStopped()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean isDone() { /* calls userlandprocess’ isDone() */
        return process.isDone();
    }

    void start() {
        // calls userlandprocess’ start()
        process.start();
    }

    public void setPriority(OS.PriorityType newPriority) {
        priority = newPriority;
    }

    public void setWakeupTime(long newWakeupTime) {
        this.wakeupTime = newWakeupTime;
    }

    public long getWakeupTime() {
        return wakeupTime;
    }
}
