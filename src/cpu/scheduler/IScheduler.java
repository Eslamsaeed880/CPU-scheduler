package cpu.scheduler;

import java.util.Map;

public interface IScheduler {
    void setProcessSet(Map<String, Process> procceses);
    default void configure(int rrQuantum, int agingInterval) {}
    default void onNewProcess(String name) {}
    String /* Process name */ scheduleNext(); // Runs for 1s.
}
