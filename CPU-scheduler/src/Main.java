import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nCPU Scheduler Menu:");
            System.out.println("1. Run Shortest Job First (SJF)");
            System.out.println("2. Run Priority Scheduler");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 0) {
                System.out.println("Exiting...");
                break;
            } else if (choice == 1) {
                ArrayList<Process> processes = new ArrayList<>();
                System.out.print("Enter number of processes: ");
                int n = scanner.nextInt();
                scanner.nextLine();
                for (int i = 0; i < n; i++) {
                    System.out.println("Process " + (i+1) + ":");
                    System.out.print("  Name: ");
                    String name = scanner.nextLine();
                    System.out.print("  Arrival Time: ");
                    int arrival = scanner.nextInt();
                    System.out.print("  Burst Time: ");
                    int burst = scanner.nextInt();
                    System.out.print("  Priority: ");
                    int priority = scanner.nextInt();
                    scanner.nextLine();
                    System.out.print("  Quantum:  ");
                    int quantum = scanner.nextInt();
                    scanner.nextLine();
                    processes.add(new Process(name, burst, arrival, priority, quantum));
                }
                SJFScheduler sjf = new SJFScheduler();
                sjf.schedule(processes);

            }else if(choice == 2) {
                ArrayList<Process> processes = new ArrayList<>();
                processes.add(new Process("P1", 8, 0, 3, 2));
                processes.add(new Process("P2", 4, 1, 1, 2));
                processes.add(new Process("P3", 2, 2, 4, 2));
                processes.add(new Process("P4", 1, 3, 2, 2));
                processes.add(new Process("P5", 3, 4, 5, 2));

                PriorityScheduler ps = new PriorityScheduler();
                ps.schedule(processes);
            }else {
                System.out.println("Invalid choice. Try again.");
            }
        }
        scanner.close();
    }
}
