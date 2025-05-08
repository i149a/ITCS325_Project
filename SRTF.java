import java.util.*;

public class SRTF {
    // ANSI color codes
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String BRIGHT_BLUE = "\u001B[94m";
    private static final String BRIGHT_GREEN = "\u001B[92m";
    private static final String BRIGHT_RED = "\u001B[91m";

    // Inner Process class
    static class Process {
        int pid, arrivalTime, burstTime, remainingTime, priority;
        int startTime = -1, completionTime, turnaroundTime, waitingTime, responseTime;

        Process(int pid, int arrivalTime, int burstTime, int priority) {
            this.pid = pid;
            this.arrivalTime = arrivalTime;
            this.burstTime = burstTime;
            this.remainingTime = burstTime;
            this.priority = priority;
        }
    }

    // Input validations
    private static int getValidNumber(Scanner scanner, boolean allowZero) {
        while (true) {
            try {
                int num = scanner.nextInt();
                if (allowZero && num >= 0) return num;
                if (!allowZero && num > 0) return num;

                System.out.print(RED + "Invalid input. Please enter a " +
                        (allowZero ? "non-negative" : "positive") + " number: " + RESET);
            } catch (InputMismatchException e) {
                System.out.print(RED + "Invalid input. Please enter a number: " + RESET);
                scanner.next(); // Clear invalid input
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<Process> processes = new ArrayList<>();

        System.out.println("\n" + YELLOW + "PROCESS SCHEDULING INPUT" + RESET);
        System.out.println(YELLOW + "-------------------------" + RESET);
        System.out.println("Enter process details for each process.");
        System.out.println("To finish input, enter " + YELLOW + "0 0 0 0" + RESET + " (all zeros).");
        System.out.println(YELLOW + "-------------------------" + RESET);

        while (true) {
            System.out.println("\n" + CYAN + "-- NEW PROCESS ENTRY --" + RESET);

            // Process ID
            System.out.print("Enter " + BRIGHT_BLUE + "Process ID" + RESET + " : ");
            int pid = getValidNumber(scanner, true);

            // Arrival Time
            System.out.print("Enter " + BRIGHT_GREEN + "Arrival Time" + RESET + " : ");
            int arrival = getValidNumber(scanner, true);

            // Burst Time
            System.out.print("Enter " + BRIGHT_RED + "Burst Time" + RESET + " : ");
            int burst = getValidNumber(scanner, true);

            // Priority
            System.out.print("Enter " + YELLOW + "Priority" + RESET + " : ");
            int priority = getValidNumber(scanner, true);

            // Check termination condition (all zeros)
            if (pid == 0 && arrival == 0 && burst == 0 && priority == 0) {
                System.out.println(PURPLE + "Process input terminated." + RESET);
                if (processes.isEmpty()) {
                    System.out.println(RED + "Warning: No processes entered!" + RESET);
                    continue;
                }
                break;
            }

            // Validate arrival, burst and priority are positive
            if (arrival < 0) {
                System.out.println(RED + "Error: Arrival time must be positive" + RESET);
                continue;
            }
            if (burst <= 0) {
                System.out.println(RED + "Error: Burst time must be positive" + RESET);
                continue;
            }
            if (priority <= 0) {
                System.out.println(RED + "Error: Priority must be positive" + RESET);
                continue;
            }

            processes.add(new Process(pid, arrival, burst, priority));
            System.out.println(GREEN + "✓ Process P" + pid + " added successfully!" + RESET);
            System.out.println("----------------------------");
        }

        // Sort processes by arrival time
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));

        int time = 0;
        int completed = 0;
        int n = processes.size();
        List<Integer> ganttPids = new ArrayList<>();
        List<Integer> ganttTimes = new ArrayList<>();

        // SRTF Scheduling
        while (completed < n) {
            Process current = null;

            // Select process with the shortest remaining time
            for (Process p : processes) {
                if (p.arrivalTime <= time && p.remainingTime > 0) {
                    if (current == null || p.remainingTime < current.remainingTime ||
                            (p.remainingTime == current.remainingTime && p.priority < current.priority)) {
                        current = p;
                    }
                }
            }

            int pid = (current != null) ? current.pid : -1;

            // Add to Gantt chart
            if (ganttPids.isEmpty() || ganttPids.get(ganttPids.size() - 1) != pid) {
                ganttPids.add(pid);
                ganttTimes.add(time);
            }

            if (current != null) {
                if (current.startTime == -1) {
                    current.startTime = time;
                    current.responseTime = current.startTime - current.arrivalTime;
                }

                current.remainingTime--;
                time++;

                if (current.remainingTime == 0) {
                    current.completionTime = time;
                    current.turnaroundTime = current.completionTime - current.arrivalTime;
                    current.waitingTime = current.turnaroundTime - current.burstTime;
                    completed++;
                }
            } else {
                time++;
            }
        }

        // Add the final time to the Gantt chart
        ganttTimes.add(time);

        // Display Gantt Chart
        System.out.println("\n" + PURPLE + "GANTT CHART" + RESET);
        System.out.println(PURPLE + "===========" + RESET);

        // Print top border
        System.out.print(BLUE + "+");
        for (int i = 0; i < ganttPids.size(); i++) {
            System.out.print("---+");
        }
        System.out.println(RESET);

        // Print process labels with fixed width
        System.out.print(BLUE + "|" + RESET);
        String[] colors = {GREEN, CYAN, YELLOW, BRIGHT_GREEN, BRIGHT_BLUE, PURPLE};
        for (int i = 0; i < ganttPids.size(); i++) {
            int pid = ganttPids.get(i);
            String color = (pid == -1) ? RED : colors[pid % colors.length];
            String label = (pid == -1) ? "IDL" : String.format("P%-2d", pid); // 3-char width
            System.out.print(color + label + RESET + BLUE + "|" + RESET);
        }
        System.out.println();

        // Print bottom border
        System.out.print(BLUE + "+");
        for (int i = 0; i < ganttPids.size(); i++) {
            System.out.print("---+");
        }
        System.out.println(RESET);

        // Print time markers
        for (int i = 0; i < ganttTimes.size(); i++) {
            System.out.printf("%-4d", ganttTimes.get(i)); // width 4 per column
        }
        System.out.println();

        // Display detailed process metrics
        System.out.println("\n" + CYAN + "PROCESS DETAILS" + RESET);
        System.out.println(CYAN + "===============" + RESET);

        double totalTAT = 0, totalWT = 0, totalRT = 0;
        String[] detailColors = {GREEN, CYAN, YELLOW, BRIGHT_GREEN, BRIGHT_BLUE};

        for (int i = 0; i < processes.size(); i++) {
            Process p = processes.get(i);
            String color = detailColors[i % detailColors.length];

            System.out.println(color + "P" + p.pid + " Details:" + RESET);
            System.out.println("Turnaround Time = " + p.completionTime + " - " + p.arrivalTime + " = " + color + p.turnaroundTime + RESET);
            System.out.println("Waiting Time = " + p.completionTime + " - " + p.arrivalTime + " - " + p.burstTime + " = " + color + p.waitingTime + RESET);
            System.out.println("Response Time = " + p.startTime + " - " + p.arrivalTime + " = " + color + p.responseTime + RESET);
            System.out.println(BLUE + "-------------------" + RESET);

            totalTAT += p.turnaroundTime;
            totalWT += p.waitingTime;
            totalRT += p.responseTime;
        }

        // Display average
        System.out.println("\n" + PURPLE + "PROCESSES AVERAGE MATRICES" + RESET);
        System.out.println(PURPLE + "==========================" + RESET);
        System.out.println("Average Turnaround Time = " + totalTAT + " / " + n + " = " + YELLOW + (totalTAT / n) + RESET);
        System.out.println("Average Waiting Time = " + totalWT + " / " + n + " = " + YELLOW + (totalWT / n) + RESET);
        System.out.println("Average Response Time = " + totalRT + " / " + n + " = " + YELLOW + (totalRT / n) + RESET);
    }
}