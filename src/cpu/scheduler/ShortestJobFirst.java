package cpu.scheduler;

import java.util.Map;

public class ShortestJobFirst implements IScheduler {
    private Map<String, Process> processes;

    @Override
    public void setProcessSet(Map<String, Process> processes) {
        this.processes = processes;
    }

    @Override
    public String /* Process name */ scheduleNext() { // Runs for 1s.
        return "";
    }
}

