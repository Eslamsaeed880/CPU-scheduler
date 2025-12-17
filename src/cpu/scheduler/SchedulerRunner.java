package cpu.scheduler;

import java.util.*;

public class SchedulerRunner {
    private Map<String, Process> processes;
    private int runtime;
    private int contextSwitchTime = 0;

    public SchedulerRunner(Map<String, Process> processes) {
        this.processes = processes;
        this.runtime = 0;
        for (Process proc : this.processes.values()) {
            this.runtime += proc.burstTime;
        }
    }

    public void setContextSwitchTime(int time) {
        this.contextSwitchTime = time;
    }

    List<String> run(IScheduler scheduler) {
        Map<String, Process> inQueueProcesses = new HashMap<>(this.processes.size());
        Set<String> processed = new HashSet<>(this.processes.size());
        scheduler.setProcessSet(inQueueProcesses);
        int leftoutTime = this.runtime;
        int time = 0;
        List<String> executionOrder = new ArrayList<>();
        while (leftoutTime > 0) {
            for (Map.Entry<String, Process> process : this.processes.entrySet()) {
                if (!inQueueProcesses.containsKey(process.getKey())
                    && time >= process.getValue().getArrivalTime()
                    && !processed.contains(process.getKey())) {
                    inQueueProcesses.put(process.getKey(), new Process(process.getValue()));
                    scheduler.onNewProcess(process.getKey());
                }
            }
            String nextProcess = scheduler.scheduleNext();
            if (inQueueProcesses.containsKey(nextProcess)) {
                if (executionOrder.size() > 0) {
                    String lastProcess = executionOrder.get(executionOrder.size()-1);
                    if (lastProcess != nextProcess) {
                        time += this.contextSwitchTime;
                        executionOrder.add(nextProcess);
                    }
                } else {
                    executionOrder.add(nextProcess);
                }
                if ((inQueueProcesses.get(nextProcess).burstTime -= 1) == 0) {
                    this.processes.get(nextProcess).completionTime = time + 1;
                    inQueueProcesses.remove(nextProcess);
                    processed.add(nextProcess);
                }
                leftoutTime -= 1;
            }
            time += 1;
        }
        return executionOrder;
    }
}
