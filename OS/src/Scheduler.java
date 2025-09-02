import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class Scheduler {
    private LinkedList<PCB> pcbs = new LinkedList<>();
    private Timer timer = new Timer();
    public PCB currentPCB = pcbs.getFirst();
    TimerTask task = new TimerTask() {
        public void run() {
            if (currentPCB != null) {
                currentPCB.requestStop();
            }
        }
    };

    public Scheduler() {
        timer.schedule(task, 250);
    }

    public int CreateProcess(UserlandProcess up, OS.PriorityType p) {
        PCB pcb = new PCB(up, p);
        pcbs.add(pcb);

        if (currentPCB == null) {
            SwitchProcess();
        }
        return pcb.pid;
    }

    public void SwitchProcess(){
        // stack is not empty and current process is not done
        if (!pcbs.isEmpty() & !currentPCB.isDone()) {
            // if current first process is done, remove it and do not add it to stack again
            while (pcbs.getFirst().isDone()){
                pcbs.removeFirst();
            }
            // remove the first pcb in the stack
            currentPCB = pcbs.removeFirst();
            // put that process at the end of the stack
            pcbs.addLast(currentPCB);
            // run the first process in the stack
            currentPCB = pcbs.getFirst();
        }
        currentPCB = null;
    }

    public boolean isCurrentPCBRunning() {
        return currentPCB != null;
    }

}
