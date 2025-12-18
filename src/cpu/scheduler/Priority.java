package cpu.scheduler;

import java.util.*;

public class Priority implements IScheduler {

    private Map<String, Process> processes;
    private int agingInterval;

    private Map<String, Integer> lastExecutedTime = new HashMap<>();
    private String lastSelected = null;

    @Override
    public void setProcessSet(Map<String, Process> processes) {
        this.processes = processes;
        lastExecutedTime.clear();
    }

    @Override
    public void onNewProcess(String process, int time) {
        lastExecutedTime.put(process, time);
    }

    @Override
    public void setParameter(String param, Object value) {
        if (param == "agingInterval") {
            this.agingInterval = (Integer)value;
        }
    }

    @Override
    public boolean doContextSwitch() {
        return false;
    }

    @Override
    public String /* Process name */ scheduleNext(int time) { // Runs for 1s.
        String selected = null;
        int bestPriority = Integer.MAX_VALUE;

        if (lastSelected != null) {
            lastExecutedTime.put(lastSelected, time);
        }

        for (Map.Entry<String, Process> entry : processes.entrySet()) {
            Process p = entry.getValue();
            int priority = p.getPriority() - ((time - lastExecutedTime.get(entry.getKey())) / this.agingInterval);
            if (priority < bestPriority) {
                bestPriority = priority;
                selected = entry.getKey();
            } else if (priority == bestPriority && p.burstTime < this.processes.get(selected).burstTime) {
                selected = entry.getKey();
            }
        }

        lastSelected = selected;
        return selected;
    }
}
