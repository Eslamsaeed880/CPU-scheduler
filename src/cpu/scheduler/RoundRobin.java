package cpu.scheduler;

import java.util.*;

public class RoundRobin implements IScheduler {
    private Map<String, Process> processes;
    private Queue<String> queue = new ArrayDeque<>();
    private int quantum;
    private int timer = 0;
    private String processToQueueNext = null;

    @Override
    public void setProcessSet(Map<String, Process> processes) {
        this.processes = processes;
        this.processToQueueNext = null;
        this.timer = 0;
        this.queue.clear();
    }

    @Override
    public void setParameter(String param, Object value) {
        if (param == "rrQuantum") {
            this.quantum = (Integer)value;
        }
    }

    @Override
    public void onNewProcess(String process, int time) {
        this.queue.add(process);
    }

    @Override
    public String /* Process name */ scheduleNext(int time) { // Runs for 1s.
        // If in the last run there was a process that has ran
        // for quantum time but still not finished, we add it
        // to th ened of the queue.
        if (this.processToQueueNext != null) {
            this.queue.add(this.processToQueueNext);
            this.processToQueueNext = null;
        }
        // Process has ran for Quantum-1 units of time,
        // schedule it just one more time and reset the timer.
        if (this.timer == quantum - 1) {
            this.timer = 0;
            String process = this.queue.poll();
            // If the process has, still, some time left,
            // add it to the end of the queue.
            if (processes.get(process).burstTime > 1) {
                this.processToQueueNext = process;
            }
            return process;
        }
        // Else, get the process on the top of our queue,
        // but don't remove it and increase the timer.
        String process = queue.peek();
        this.timer += 1;
        // If the process has exactly 1 time unit left to
        // finish execution, then we should remove it
        // from the queue and reset the timer and
        // schedule it one last time.
        if (processes.get(process).burstTime == 1) {
            queue.poll();
            this.timer = 0;
        }
        return process;
    }
}
