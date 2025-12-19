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
        return true;
    }

    @Override
    public String scheduleNext(int time) {
        String selected = null;
        int bestPriority = Integer.MAX_VALUE;

        for (Map.Entry<String, Process> entry : processes.entrySet()) {
            String name = entry.getKey();
            Process p = entry.getValue();

            // Calculate priority based on arrival time to represent total time in system
            // OR calculate wait time as (current time - last time it finished a burst)
            int waitTime = time - lastExecutedTime.get(name);
            int agedPriority = p.getPriority() - (waitTime / this.agingInterval);

            if (agedPriority < bestPriority) {
                bestPriority = agedPriority;
                selected = name;
            }
            // Tie-breaker 1: If priorities are equal, prefer the one ALREADY running (prevents thrashing)
            else if (agedPriority == bestPriority && name.equals(lastSelected)) {
                selected = name;
            }
            // Tie-breaker 2: If neither is running, prefer earlier arrival
            else if (agedPriority == bestPriority) {
                if (p.getArrivalTime() < processes.get(selected).getArrivalTime()) {
                    selected = name;
                }
            }

        }

        lastSelected = selected;
        return selected;
    }
}
