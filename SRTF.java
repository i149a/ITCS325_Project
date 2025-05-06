import java.util.*;

public class SRTF {
    // ANSI color codes
    public static final String YELLOW = "\u001B[33m";
    public static final String GREEN = "\u001B[32m";
    public static final String BLUE = "\u001B[34m";
    public static final String CYAN = "\u001B[36m";
    public static final String RED = "\u001B[31m";
    public static final String RESET = "\u001B[0m";

    // Inner class to represent a Process
    static class Process {
        int pid;            // Process ID
        int arrival;        // Arrival Time
        int burst;          // Burst Time
        int priority;       // Priority
        int remaining;      // Remaining Time
        int start;          // Start Time
        int finish;         // Finish Time
        boolean started;    // To check if process has started

        public Process(int pid, int arrival, int burst, int priority) {
            this.pid = pid;
            this.arrival = arrival;
            this.burst = burst;
            this.priority = priority;
            this.remaining = burst;
            this.started = false;
            this.start = -1;
            this.finish = -1;
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
            System.out.print("Enter " + BLUE + "Process ID" + RESET + " : ");
            int pid = getValidNumber(scanner, true);

            // Arrival Time
            System.out.print("Enter " + GREEN + "Arrival Time" + RESET + " : ");
            int arrival = getValidNumber(scanner, true);

            // Burst Time
            System.out.print("Enter " + RED + "Burst Time" + RESET + " : ");
            int burst = getValidNumber(scanner, true);

            // Priority
            System.out.print("Enter " + YELLOW + "Priority" + RESET + " : ");
            int priority = getValidNumber(scanner, true);

            // Check termination condition (all zeros)
            if (pid == 0 && arrival == 0 && burst == 0 && priority == 0) {
                if (processes.isEmpty()) {
                    System.out.println(RED + "Warning: No processes entered!" + RESET);
                    continue;
                }
                break;
            }

            // Validate burst and priority are positive
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
        processes.sort(Comparator.comparingInt(p -> p.arrival));

        // Priority SRTF Scheduling
        PriorityQueue<Process> queue = new PriorityQueue<>((p1, p2) -> {
            if (p1.priority != p2.priority) {
                return p1.priority - p2.priority; // Lower priority number = higher priority
            } else {
                return p1.remaining - p2.remaining; // If same priority, go to SRTF
            }
        });

        List<String> ganttChart = new ArrayList<>();
        int currentTime = 0;
        int completed = 0;
        int totalProcesses = processes.size();
        Process currentProcess = null;

        System.out.println("\n" + YELLOW + "GANTT CHART" + RESET);
        System.out.println(YELLOW + "-----------" + RESET);
        System.out.print(GREEN + "0 " + RESET);

        while (completed < totalProcesses) {
            // Add arriving processes to the queue
            for (Process p : processes) {
                if (p.arrival == currentTime) {
                    queue.add(p);
                }
            }

            // If current process is finished
            if (currentProcess != null && currentProcess.remaining == 0) {
                currentProcess.finish = currentTime;
                completed++;
                currentProcess = null;
            }

            // Get the highest priority process with SRT
            Process nextProcess = queue.peek();
            if (nextProcess != null) {
                if (currentProcess == null ||
                        (nextProcess.priority < currentProcess.priority) ||
                        (nextProcess.priority == currentProcess.priority &&
                                nextProcess.remaining < currentProcess.remaining)) {

                    // Preempt current process if needed
                    if (currentProcess != null) {
                        queue.add(currentProcess);
                    }

                    currentProcess = queue.poll();

                    // Record start time for response time calculation
                    if (!currentProcess.started) {
                        currentProcess.start = currentTime;
                        currentProcess.started = true;
                    }

                    // Add to Gantt chart
                    ganttChart.add(YELLOW + "|" + RESET + " " + GREEN + currentTime + " " + RESET +
                            YELLOW + "|" + RESET + " " + BLUE + "P" + currentProcess.pid + " " + RESET);
                }
            }

            // Execute current process
            if (currentProcess != null) {
                currentProcess.remaining--;
            } else {
                ganttChart.add(YELLOW + "|" + RESET + " " + GREEN + currentTime + " " + RESET +
                        YELLOW + "|" + RESET + " IDLE ");
            }

            currentTime++;
        }

        // Print Gantt chart
        for (String entry : ganttChart) {
            System.out.print(entry);
        }
        System.out.println(YELLOW + "|" + RESET + " " + GREEN + currentTime + RESET);
        System.out.println(YELLOW + "-----------" + RESET);

        // Calculate and print metrics with correct formulas
        System.out.println("\n" + YELLOW + "PROCESS DETAILS" + RESET);
        System.out.println(YELLOW + "---------------" + RESET);

        float totalTAT = 0, totalWT = 0, totalRT = 0;

        for (Process p : processes) {
            int turnaround = p.finish - p.arrival;
            int waiting = p.finish - p.burst - p.arrival;
            int response = p.start - p.arrival;

            totalTAT += turnaround;
            totalWT += waiting;
            totalRT += response;

            System.out.println(BLUE + "P" + p.pid + " Details:" + RESET);
            System.out.println("Turnaround Time = finish(" + p.finish + ") - arrival(" + p.arrival + ") = " + turnaround);
            System.out.println("Waiting Time = finish(" + p.finish + ") - burst(" + p.burst + ") - arrival(" + p.arrival + ") = " + waiting);
            System.out.println("Response Time = start(" + p.start + ") - arrival(" + p.arrival + ") = " + response);
            System.out.println(YELLOW + "---------------" + RESET);
        }

        System.out.println("\n" + YELLOW + "AVERAGE METRICS" + RESET);
        System.out.println(YELLOW + "--------------" + RESET);
        System.out.printf("Average Turnaround Time = %.2f\n", totalTAT / totalProcesses);
        System.out.printf("Average Response Time = %.2f\n", totalRT / totalProcesses);
        System.out.printf("Average Waiting Time = %.2f\n", totalWT / totalProcesses);
        System.out.println(YELLOW + "--------------" + RESET);

        scanner.close();
    }

    // Validation for entered numbers from users
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
}