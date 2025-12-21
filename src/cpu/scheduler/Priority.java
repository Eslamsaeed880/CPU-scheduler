package cpu.scheduler;

import java.util.*;

public class Priority implements IScheduler {

    private Map<String, Process> processes;
    private int agingInterval;
    private int contextSwitchTime;

    // Store initial burst times to calculate how long a process has executed
    private Map<String, Integer> initialBurstTimes = new HashMap<>();

    private String lastSelected = null;
    private boolean doRunProcess = true;

    @Override
    public void setProcessSet(Map<String, Process> processes) {
        this.processes = processes;
        this.initialBurstTimes.clear();
        for (Map.Entry<String, Process> entry : processes.entrySet()) {
            this.initialBurstTimes.put(entry.getKey(), entry.getValue().burstTime);
        }
        this.lastSelected = null;
        this.doRunProcess = true;
    }

    @Override
    public void setParameter(String param, Object value) {
        if (param.equals("agingInterval")) {
            this.agingInterval = (Integer)value;
        }
        else if (param.equals("contextSwitchTime")) {
            this.contextSwitchTime = (Integer)value;
        }
    }

    @Override
    public boolean runProcess() {
        return doRunProcess;
    }

    @Override
    public boolean doContextSwitch() {
        return true;
    }

    @Override
    public boolean processSetIsInQueueSet() {
        return false;
    }

    @Override
    public String scheduleNext(int time) {
        String selected = scheduleNextHelper(time);
        if (!doRunProcess) {
            doRunProcess = true;
            lastSelected = selected;
            return selected;
        }
        String potentiallyBetter = scheduleNextHelper(time + this.contextSwitchTime);
        if (selected == potentiallyBetter) {
            lastSelected = selected;
            return selected;
        }
        doRunProcess = false;
        return selected;
    }

    public String scheduleNextHelper(int time) {
        String selected = null;
        int bestPriority = Integer.MAX_VALUE;

        for (Map.Entry<String, Process> entry : processes.entrySet()) {
            String name = entry.getKey();
            Process p = entry.getValue();

            if (time < p.getArrivalTime() || p.burstTime == 0) {
                continue;
            }

            // 1. Calculate waiting time
            int executedTime = initialBurstTimes.get(name) - p.burstTime;
            int waitingTime = time - p.getArrivalTime() - executedTime;

            // 2. Calculate aged priority
            int agedPriority = Math.max(1, p.getPriority() - Math.floorDiv(waitingTime, this.agingInterval));

            // 3. Selection logic
            if (agedPriority < bestPriority) {
                bestPriority = agedPriority;
                selected = name;
            }
            // Tie-Breaking logic
            else if (agedPriority == bestPriority) {
                // Tie-Breaker: Arrival time
                if (selected != null && p.getArrivalTime() < processes.get(selected).getArrivalTime()) {
                    selected = name;
                }
            }
        }

        return selected;
    }
}