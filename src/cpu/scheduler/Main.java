package cpu.scheduler;

import cpu.scheduler.Process;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import org.json.*;

public class Main {
    public static void main(String[] testFiles) {
        Map<String, IScheduler> schedulers = new HashMap<>();
        schedulers.put("SJF", new ShortestJobFirst());
        schedulers.put("RR", new RoundRobin());
        schedulers.put("Priority", new Priority());
        schedulers.put("AG", new AG());
        Map<String, Integer> schedulersFails = new HashMap<>();
        schedulersFails.put("SJF", 0);
        schedulersFails.put("RR", 0);
        schedulersFails.put("Priority", 0);
        schedulersFails.put("AG", 0);
        int numberOfAGTests = 0;
        int numberOfTests = 0;
        LinkedList<String> errors = new LinkedList();
        for (String fileName : testFiles) {
            String contents;
            Path path = Path.of(fileName);
            try {
                contents = Files.readString(path);
            } catch (IOException exc) {
                errors.push("error: " + path + ": " + exc.getMessage());
                continue;
            }
            numberOfTests++;
            fileName = path.getFileName().toString();
            boolean isAGTest =  fileName.startsWith("AG");
            if (isAGTest) numberOfAGTests++;
            JSONObject test = new JSONObject(contents);
            String testName = !isAGTest ? test.getString("name") : fileName;
            String decoration = "=".repeat(testName.length());
            System.out.println();
            System.out.println(decoration);
            System.out.println(testName);
            System.out.println(decoration);
            JSONObject testInput = test.getJSONObject("input");
            int contextSwitch = 0;
            if (!isAGTest) {
                contextSwitch = testInput.getInt("contextSwitch");
                int rrQuantum = testInput.getInt("rrQuantum");
                int agingInterval = testInput.getInt("agingInterval");
                for (IScheduler scheduler : schedulers.values()) {
                    scheduler.setParameter("rrQuantum", rrQuantum);
                    scheduler.setParameter("agingInterval", agingInterval);
                }
            }
            JSONArray processesArray = testInput.getJSONArray("processes");
            Map<String, Process> processes = new HashMap<>(processesArray.length());
            for (int i = 0; i < processesArray.length(); i++) {
                JSONObject proc = processesArray.getJSONObject(i);
                String name = proc.getString("name");
                int arrival = proc.getInt("arrival");
                int burst = proc.getInt("burst");
                int priority = proc.getInt("priority");
                Process newProcess = new Process(arrival, burst, priority);
                if (isAGTest) {
                    newProcess.quantum = proc.getInt("quantum");
                }
                processes.put(name, newProcess);
            }
            SchedulerRunner runner = new SchedulerRunner(processes);
            runner.setContextSwitchTime(contextSwitch);
            JSONObject testOutput = test.getJSONObject("expectedOutput");
            if (isAGTest) {
                boolean correctAlgorithm = testAlgorithm(processes, runner, schedulers.get("AG"), testOutput);
                if (!correctAlgorithm) {
                    System.out.println();
                    System.out.println("INCORRECT AG ALGORITHM IMPLEMENTATION");
                    schedulersFails.put("AG", schedulersFails.get("AG") + 1);
                }
                continue;
            }
            int algorithmNumber = 0;
            for (String algorithm : testOutput.keySet()) {
                System.out.println();
                System.out.print(++algorithmNumber + ". " + algorithm);
                if (!schedulers.containsKey(algorithm)) {
                    System.out.println(": NOT IMPLEMENETED");
                    continue;
                }
                System.out.println();
                IScheduler scheduler = schedulers.get(algorithm);
                JSONObject testOutputForAlgorithm = testOutput.getJSONObject(algorithm);
                boolean correctAlgorithm = testAlgorithm(processes, runner, scheduler, testOutputForAlgorithm);
                if (!correctAlgorithm) {
                    System.out.println();
                    System.out.println("INCORRECT " + algorithm +  " ALGORITHM IMPLEMENTATION");
                    schedulersFails.put(algorithm, schedulersFails.get(algorithm) + 1);
                }
            }
        }
        System.out.println();
        int numberOfNormalTests = numberOfTests - numberOfAGTests;
        for (Map.Entry<String, Integer> fail : schedulersFails.entrySet()) {
            int n  = fail.getKey() != "AG" ? numberOfNormalTests : numberOfAGTests;
            if (n == 0) {
                System.out.println(fail.getKey() + " didn't have any tests to run");
                continue;
            }
            if (fail.getValue() == n) {
                System.out.println(fail.getKey() + " failed every test cases out of " + n);
            } else if (fail.getValue() > 0) {
                System.out.println(fail.getKey() + " failed " + fail.getValue() +  " test cases out of " + n);
            } else {
                System.out.println(fail.getKey() + " passed every test case out of " + n);
            }
        }
        if (errors.size() > 0) System.out.println();
        for (String err : errors) System.out.println(err);
    }

