import java.util.ArrayList;
import java.util.List;

public class Process {
    private String name;
    private int arrivalTime;
    private int burstTime;
    private int priority;
    private int quantum;
    private int waitingTime;
    private int turnaroundTime;
    private int remainingTime;

    List<Integer> quantumHistory = new ArrayList<>();

    public Process(String name, int burstTime, int arrivalTime, int priority, int quantum) {
        this.name = name;
        this.burstTime = burstTime;
        this.arrivalTime = arrivalTime;
        this.priority = priority;
        this.quantum = quantum;
        quantumHistory.add(arrivalTime);
    }

    public int getTurnaroundTime() {
        return turnaroundTime;
    }

    public void setTurnaroundTime(int turnaroundTime) {
        this.turnaroundTime = turnaroundTime;
    }

    public void addQuantum(int quantum) {
        this.quantumHistory.add(quantum);
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public void setBurstTime(int burstTime) {
        this.burstTime = burstTime;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getQuantum() {return quantum;}

    public void setQuantum(int quantum) {this.quantum = quantum;}

    public int getRemainingTime(){return remainingTime;}

    public void setRemainingTime(int remainingTime) {this.remainingTime = remainingTime;}

    @Override
    public String toString() {
        return getName();
    }
}
