package cpu.scheduler;

import java.util.Map;

public interface IScheduler {
    void setProcessSet(Map<String, Process> procceses);
    default void setParameter(String parameter, Object value) {}
    default void onNewProcess(String name, int time) {}
    default boolean doContextSwitch() { return true; }
    String /* Process name */ scheduleNext(int time); // Runs for 1s.
}
