import java.util.*;

public class AGScheduler implements IScheduler {

    Queue<Process> readyQueue = new LinkedList<>();
    List<String> executionOrder = new ArrayList<>();
    int time = 0;

    @Override
    public void schedule(ArrayList<Process> processes) {

        processes.sort(Comparator.comparingInt(Process::getArrivalTime));

        while (!allFinished(processes)) {

            // Add arrived processes
            for (Process p : processes) {
                if (p.getArrivalTime() == time)
                    readyQueue.add(p);
            }

            if (readyQueue.isEmpty()) {
                time++;
                continue;
            }

            Process current = readyQueue.poll();
            executionOrder.add(current.getName());

            int q = current.getQuantum();

            int q25 = (int) Math.ceil(q * 0.25);
            int q50 = (int) Math.ceil(q * 0.50);

            int used = 0;

            //FCFS case
            used += execute(current, q25);
            if (finishCheck(current)) continue;

            //non-preemptive priority case
            if (existsHigherPriority(current)) {
                handlePriorityInterruption(current, q, used);
                continue;
            }

            used += execute(current, q25);
            if (finishCheck(current)) continue;

            //SJF case
            if (existsShorterJob(current)) {
                handleSJFInterruption(current, q, used);
                continue;
            }

            //finish remaining quantum
            int remainingQuantum = q - used;
            used += execute(current, remainingQuantum);

            if (finishCheck(current)) continue;

            //full quantum used
            current.setQuantum(current.getQuantum() + 2);
            readyQueue.add(current);
        }

        printResults(processes);
    }

    // helper functions

    int execute(Process p, int timeSlice) {
        int exec = Math.min(timeSlice, p.getRemainingTime());
        p.setRemainingTime(p.getRemainingTime() - exec);
        time += exec;
        return exec;
    }

    boolean finishCheck(Process p) {
        if (p.getRemainingTime() == 0) {
            p.setQuantum(0);
            p.setTurnaroundTime(time - p.getArrivalTime());
            return true;
        }
        return false;
    }

    boolean existsHigherPriority(Process current) {
        return readyQueue.stream().anyMatch(p -> p.getPriority() < current.getPriority());
    }

    boolean existsShorterJob(Process current) {
        return readyQueue.stream().anyMatch(p -> p.getRemainingTime() < current.getRemainingTime());
    }

    void handlePriorityInterruption(Process p, int q, int used) {
        int remaining = q - used;
        p.setQuantum(p.getQuantum() + (int) Math.ceil(remaining / 2.0));
        readyQueue.add(p);
    }

    void handleSJFInterruption(Process p, int q, int used) {
        int remaining = q - used;
        p.setQuantum(p.getQuantum() + remaining);
        readyQueue.add(p);
    }

    boolean allFinished(ArrayList<Process> processes) {
        return processes.stream().allMatch(p -> p.getRemainingTime() == 0);
    }

    void printResults(ArrayList<Process> processes) {
        System.out.println("Execution Order: " + executionOrder);
        for (Process p : processes) {
            p.setWaitingTime(p.getTurnaroundTime() - p.getBurstTime());
            System.out.println(
                    p.getName() + " | Waiting: " + p.getWaitingTime() +
                            " | Turnaround: " + p.getTurnaroundTime()
            );
        }
    }
}
