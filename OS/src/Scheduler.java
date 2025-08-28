import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class Scheduler {
    private LinkedList<PCB> pcbs = new LinkedList<>();
    private Timer timer = new Timer();
    public static PCB currentPCB;
    TimerTask task = new TimerTask() {
        public void run() {
            if (currentPCB != null) {
                currentPCB.requestStop();
                //System.out.println(currentPCB);
            }
        }
    };

    public Scheduler() {
        timer.schedule(task, 250);
    }

    public int CreateProcess(UserlandProcess up, OS.PriorityType p){
        PCB pcb = new PCB(up, p);
        if (currentPCB.isDone()) {
            SwitchProcess();
        }
        pcb = currentPCB;
        return pcb.pid;
    }

    public void SwitchProcess(){
        if (!pcbs.isEmpty()) {
            currentPCB = pcbs.pop();
            if (!currentPCB.isDone()){
                pcbs.addLast(currentPCB);
            }
        }
    }

}
