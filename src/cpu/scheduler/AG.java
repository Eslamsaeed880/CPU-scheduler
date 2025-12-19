package cpu.scheduler;

import java.util.*;

public class AG implements IScheduler {

    private Map<String, Process> processes;
    public Map<String, ArrayList<Integer>> quantumHistory = new HashMap<>();

    private LinkedList<String> readyQueue = new LinkedList<>();
    private Set<String> inQueue = new HashSet<>();
    private String currentProcess = null;
    private int currentQuantum = 0;
    private int timeInQuantum = 0;

    @Override
    public void setProcessSet(Map<String, Process> processes) {
        this.processes = processes;
        quantumHistory.clear();
        readyQueue.clear();
        inQueue.clear();
        currentProcess = null;
        currentQuantum = 0;
        timeInQuantum = 0;
    }

    @Override
    public void onNewProcess(String name, int time) {
        readyQueue.add(name);
        inQueue.add(name);
        quantumHistory.putIfAbsent(name, new ArrayList<>());
        quantumHistory.get(name).add(processes.get(name).quantum);
    }

    @Override
    public boolean doContextSwitch() { return false; }

    @Override
    public String scheduleNext(int time) {
        // Handle finished process
        if (currentProcess != null && !processes.containsKey(currentProcess)) {
            quantumHistory.get(currentProcess).add(0);
            inQueue.remove(currentProcess);
            currentProcess = null;
            timeInQuantum = 0;
            currentQuantum = 0;
        }

        // Clean finished processes from queue
        readyQueue.removeIf(p -> !processes.containsKey(p));

        // Check if current process used its quantum
        if (currentProcess != null && timeInQuantum >= currentQuantum) {
            // Quantum expired without completion, move to end of queue
            Process p = processes.get(currentProcess);
            int newQ = p.quantum + 2;
            p.quantum = newQ;
            quantumHistory.get(currentProcess).add(newQ);
            readyQueue.add(currentProcess);
            currentProcess = null;
            timeInQuantum = 0;
            currentQuantum = 0;
        }

        // Pick new process if none running
        if (currentProcess == null) {
            if (readyQueue.isEmpty()) return "";
            currentProcess = readyQueue.poll();
            Process p = processes.get(currentProcess);
            currentQuantum = p.quantum;
            timeInQuantum = 0;
        }

        int phase1 = (int) Math.ceil(currentQuantum * 0.25);
        int phase2 = phase1 + (int) Math.ceil(currentQuantum * 0.25);

        // Phase 1: At 25% of quantum, check for higher priority process
        if (timeInQuantum == phase1) {
            String hp = findHigherPriority();
            if (hp != null) {
                preemptByPriority(hp);
                timeInQuantum++;
                return currentProcess;
            }
        }

        // Phase 2: At 50% of quantum, check for shorter burst process
        if (timeInQuantum == phase2) {
            String sj = findShorterBurst();
            if (sj != null) {
                preemptByBurst(sj);
                timeInQuantum++;
                return currentProcess;
            }
        }

        // Check if this is the last process and it will complete this tick
        if (processes.get(currentProcess).burstTime == 1 && readyQueue.isEmpty()) {
            quantumHistory.get(currentProcess).add(0);
        }

        timeInQuantum++;
        return currentProcess;
    }

    private void preemptByPriority(String newProcess) {
        int remaining = currentQuantum - timeInQuantum;
        Process p = processes.get(currentProcess);
        int newQ = p.quantum + (int) Math.ceil(remaining / 2.0);
        p.quantum = newQ;
        quantumHistory.get(currentProcess).add(newQ);
        readyQueue.add(currentProcess);

        switchTo(newProcess);
    }

    private void preemptByBurst(String newProcess) {
        int remaining = currentQuantum - timeInQuantum;
        Process p = processes.get(currentProcess);
        int newQ = p.quantum + remaining;
        p.quantum = newQ;
        quantumHistory.get(currentProcess).add(newQ);
        readyQueue.add(currentProcess);

        switchTo(newProcess);
    }

    private void switchTo(String newProcess) {
        currentProcess = newProcess;
        readyQueue.remove(newProcess);
        Process newP = processes.get(currentProcess);
        currentQuantum = newP.quantum;
        timeInQuantum = 0;
    }

    private String findHigherPriority() {
        if (currentProcess == null || !processes.containsKey(currentProcess)) return null;
        int currPri = processes.get(currentProcess).getPriority();

        String best = null;
        int bestPri = currPri;
        for (String name : readyQueue) {
            if (!processes.containsKey(name)) continue;
            int pri = processes.get(name).getPriority();
            if (pri < bestPri) {
                bestPri = pri;
                best = name;
            }
        }
        return best;
    }

    private String findShorterBurst() {
        if (currentProcess == null || !processes.containsKey(currentProcess)) return null;
        int currBurst = processes.get(currentProcess).burstTime;

        String best = null;
        int bestBurst = currBurst;
        for (String name : readyQueue) {
            if (!processes.containsKey(name)) continue;
            int burst = processes.get(name).burstTime;
            if (burst < bestBurst) {
                bestBurst = burst;
                best = name;
            }
        }
        return best;
    }
}