    static boolean testAlgorithm(Map<String, Process> processes, SchedulerRunner runner, IScheduler scheduler, JSONObject testOutput) {
        boolean correctAlgorithm = true;
        List<String> expectedExecutionOrder = toStringList(testOutput.getJSONArray("executionOrder"));
        List<String> executionOrder = runner.run(scheduler);
        if (executionOrder.equals(expectedExecutionOrder)) {
            System.out.println("Execution order: OK: " + executionOrder);
        } else {
            System.out.println("Execution order: BAD:      " + executionOrder);
            System.out.println("                 EXPECTED: " + expectedExecutionOrder);
            correctAlgorithm = false;
        }
        JSONArray expectedProcessResults = testOutput.getJSONArray("processResults");
        boolean isAGTest = scheduler instanceof AG;
        int sumTurnaroundTime = 0;
        int sumWaitingTime = 0;
        for (int i = 0; i < expectedProcessResults.length(); i++) {
            JSONObject expectedResult = expectedProcessResults.getJSONObject(i);
            String processName = expectedResult.getString("name");
            int expectedWaitingTime = expectedResult.getInt("waitingTime");
            int expectedTurnaroundTime = expectedResult.getInt("turnaroundTime");
            Process process = processes.get(processName);
            int turnaroundTime = process.completionTime - process.getArrivalTime();
            int waitingTime = turnaroundTime - process.burstTime;
            sumTurnaroundTime += turnaroundTime;
            sumWaitingTime += waitingTime;
            if (isAGTest) {
                List<Integer> expectedQuantumHistory = toIntegerList(expectedResult.getJSONArray("quantumHistory"));
                AG ag = (AG)scheduler;
                if (waitingTime == expectedWaitingTime
                    && expectedTurnaroundTime == expectedTurnaroundTime
                    && ag.quantumHistory.get(processName).equals(expectedQuantumHistory)) {
                    System.out.println(processName + ": OK: W=" + waitingTime + "\tT=" + turnaroundTime + "\tQ=" + ag.quantumHistory.get(processName));
                } else {
                    System.out.println(processName + ": BAD:     \tW=" + waitingTime + "\tT=" + turnaroundTime + "\tQ=" + ag.quantumHistory.get(processName));
                    System.out.println("    EXPECTED:\tW=" + expectedWaitingTime + "\tT=" + expectedTurnaroundTime + "\tQ=" + expectedQuantumHistory);
                    correctAlgorithm = false;
                }
            } else {
                if (waitingTime == expectedWaitingTime && expectedTurnaroundTime == expectedTurnaroundTime) {
                    System.out.println(processName + ": OK: W=" + waitingTime + "\tT=" + turnaroundTime);
                } else {
                    System.out.println(processName + ": BAD:     \tW=" + waitingTime + "\tT=" + turnaroundTime);
                    System.out.println("    EXPECTED:\tW=" + expectedWaitingTime + "\tT=" + expectedTurnaroundTime);
                    correctAlgorithm = false;
                }
            }
        }
        float averageWaitingTime = ((float)sumWaitingTime / processes.size());
        float averageTurnaroundTime = ((float)sumTurnaroundTime / processes.size());
        System.out.println("Average waiting time: " + String.format("%.2f", averageWaitingTime));
        System.out.println("Average turnaround time: " + String.format("%.2f", averageTurnaroundTime));
        return correctAlgorithm;
    }

    static List<String> toStringList(JSONArray jsonArray) {
        List<String> list = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(jsonArray.getString(i));
        }
        return list;
    }

    static List<Integer> toIntegerList(JSONArray jsonArray) {
        List<Integer> list = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(jsonArray.getInt(i));
        }
        return list;
    }
}
