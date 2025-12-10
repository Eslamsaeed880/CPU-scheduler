import java.util.List;

public interface IScheduler {
    Result schedule(List<Process> processes);
}
