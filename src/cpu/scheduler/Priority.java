package cpu.scheduler;

import java.util.Map;

public class Priority implements IScheduler {
    private Map<String, Process> processes;
    private int agingInterval;

    @Override
    public void setProcessSet(Map<String, Process> processes) {
        this.processes = processes;
    }

    @Override
    public void configure(int rrQuantum, int agingInterval) {
        this.agingInterval = agingInterval;
    }

    @Override
    public String /* Process name */ scheduleNext() { // Runs for 1s.
        return "";
    }
}

