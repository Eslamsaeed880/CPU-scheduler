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
        Map<String, Process> allProcesses = new HashMap<>(this.processes.size());
        for (Map.Entry<String, Process> entry : this.processes.entrySet()) {
            allProcesses.put(entry.getKey(), new Process(entry.getValue()));
        }
        Map<String, Process> inQueueProcesses = new HashMap<>(this.processes.size());
        if (scheduler.processSetIsInQueueSet()) {
            scheduler.setProcessSet(inQueueProcesses);
        } else {
            scheduler.setProcessSet(allProcesses);
        }
        int leftoutTime = this.runtime;
        int time = 0;
        List<String> executionOrder = new ArrayList<>();
        scheduler.setParameter("contextSwitchTime", this.contextSwitchTime);
        while (leftoutTime > 0) {
            for (Map.Entry<String, Process> process : this.processes.entrySet()) {
                if (!inQueueProcesses.containsKey(process.getKey())
                    && time >= process.getValue().getArrivalTime()
                    && allProcesses.get(process.getKey()).burstTime > 0 ) {
                    inQueueProcesses.put(process.getKey(), allProcesses.get(process.getKey()));
                    scheduler.onNewProcess(process.getKey(), time);
                }
            }
            String nextProcess = scheduler.scheduleNext(time);
            if (inQueueProcesses.containsKey(nextProcess)) {
                if (executionOrder.size() > 0) {
                    String lastProcess = executionOrder.get(executionOrder.size()-1);
                    if (lastProcess != nextProcess) {
                        if (scheduler.doContextSwitch()) {
                            time += this.contextSwitchTime;
                        }
                        executionOrder.add(nextProcess);
                    }
                } else {
                    executionOrder.add(nextProcess);
                }
                if (scheduler.runProcess()) {
                    if ((inQueueProcesses.get(nextProcess).burstTime -= 1) == 0) {
                        this.processes.get(nextProcess).completionTime = time + 1;
                        inQueueProcesses.remove(nextProcess);
                    }
                    leftoutTime -= 1;
                    time += 1;
                }
            }
        }
        return executionOrder;
    }
}
