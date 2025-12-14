import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class SJFScheduler implements IScheduler {
    ArrayList<String> executionOrder = new ArrayList<>();

    public void schedule(ArrayList<Process> processes) {
        int i = 0;
        while(true) {
            processes.sort(
                    Comparator.comparingInt(Process::getBurstTime)
                            .thenComparingInt(Process::getArrivalTime)
                            .thenComparing(Comparator.comparingInt(Process::getPriority).reversed())
            );


            Process processToExecute  = new Process("", -1, -1, -1, -1);
            for (Process process : processes) {
                if (
                        process.getArrivalTime() <= i &&
                                (process.getBurstTime() < processToExecute.getBurstTime() || processToExecute.getBurstTime() == -1) &&
                                process.getBurstTime() > 0
                ) {

                    processToExecute = process;
                    process.setBurstTime(processToExecute.getBurstTime() - 1);
                } else if(process.getArrivalTime() <= i && process.getBurstTime() > 0) {
                    process.setWaitingTime(process.getWaitingTime() + 1);
                }
            }

            if(processToExecute.getArrivalTime() != -1 && (executionOrder.isEmpty() || !Objects.equals(processToExecute.getName(), executionOrder.getLast()))) {
                executionOrder.add(processToExecute.getName());
            }

            if(processes.stream().anyMatch(p -> p.getBurstTime() > 0)) {
                i++;
                continue;
            }
            break;
        }

        System.out.println("Execution Order: " + executionOrder);
        processes.sort(Comparator.comparing(Process::toString));
        System.out.println("Process Results: ");

        for(var process : processes) {
            System.out.println("name: " + process.getName() + ", waitingTime: " + process.getWaitingTime());
        }
    }
}
