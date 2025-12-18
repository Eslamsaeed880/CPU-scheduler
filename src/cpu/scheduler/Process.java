package cpu.scheduler;

import java.util.ArrayList;
import java.util.List;

public class Process {
    private int arrivalTime;
    public int burstTime;
    private int priority;

    public int quantum;

    // Implementers of IScheduler shouldn't access this variable
    public int completionTime;

    public Process(int arrivalTime, int burstTime, int priority) {
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
    }

    public Process(Process other) {
        this.arrivalTime = other.arrivalTime;
        this.burstTime = other.burstTime;
        this.priority = other.priority;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getPriority() {
        return priority;
    }
}
