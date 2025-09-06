import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class Scheduler {
    private LinkedList<PCB> processQueue = new LinkedList<>();
    private Timer timer = new Timer(true);
    public PCB currentRunning;

    public Scheduler() {
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (currentRunning != null) {
                    currentRunning.requestStop();
                }
            }
        }, 250, 250);
    }

    public int CreateProcess(UserlandProcess up, OS.PriorityType p){
        PCB pcb = new PCB(up, p);
        processQueue.addLast(pcb);
        // if nothing else is running, call switchProcess
        if (currentRunning == null) {
            SwitchProcess();
        }
        return pcb.pid;
    }

    public void SwitchProcess(){
        PCB cur = currentRunning;
        currentRunning = null;

        // Nothing is currently running (we are at startup). We just don’t put null on our list.
        // The user process is done() – we just don’t add it to the list.
        if (cur != null && !cur.isDone()){
            processQueue.addLast(cur);
        }
        while (!processQueue.isEmpty()){
            PCB next = processQueue.removeFirst();
            if (!next.isDone()){
                currentRunning = next;
                break;
            }
        }
    }
}
