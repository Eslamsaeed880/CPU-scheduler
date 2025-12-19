package cpu.scheduler;

import java.util.*;

public class Priority implements IScheduler {

    private Map<String, Process> processes;
    private int agingInterval = 1; // Default to avoid division by zero

    // Tracks when a process arrived to help calculate "wait time" roughly
    private Map<String, Integer> lastExecutedTime = new HashMap<>();
    private String lastSelected = null;

    @Override
    public void setProcessSet(Map<String, Process> processes) {
        this.processes = processes;
        lastExecutedTime.clear();
    }

    @Override
    public void onNewProcess(String process, int time) {
        // We initialize lastExecutedTime with arrival time
        lastExecutedTime.put(process, time);
    }

    @Override
    public void setParameter(String param, Object value) {
        if (param.equals("agingInterval")) {
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


            int waitTime = time - p.getArrivalTime();

            // Calculate Aged Priority (Lower value is better)
            int agedPriority = Math.max(1, p.getPriority() - (waitTime / this.agingInterval));

            // 1. Found a strictly better priority
            if (agedPriority < bestPriority) {
                bestPriority = agedPriority;
                selected = name;
            }
            // 2. Priority is Equal: Check Tie-Breakers
            else if (agedPriority == bestPriority) {
                // Tie-Breaker A: Arrival Time (Earlier Arrival wins)
                // We check this BEFORE checking if it's currently running,
                // to allow the "Preemption" scenario you requested.
                if (selected != null && p.getArrivalTime() < processes.get(selected).getArrivalTime()) {
                    selected = name;
                }
                // Tie-Breaker B: If arrival times are also equal (rare), prefer the one running
                else if (selected != null && p.getArrivalTime() == processes.get(selected).getArrivalTime()) {
                    if (name.equals(lastSelected)) {
                        selected = name;
                    }
                }
            }
        }

        lastSelected = selected;
        return selected;
    }
}