package cpu.scheduler;

import java.util.HashMap;
import java.util.Map;

public class Priority implements IScheduler {

    private Map<String, Process> processes;
    private int agingInterval;
    private Map<String, Integer> waitingTime = new HashMap<>();

    // Currently running process
    private String currentProcess = null;

    private int current_time;

    @Override
    public void setProcessSet(Map<String, Process> processes) {
        this.processes = processes;
        waitingTime.clear();
        currentProcess = null;
        current_time = 0;

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

        if (processes == null || processes.isEmpty()) {
            return null;
        }

        current_time++;

        // 1- Aging: increase waiting time for all processes
        for (String name : processes.keySet()) {
            Process p = processes.get(name);
            if (p.getArrivalTime() > current_time || name.equals(currentProcess)) continue;
            int wait = waitingTime.computeIfAbsent(name, k -> 0) + 1;
            waitingTime.put(name, wait);
            // Apply aging
            if (agingInterval > 0 && wait % agingInterval == 0) {
                try {
                    var field = Process.class.getDeclaredField("priority");
                    field.setAccessible(true);
                    int newPriority = Math.max(0, p.getPriority() - 1);
                    field.setInt(p, newPriority);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace(); // or ignore if you prefer
                }
            }

        }

        // 2- Select process with highest priority (lowest value)
        String selected = null;
        int bestPriority = Integer.MAX_VALUE;
        int earliestArrival = Integer.MAX_VALUE;

        for (Map.Entry<String, Process> entry : processes.entrySet()) {
            Process p = entry.getValue();
            if (p.getArrivalTime() > current_time) continue;
            if (p.getPriority() < bestPriority ||
                    (p.getPriority() == bestPriority && p.getArrivalTime() < earliestArrival)) {
                bestPriority = p.getPriority();
                earliestArrival = p.getArrivalTime();
                selected = entry.getKey();
            }
        }

        if (selected == null) return null;

        // 4- Update current process and reset its waiting time
        currentProcess = selected;
        waitingTime.put(selected, 0);

        return selected;
    }

}
