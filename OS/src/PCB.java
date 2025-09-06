public class PCB { // Process Control Block
    private static int nextPid = 1;
    public int pid;
    private OS.PriorityType priority;
    private UserlandProcess process;

    PCB(UserlandProcess up, OS.PriorityType priority) {
        this.pid = nextPid;
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
        /* calls userlandprocess’ stop. Loops with Thread.sleep() until ulp.isStopped() is true.  */
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
}
