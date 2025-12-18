package cpu.scheduler;

import java.util.*;

public class AG implements IScheduler {
    private Map<String, Process> processes;
    public Map<String, ArrayList<Integer>> quantumHistory = new HashMap();

    @Override
    public void setProcessSet(Map<String, Process> processes) {
        this.processes = processes;
        this.quantumHistory.clear();
    }

    @Override
    public void onNewProcess(String process, int time) {
    }

    @Override
    public String /* Process name */ scheduleNext(int time) { // Runs for 1s.
        return "";
    }
}
