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
                scanner.next();
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

            System.out.print("Enter " + BRIGHT_BLUE + "Process ID" + RESET + " : ");
            int pid = getValidNumber(scanner, true);

            System.out.print("Enter " + BRIGHT_GREEN + "Arrival Time" + RESET + " : ");
            int arrival = getValidNumber(scanner, true);

            System.out.print("Enter " + BRIGHT_RED + "Burst Time" + RESET + " : ");
            int burst = getValidNumber(scanner, true);

            System.out.print("Enter " + YELLOW + "Priority" + RESET + " : ");
            int priority = getValidNumber(scanner, true);

            if (pid == 0 && arrival == 0 && burst == 0 && priority == 0) {
                System.out.println(PURPLE + "Process input terminated." + RESET);
                System.out.println(YELLOW + "----------------------------" + RESET);
                if (processes.isEmpty()) {
                    System.out.println(RED + "Warning: No processes entered!" + RESET);
                    continue;
                }
                break;
            }

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

        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));

        int time = 0, completed = 0, n = processes.size();
        List<Integer> ganttPids = new ArrayList<>();
        List<Integer> ganttTimes = new ArrayList<>();
        ganttTimes.add(0);
        Process current = null;

        while (completed < n) {
            Process selected = null;

            for (Process p : processes) {
                if (p.arrivalTime <= time && p.remainingTime > 0) {
                    if (selected == null ||
                            p.priority < selected.priority ||
                            (p.priority == selected.priority && p.remainingTime < selected.remainingTime)) {
                        selected = p;
                    }
                }
            }

            if (selected != null) {
                if (current == null || current.pid != selected.pid) {
                    if (current != null) {
                        ganttTimes.add(time);
                    }
                    ganttPids.add(selected.pid);
                    current = selected;
                }

                if (selected.startTime == -1) {
                    selected.startTime = time;
                    selected.responseTime = time - selected.arrivalTime;
                }

                selected.remainingTime--;
                time++;

                if (selected.remainingTime == 0) {
                    selected.completionTime = time;
                    selected.turnaroundTime = selected.completionTime - selected.arrivalTime;
                    selected.waitingTime = selected.turnaroundTime - selected.burstTime;
                    completed++;
                    ganttTimes.add(time);
                    current = null;
                }
            } else {
                time++;
            }
        }

        // Print Gantt Chart
        System.out.println("\n" + PURPLE + "GANTT CHART" + RESET);
        System.out.println(PURPLE + "===========" + RESET);
        System.out.print(BLUE + "+");
        for (int pid : ganttPids) System.out.print("----+");
        System.out.println(RESET);

        System.out.print(BLUE + "|");
        String[] colors = {GREEN, CYAN, YELLOW, BRIGHT_GREEN, BRIGHT_BLUE, PURPLE};
        for (int pid : ganttPids) {
            String color = colors[pid % colors.length];
            System.out.print(color + " P" + pid + " " + RESET + BLUE + "|");
        }
        System.out.println(RESET);

        System.out.print(BLUE + "+");
        for (int pid : ganttPids) System.out.print("----+");
        System.out.println(RESET);

        for (int i = 0; i < ganttTimes.size(); i++) {
            System.out.printf("%-5d", ganttTimes.get(i));
        }
        System.out.println();

        // Calculate and display metrics
        double totalTAT = 0, totalWT = 0, totalRT = 0;
        System.out.println("\n" + CYAN + "PROCESS DETAILS" + RESET);
        System.out.println(CYAN + "===============" + RESET);

        for (Process p : processes) {
            String color = colors[p.pid % colors.length];
            System.out.println(color + "P" + p.pid + " Details:" + RESET);
            System.out.println("Turnaround Time = " + p.completionTime + " - " +
                    p.arrivalTime + " = " + color + p.turnaroundTime + RESET);
            System.out.println("Waiting Time = " + p.turnaroundTime + " - " +
                    (p.completionTime - p.startTime) + " = " + color + p.waitingTime + RESET);
            System.out.println("Response Time = " + p.startTime + " - " +
                    p.arrivalTime + " = " + color + p.responseTime + RESET);
            System.out.println(BLUE + "-------------------" + RESET);

            totalTAT += p.turnaroundTime;
            totalWT += p.waitingTime;
            totalRT += p.responseTime;
        }

        System.out.println("\n" + PURPLE + "AVERAGE METRICS" + RESET);
        System.out.println(PURPLE + "===============" + RESET);
        System.out.println("Average Turnaround Time = " + totalTAT + " / " + n + " = " +
                YELLOW + String.format("%.2f", totalTAT / n) + RESET);
        System.out.println("Average Waiting Time = " + totalWT + " / " + n + " = " +
                YELLOW + String.format("%.2f", totalWT / n) + RESET);
        System.out.println("Average Response Time = " + totalRT + " / " + n + " = " +
                YELLOW + String.format("%.2f", totalRT / n) + RESET);
    }
}
