package cpu.scheduler;

import java.util.*;

public class Priority implements IScheduler {

    private Map<String, Process> processes;
    private int agingInterval;

    // Tracks how long each process has been waiting
    private Map<String, Integer> waitingTime = new HashMap<>();

    @Override
    public void setProcessSet(Map<String, Process> processes) {
        this.processes = processes;
        waitingTime.clear();

        // Initialize waiting times
        for (String name : processes.keySet()) {
            waitingTime.put(name, 0);
        }
    }

    @Override
    public void configure(int rrQuantum, int agingInterval) {
        this.agingInterval = agingInterval;
    }

    @Override
    public String scheduleNext() {

        if (processes.isEmpty()) {
            return null;
        }

        // Find process with highest priority (lowest value)
        String selected = null;
        int bestPriority = Integer.MAX_VALUE;

        for (Map.Entry<String, Process> entry : processes.entrySet()) {
            Process p = entry.getValue();
            if (p.getPriority() < bestPriority) {
                bestPriority = p.getPriority();
                selected = entry.getKey();
            }
        }

        // Aging: increase waiting time for all except selected
        for (String name : processes.keySet()) {
            if (!name.equals(selected)) {
                int wait = waitingTime.getOrDefault(name, 0) + 1;
                waitingTime.put(name, wait);

                // Apply aging
                if (wait % agingInterval == 0) {
                    // Improve priority
                    Process p = processes.get(name);
                    try {
                        var field = Process.class.getDeclaredField("priority");
                        field.setAccessible(true);
                        // Prevent priority going negative
                        int newPriority = Math.max(0, p.getPriority() - 1);
                        field.setInt(p, newPriority);
                    } catch (Exception ignored) {}
                }
            } else {
                // Reset waiting time for running process
                waitingTime.put(name, 0);
            }
        }

        return selected;
    }
}
