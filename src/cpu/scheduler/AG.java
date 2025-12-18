package cpu.scheduler;

import java.util.*;

public class AG implements IScheduler {

    private Map<String, Process> processes;

    public Map<String, ArrayList<Integer>> quantumHistory = new HashMap<>();

    private String currentProcess = null;
    private int timeInCurrentQuantum = 0;
    private int currentQuantumValue = 0;

    private LinkedList<String> readyQueue = new LinkedList<>();
    private Set<String> inQueue = new HashSet<>();

    @Override
    public void setProcessSet(Map<String, Process> processes) {
        this.processes = processes;

        quantumHistory.clear();
        readyQueue.clear();
        inQueue.clear();

        currentProcess = null;
        timeInCurrentQuantum = 0;
        currentQuantumValue = 0;
    }

    @Override
    public void onNewProcess(String processName, int time) {
        if (!inQueue.contains(processName)) {
            readyQueue.add(processName);
            inQueue.add(processName);

            quantumHistory.putIfAbsent(processName, new ArrayList<>());
            quantumHistory.get(processName).add(processes.get(processName).quantum);
        }
    }

    @Override
    public boolean doContextSwitch() {
        return false;
    }

    @Override
    public String scheduleNext(int time) {

        // handle termination of current process
        if (currentProcess != null && !processes.containsKey(currentProcess)) {

            ArrayList<Integer> history = quantumHistory.get(currentProcess);
            if (history != null && history.get(history.size() - 1) != 0) {
                history.add(0);
            }

            inQueue.remove(currentProcess);
            currentProcess = null;
            timeInCurrentQuantum = 0;
            currentQuantumValue = 0;
        }

        // remove finished process
        readyQueue.removeIf(p -> !processes.containsKey(p));

        // select new process FCFS
        if (currentProcess == null) {

            if (readyQueue.isEmpty()) {
                return "";
            }

            currentProcess = readyQueue.poll();
            currentQuantumValue = processes.get(currentProcess).quantum;
            timeInCurrentQuantum = 0;

            if (currentQuantumValue > processes.get(currentProcess).burstTime) {
                currentQuantumValue = processes.get(currentProcess).burstTime;
            }

            return currentProcess;
        }

        // determine boundaries
        int phase1End = (int) Math.ceil(currentQuantumValue * 0.25);
        int phase2End = phase1End + (int) Math.ceil(currentQuantumValue * 0.25);

        // phase 2 priority check
        if (timeInCurrentQuantum == phase1End &&
                timeInCurrentQuantum < currentQuantumValue) {

            String higherPriority = findHigherPriorityProcess();
            if (higherPriority != null) {
                handlePreemption(2);
                return scheduleNext(time);
            }
        }

        // phase 3 SJF
        if (timeInCurrentQuantum == phase2End &&
                timeInCurrentQuantum < currentQuantumValue) {

            String shorterJob = findShorterProcess();
            if (shorterJob != null) {
                handlePreemption(3);
                return scheduleNext(time);
            }
        }

        // quantum exhausted
        if (timeInCurrentQuantum >= currentQuantumValue) {

            if (processes.containsKey(currentProcess)) {
                readyQueue.add(currentProcess);
            }

            currentProcess = null;
            return scheduleNext(time);
        }


        timeInCurrentQuantum++; // execute one time unit
        return currentProcess;
    }


    // needed functions

    private String findHigherPriorityProcess() {
        int currentPriority = processes.get(currentProcess).getPriority();
        for (String p : readyQueue) {
            if (processes.get(p).getPriority() < currentPriority) {
                return p;
            }
        }
        return null;
    }

    private String findShorterProcess() {
        int currentBurst = processes.get(currentProcess).burstTime;
        for (String p : readyQueue) {
            if (processes.get(p).burstTime < currentBurst) {
                return p;
            }
        }
        return null;
    }

    private void handlePreemption(int phaseNumber) {

        int remainingQuantum = currentQuantumValue - timeInCurrentQuantum;
        int oldQuantum = processes.get(currentProcess).quantum;
        int newQuantum;

        if (phaseNumber == 2) {
            newQuantum = oldQuantum + (int) Math.ceil(remainingQuantum / 2.0);
        } else {
            newQuantum = oldQuantum + remainingQuantum;
        }

        processes.get(currentProcess).quantum = newQuantum;
        quantumHistory.get(currentProcess).add(newQuantum);

        readyQueue.add(currentProcess);

        currentProcess = null;
        timeInCurrentQuantum = 0;
        currentQuantumValue = 0;
    }
}
