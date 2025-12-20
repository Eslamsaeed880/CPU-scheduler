package cpu.scheduler;

import java.util.*;

public class Priority implements IScheduler {

    private Map<String, Process> processes;
    private int agingInterval = 1;

    // Store initial burst times to calculate how long a process has executed
    private Map<String, Integer> initialBurstTimes = new HashMap<>();

    private String lastSelected = null;

    @Override
    public void setProcessSet(Map<String, Process> processes) {
        this.processes = processes;
        initialBurstTimes.clear();
    }

    @Override
    public void onNewProcess(String process, int time) {
        if (processes.containsKey(process)) {
            // Capture the full burst time when the process arrives
            initialBurstTimes.put(process, processes.get(process).burstTime);
        }
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

            // 1. Calculate Execution Time & Wait Time
            int executedTime = 0;
            if (initialBurstTimes.containsKey(name)) {
                executedTime = initialBurstTimes.get(name) - p.burstTime;
            }
            int waitTime = time - p.getArrivalTime() - executedTime;

            // 2. Calculate Aged Priority
            int agedPriority = Math.max(1, p.getPriority() - (waitTime / this.agingInterval));

            // 3. Selection Logic
            if (agedPriority < bestPriority) {
                bestPriority = agedPriority;
                selected = name;
            }
            // Tie-Breaking Logic
            else if (agedPriority == bestPriority) {
                // Tie-Breaker A: Arrival Time
                if (selected != null && p.getArrivalTime() < processes.get(selected).getArrivalTime()) {
                    selected = name;
                }

            }
        }

        lastSelected = selected;
        return selected;
    }
}