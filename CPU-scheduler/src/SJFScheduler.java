import java.util.ArrayList;
import java.util.Comparator;

public class SJFScheduler implements IScheduler {
    public void schedule(ArrayList<Process> processes) {

        int i = 0;
        while(true) {
            processes.sort(
                    Comparator.comparingInt(Process::getBurstTime)
                            .thenComparingInt(Process::getArrivalTime)
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
                }
            }

            System.out.println("In the " + i + "th iteration " + processToExecute.getName());

            if(processes.stream().anyMatch(p -> p.getBurstTime() > 0)) {
                i++;
                continue;
            }
            break;
        }

        return ;
    }
}
