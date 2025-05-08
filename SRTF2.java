import java.util.*;

class Process {
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

public class SRTF {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        List<Process> processes = new ArrayList<>();

        System.out.println("Enter process details (PID ArrivalTime BurstTime Priority). Enter '0 0 0 0' to stop:");

        // Input processes
        while (true) {
            int pid = sc.nextInt();
            int at = sc.nextInt();
            int bt = sc.nextInt();
            int pr = sc.nextInt();
            if (pid == 0 && at == 0 && bt == 0 && pr == 0) break;
            if (bt > 0) {  // Ignore processes with 0 or negative burst time
                processes.add(new Process(pid, at, bt, pr));
            } else {
                System.out.println("Burst time must be greater than 0. Skipping process " + pid);
            }
        }

        // Sort processes by arrival time
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));

        int time = 0;
        int completed = 0;
        int n = processes.size();
        List<Integer> ganttChart = new ArrayList<>();

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

            if (current != null) {
                ganttChart.add(current.pid);

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
                ganttChart.add(-1);  // CPU idle
                time++;
            }
        }

        // Display Gantt Chart with timestamps
        System.out.println("\nGantt Chart:");
        System.out.print("|");

        int currentPid = ganttChart.get(0);
        int startTime = 0;

        List<Integer> changePoints = new ArrayList<>();
        List<String> ganttLabels = new ArrayList<>();

        for (int i = 1; i < ganttChart.size(); i++) {
            if (!Objects.equals(ganttChart.get(i), currentPid)) {
                changePoints.add(i);
                ganttLabels.add(currentPid == -1 ? "IDLE" : "P" + currentPid);
                currentPid = ganttChart.get(i);
            }
        }

        // Add final segment
        changePoints.add(ganttChart.size());
        ganttLabels.add(currentPid == -1 ? "IDLE" : "P" + currentPid);

        // Print labels
        for (String label : ganttLabels) {
            System.out.print(" " + label + " |");
        }
        System.out.println();

        // Print timestamps
        int t = 0;
        System.out.print("0");
        for (int cp : changePoints) {
            System.out.printf("%" + (ganttLabels.get(changePoints.indexOf(cp)).length() + 3) + "d", cp);
        }
        System.out.println();

        // Display metrics
        System.out.println("\nProcess\tTAT\tWT\tRT");

        double totalTAT = 0, totalWT = 0, totalRT = 0;

        for (Process p : processes) {
            System.out.printf("P%d\t\t%d\t%d\t%d\n",
                    p.pid, p.turnaroundTime, p.waitingTime, p.responseTime);

            totalTAT += p.turnaroundTime;
            totalWT += p.waitingTime;
            totalRT += p.responseTime;
        }

        System.out.printf("\nAverage Turnaround Time: %.2f\n", totalTAT / n);
        System.out.printf("Average Waiting Time   : %.2f\n", totalWT / n);
        System.out.printf("Average Response Time  : %.2f\n", totalRT / n);
    }
}
