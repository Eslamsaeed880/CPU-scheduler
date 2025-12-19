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
        boolean isPriority = scheduler instanceof Priority;

        Map<String, Process> inQueueProcesses = new HashMap<>(this.processes.size());
        Set<String> processed = new HashSet<>(this.processes.size());
        scheduler.setProcessSet(inQueueProcesses);

        int leftoutTime = this.runtime;
        int time = 0;
        List<String> executionOrder = new ArrayList<>();

        while (leftoutTime > 0) {
            // 1. Check for new arrivals
            for (Map.Entry<String, Process> process : this.processes.entrySet()) {
                if (!inQueueProcesses.containsKey(process.getKey())
                        && time >= process.getValue().getArrivalTime()
                        && !processed.contains(process.getKey())) {
                    inQueueProcesses.put(process.getKey(), new Process(process.getValue()));
                    scheduler.onNewProcess(process.getKey(), time);
                }
            }

            // 2. Schedule next process
            String nextProcess = scheduler.scheduleNext(time);

            if (inQueueProcesses.containsKey(nextProcess)) {

                // --- LOGGING / CONTEXT SWITCH LOGIC ---
                if (executionOrder.isEmpty()) {
                    // Always add the very first process
                    executionOrder.add(nextProcess);
                } else {
                    String lastProcess = executionOrder.get(executionOrder.size() - 1);

                    // Only handle switching if the process is DIFFERENT
                    if (!lastProcess.equals(nextProcess)) {

                        if (scheduler.doContextSwitch()) {
                            time += this.contextSwitchTime;

                            // Special Priority Logic: Check if aging occurred during switch time
                            if (isPriority) {
                                String potentiallyBetter = scheduler.scheduleNext(time);

                                if (!potentiallyBetter.equals(nextProcess)) {
                                    // Add the process we skipped to the log
                                    executionOrder.add(nextProcess);

                                    // Add another context switch penalty
                                    time += this.contextSwitchTime;

                                    // Switch target to the new winner
                                    nextProcess = potentiallyBetter;
                                }
                            }
                        }

                        // Add the final selected process to the Gantt chart
                        executionOrder.add(nextProcess);
                    }
                }
                // --------------------------------------

                // 3. Execution (Decrement Burst)
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