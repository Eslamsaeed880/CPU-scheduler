package cpu.scheduler;

import cpu.scheduler.Process;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import org.json.*;

public class Main {
    public static void main(String[] testFiles) {
        Map<String, IScheduler> schedulers = new HashMap<>();
        // schedulers.put("SJF", new ShortestJobFirst());
        schedulers.put("RR", new RoundRobin());
        // schedulers.put("Priority", new Priority());
        int failedTests = 0;
        for (String fileName : testFiles) {
            String contents;
            try {
                contents = Files.readString(Path.of(fileName));
            } catch (IOException exc) {
                continue;
            }
            JSONObject test = new JSONObject(contents);
            String testName = test.getString("name");
            String decoration = "=".repeat(testName.length());
            System.out.println();
            System.out.println(decoration);
            System.out.println(testName);
            System.out.println(decoration);
            JSONObject testInput = test.getJSONObject("input");
            int contextSwitch = testInput.getInt("contextSwitch");
            int rrQuantum = testInput.getInt("rrQuantum");
            int agingInterval = testInput.getInt("agingInterval");
            for (IScheduler scheduler : schedulers.values()) {
                scheduler.configure(rrQuantum, agingInterval);
            }
            JSONArray processesArray = testInput.getJSONArray("processes");
            Map<String, Process> processes = new HashMap<>(processesArray.length());
            for (int i = 0; i < processesArray.length(); i++) {
                JSONObject proc = processesArray.getJSONObject(i);
                String name = proc.getString("name");
                int arrival = proc.getInt("arrival");
                int burst = proc.getInt("burst");
                int priority = proc.getInt("priority");
                processes.put(name, new Process(arrival, burst, priority));
            }
            SchedulerRunner runner = new SchedulerRunner(processes);
            runner.setContextSwitchTime(contextSwitch);
            JSONObject testOutput = test.getJSONObject("expectedOutput");
            int algorithmNumber = 0;
            boolean anyAlgorithmFailed = true;
            for (String algorithm : testOutput.keySet()) {
                System.out.print(++algorithmNumber + ". " + algorithm);
                if (!schedulers.containsKey(algorithm)) {
                    System.out.println(": NOT IMPLEMENETED");
                    continue;
                }
                boolean correctAlgorithm = true;
                System.out.println();
                IScheduler scheduler = schedulers.get(algorithm);
                JSONObject testOutputForAlgorithm = testOutput.getJSONObject(algorithm);
                List<String> expectedExecutionOrder = toList(testOutputForAlgorithm.getJSONArray("executionOrder"));
                List<String> executionOrder = runner.run(scheduler);
                if (executionOrder.equals(expectedExecutionOrder)) {
                    System.out.println("Execution order: OK: " + executionOrder);
                } else {
                    System.out.println("Execution order: BAD:      " + executionOrder);
                    System.out.println("                 EXPECTED: " + expectedExecutionOrder);
                    correctAlgorithm = false;
                }
                JSONArray expectedProcessResults = testOutputForAlgorithm.getJSONArray("processResults");
                for (int i = 0; i < expectedProcessResults.length(); i++) {
                    JSONObject expectedResult = expectedProcessResults.getJSONObject(i);
                    String processName = expectedResult.getString("name");
                    int expectedWaitingTime = expectedResult.getInt("waitingTime");
                    int expectedTurnaroundTime = expectedResult.getInt("turnaroundTime");
                    Process process = processes.get(processName);
                    int turnaroundTime = process.completionTime - process.getArrivalTime();
                    int waitingTime = turnaroundTime - process.burstTime;
                    if (waitingTime == expectedWaitingTime && expectedTurnaroundTime == expectedTurnaroundTime) {
                        System.out.println(processName + ": OK: W=" + waitingTime + "\tT=" + turnaroundTime);
                    } else {
                        System.out.println(processName + ": BAD:     \tW=" + waitingTime + "\tT=" + turnaroundTime);
                        System.out.println("    EXPECTED:\tW=" + expectedWaitingTime + "\tT=" + expectedTurnaroundTime);
                        correctAlgorithm = false;
                    }
                }
                if (!correctAlgorithm) {
                    System.out.println();
                    System.out.println("INCORRECT " + algorithm +  " ALGORITHM IMPLEMENTATION");
                    anyAlgorithmFailed = true;
                }
            }
            if (anyAlgorithmFailed) {
                failedTests++;
            }
        }
        System.out.println();
        System.out.println(failedTests + " tests failed out of " + testFiles.length);
    }

    static List<String> toList(JSONArray jsonArray) {
        List<String> list = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(jsonArray.getString(i));
        }
        return list;
    }
}
