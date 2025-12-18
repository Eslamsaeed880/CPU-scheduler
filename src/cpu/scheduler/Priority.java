package cpu.scheduler;

import java.util.HashMap;
import java.util.Map;

public class Priority implements IScheduler {

    private Map<String, Process> processes;
    private int agingInterval;

    // How long each process has been waiting (in CPU ticks)
    private Map<String, Integer> waitingTime = new HashMap<>();

    // Dynamic priority used by the scheduler
    // Lower value = higher priority
    private Map<String, Integer> effectivePriority = new HashMap<>();

    @Override
    public void setProcessSet(Map<String, Process> processes) {
        this.processes = processes;
        waitingTime.clear();
        effectivePriority.clear();

        // Initialize state for all currently available processes
        for (Map.Entry<String, Process> e : processes.entrySet()) {
            waitingTime.put(e.getKey(), 0);
            effectivePriority.put(e.getKey(), e.getValue().getPriority());
        }
    }

    @Override
    public void configure(int rrQuantum, int agingInterval) {
        this.agingInterval = agingInterval;
    }

    @Override
    public void onNewProcess(String name) {
        // Initialize newly arrived process
        if (!waitingTime.containsKey(name) && processes.containsKey(name)) {
            waitingTime.put(name, 0);
            effectivePriority.put(name, processes.get(name).getPriority());
        }
    }

    @Override
    public String scheduleNext() {

        if (processes == null || processes.isEmpty()) {
            return null;
        }

        // 1) Select process with best EFFECTIVE priority
        String selected = null;
        int bestPriority = Integer.MAX_VALUE;

        for (Map.Entry<String, Process> entry : processes.entrySet()) {
            String name = entry.getKey();
            Process p = entry.getValue();
            int pr = effectivePriority.get(name);

            if (pr < bestPriority) {
                bestPriority = pr;
                selected = name;
            }
        }

        if (selected == null) {
            return null;
        }

        // 2) Aging: everyone except the selected process waited 1 tick
        for (String name : processes.keySet()) {
            if (name.equals(selected)) {
                // Reset waiting info and restore base priority
                waitingTime.put(name, 0);
            } else {
                int wait = waitingTime.getOrDefault(name, 0) + 1;
                waitingTime.put(name, wait);

                if (agingInterval > 0 && wait % agingInterval == 0) {
                    effectivePriority.put(
                            name,
                            Math.max(0, effectivePriority.get(name) - 1)
                    );
                }
            }
        }

        return selected;
    }
}
