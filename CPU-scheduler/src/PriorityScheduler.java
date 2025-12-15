import java.util.*;

public class PriorityScheduler implements IScheduler {

    private static final int AGING_INTERVAL = 3;
    private static final int CONTEXT_SWITCH_TIME = 1;

    @Override
    public void schedule(ArrayList<Process> processes) {

        Process lastRunning = null;

        processes.sort(Comparator.comparingInt(Process::getArrivalTime));

        PriorityQueue<Process> readyQueue = new PriorityQueue<>(
                Comparator
                        .comparingInt(Process::getPriority)
                        .thenComparingInt(Process::getReadyQueueTime)
        );


        List<String> executionOrder = new ArrayList<>();
        List<Process> finishedProcesses = new ArrayList<>();

        int time = 0;
        int index = 0;
        Process current = null;

        while (index < processes.size() || !readyQueue.isEmpty() || current != null) {

            // Add arriving processes
            while (index < processes.size() &&
                    processes.get(index).getArrivalTime() <= time) {

                Process p = processes.get(index);
                p.setReadyQueueTime(time);
                readyQueue.add(p);
                index++;
            }


            // Aging
            List<Process> temp = new ArrayList<>();
            while (!readyQueue.isEmpty()) {
                Process p = readyQueue.poll();
                int waited = time - p.getArrivalTime();
                if (waited > 0 && waited % AGING_INTERVAL == 0) {
                    p.setPriority(Math.max(0, p.getPriority() - 1));
                }
                temp.add(p);
            }
            readyQueue.addAll(temp);

            // Preemption
            Process previous = current;
            if (current != null) {
                current.setReadyQueueTime(time);
                readyQueue.add(current);
                current = null;
            }


            if (!readyQueue.isEmpty()) {
                Process next = readyQueue.poll();

                // ADD CONTEXT SWITCH IF CPU CHANGES PROCESS
                if (lastRunning != null && !lastRunning.getName().equals(next.getName())) {
                    time += CONTEXT_SWITCH_TIME;
                }

                if (executionOrder.isEmpty() ||
                        !executionOrder.get(executionOrder.size() - 1).equals(next.getName())) {
                    executionOrder.add(next.getName());
                }

                current = next;
                lastRunning = next;
            }


            // Execute 1 time unit
            if (current != null) {
                current.setBurstTime(current.getBurstTime() - 1);
                time++;

                if (current.getBurstTime() == 0) {
                    current.setTurnaroundTime(time - current.getArrivalTime());
                    finishedProcesses.add(current);
                    current = null;
                }
            } else {
                time++;
            }
        }

        printResults(executionOrder, finishedProcesses);
    }

    private void printResults(List<String> executionOrder, List<Process> finishedProcesses) {

        System.out.println("Execution Order:");
        System.out.println(executionOrder);

        double totalWT = 0;
        double totalTAT = 0;

        System.out.println("\nProcess\tWT\tTAT");

        for (Process p : finishedProcesses) {
            int wt = p.getTurnaroundTime() - p.getOriginalBurstTime();
            p.setWaitingTime(wt);

            totalWT += wt;
            totalTAT += p.getTurnaroundTime();

            System.out.println(
                    p.getName() + "\t" +
                            wt + "\t" +
                            p.getTurnaroundTime()
            );
        }

        System.out.println("\nAverage Waiting Time = " +
                totalWT / finishedProcesses.size());
        System.out.println("Average Turnaround Time = " +
                totalTAT / finishedProcesses.size());
    }
}
