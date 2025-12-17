package cpu.scheduler;

import java.util.*;

public class RoundRobin implements IScheduler {
    private Map<String, Process> processes;
    private Queue<String> queue = new ArrayDeque<>();
    private int quantum;
    private int time = 0;
    private String processToAddNext = "";

    @Override
    public void setProcessSet(Map<String, Process> processes) {
        this.processes = processes;
    }


    @Override
    public void configure(int rrQuantum, int agingInterval) {
        this.quantum = rrQuantum;
    }

    @Override
    public void onNewProcess(String process) {
        this.queue.add(process);
    }

    @Override
    public String /* Process name */ scheduleNext() { // Runs for 1s.
        if (this.processToAddNext.length() != 0) {
            this.queue.add(this.processToAddNext);
            this.processToAddNext = "";
        }
        String process;
        if (time == quantum - 1) {
            time = 0;
            process = this.queue.poll();
            if (processes.get(process).burstTime > 1) this.processToAddNext = process;
        } else {
            process = queue.peek();
            time += 1;
            if (processes.get(process).burstTime == 1) {
                queue.poll();
                time = 0;
            }
        }
        return process;
    }
}
