import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class Scheduler {
    private LinkedList<PCB> processQueue = new LinkedList<>();
    private Timer timer = new Timer(true);
    public PCB currentRunning;

    public Scheduler() {
        new TimerTask() {
            public void run() {

            }
        }
    }

    public int CreateProcess(UserlandProcess up, OS.PriorityType p){
        return 0;
    }

    public void SwitchProcess(){

    }

}
