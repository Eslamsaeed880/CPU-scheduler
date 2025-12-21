package cpu.scheduler;

import java.util.Map;

public interface IScheduler {
    default boolean processSetIsInQueueSet() { return true; }
    void setProcessSet(Map<String, Process> procceses);
    default void setParameter(String parameter, Object value) {}
    default void onNewProcess(String name, int time) {}
    default boolean doContextSwitch() { return true; }
    default boolean runProcess() { return true; }
    String /* Process name */ scheduleNext(int time); // Runs for 1s.
}
