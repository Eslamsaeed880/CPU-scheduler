package cpu.scheduler;

import java.util.Map;

public class ShortestJobFirst implements IScheduler {
    private Map<String, Process> processes;

    @Override
    public void setProcessSet(Map<String, Process> processes) {
        this.processes = processes;
    }

    @Override
    public String /* Process name */ scheduleNext(int time) { // Runs for 1s.
        int minBurstTime = Integer.MAX_VALUE;
        String minBurstTimeProcess = null;

        for (Map.Entry<String, Process> entry : this.processes.entrySet()) {
            Process p = entry.getValue();

            if (p.burstTime < minBurstTime) {
                // Found a strictly shorter job
                minBurstTime = p.burstTime;
                minBurstTimeProcess = entry.getKey();
            }
            else if (p.burstTime == minBurstTime
                     && p.getArrivalTime() < this.processes.get(minBurstTimeProcess).getArrivalTime()) {
                // Tie-breaker: Pick the one that arrived earliest (FCFS)
                minBurstTimeProcess = entry.getKey();
            }
        }

        return minBurstTimeProcess;
    }
}
