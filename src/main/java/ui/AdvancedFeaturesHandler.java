package ui;

import context.ApplicationContext;
import core.Student;
import core.Grade;
import manager.GradeManager;
import analytics.StatisticsDashboard;
import search.ConcurrentReportGenerator;
import scheduler.ScheduledTask;
import scheduler.TaskScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * AdvancedFeaturesHandler - Handles dashboard, reporting, and task scheduling
 * Responsibility: Advanced analytics, concurrent operations, and scheduled tasks
 */
public class AdvancedFeaturesHandler {
    private final ApplicationContext context;
    private final Scanner scanner;
    private final ArrayList<Student> students;
    private final GradeManager gradeManager;

    public AdvancedFeaturesHandler(ApplicationContext context, Scanner scanner, 
                                   ArrayList<Student> students, GradeManager gradeManager) {
        this.context = context;
        this.scanner = scanner;
        this.students = students;
        this.gradeManager = gradeManager;
    }

    // ==================== STATISTICS DASHBOARD ====================
    public void launchStatisticsDashboard() {
        StatisticsDashboard dashboard = new StatisticsDashboard(gradeManager, students, scanner);
        dashboard.launch();
    }

    // ==================== CONCURRENT BATCH REPORTING ====================
    public void concurrentBatchReportGeneration() {
        if (students.isEmpty()) {
            System.out.println("No students in system.");
            return;
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("CONCURRENT BATCH REPORT GENERATION");
        System.out.println("=".repeat(60));
        System.out.println("1. Generate reports for all students (concurrent)");
        System.out.println("2. Generate reports for top 10 students");
        System.out.println("3. Back to Main Menu");
        System.out.print("Select option: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                ConcurrentReportGenerator generator = new ConcurrentReportGenerator(gradeManager, students, 4);
                generator.generateReportsParallel();
                break;
            case 2:
                ConcurrentReportGenerator generator2 = new ConcurrentReportGenerator(gradeManager, students, 4);
                generator2.generateReportsParallel();
                break;
            case 3:
                return;
            default:
                System.out.println("Invalid option!");
        }
    }

    // ==================== SCHEDULED TASKS MENU ====================
    public void openScheduledTasksMenu() {
        TaskScheduler taskScheduler = context.getTaskScheduler();
        if (taskScheduler == null) {
            System.out.println("Task scheduler is not available.");
            return;
        }
        List<ScheduledTask> tasks = taskScheduler.getActiveTasks();
        boolean inScheduler = true;

        while (inScheduler) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("TASK SCHEDULER");
            System.out.println("=".repeat(50));
            System.out.println("1. List all scheduled tasks");
            System.out.println("2. View task details");
            System.out.println("3. Toggle task status (enable/disable)");
            System.out.println("4. Execute a task immediately");
            System.out.println("5. Back to Main Menu");
            System.out.print("Select option: ");

            try {
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        listScheduledTasks(tasks);
                        break;
                    case 2:
                        viewTaskDetails(tasks);
                        break;
                    case 3:
                        toggleTaskStatus(tasks);
                        break;
                    case 4:
                        executeTaskImmediately(tasks);
                        break;
                    case 5:
                        inScheduler = false;
                        break;
                    default:
                        System.out.println("Invalid option!");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                scanner.nextLine();
            }
        }
    }

    private void listScheduledTasks(List<ScheduledTask> tasks) {
        if (tasks.isEmpty()) {
            System.out.println("No scheduled tasks.");
            return;
        }

        System.out.println("\n" + "=".repeat(80));
        System.out.println("SCHEDULED TASKS");
        System.out.println("=".repeat(80));
        System.out.printf("%-3s %-20s %-25s %-12s %-10s%n",
                "ID", "Task Type", "Schedule Type", "Interval", "Status");
        System.out.println("=".repeat(80));

        for (int i = 0; i < tasks.size(); i++) {
            ScheduledTask task = tasks.get(i);
            System.out.printf("%-3d %-20s %-25s %-12d %-10s%n",
                    i + 1,
                    task.taskType.displayName,
                    task.scheduleType.displayName,
                    task.intervalMinutes,
                    task.enabled ? "Enabled" : "Disabled");
        }
        System.out.println("=".repeat(80));
    }

    private void viewTaskDetails(List<ScheduledTask> tasks) {
        if (tasks.isEmpty()) {
            System.out.println("No scheduled tasks.");
            return;
        }

        listScheduledTasks(tasks);
        System.out.print("Enter task ID to view details: ");
        int taskId = scanner.nextInt() - 1;
        scanner.nextLine();

        if (taskId >= 0 && taskId < tasks.size()) {
            ScheduledTask task = tasks.get(taskId);
            System.out.println("\n" + "-".repeat(50));
            System.out.println("TASK DETAILS");
            System.out.println("-".repeat(50));
            System.out.println("Task ID: " + task.taskId);
            System.out.println("Task Type: " + task.taskType.displayName);
            System.out.println("Description: " + task.description);
            System.out.println("Schedule: " + task.scheduleType.displayName);
            System.out.println("Interval (mins): " + task.intervalMinutes);
            System.out.println("Status: " + (task.enabled ? "Enabled" : "Disabled"));
            System.out.println("Created: " + task.createdAt);
            System.out.println("-".repeat(50));
        } else {
            System.out.println("Invalid task ID!");
        }
    }

    private void toggleTaskStatus(List<ScheduledTask> tasks) {
        if (tasks.isEmpty()) {
            System.out.println("No scheduled tasks.");
            return;
        }

        listScheduledTasks(tasks);
        System.out.print("Enter task ID to toggle: ");
        int taskId = scanner.nextInt() - 1;
        scanner.nextLine();

        if (taskId >= 0 && taskId < tasks.size()) {
            ScheduledTask task = tasks.get(taskId);
            task.enabled = !task.enabled;
            System.out.println("Task '" + task.taskType.displayName + "' is now " + 
                    (task.enabled ? "ENABLED" : "DISABLED"));
        } else {
            System.out.println("Invalid task ID!");
        }
    }

    private void executeTaskImmediately(List<ScheduledTask> tasks) {
        if (tasks.isEmpty()) {
            System.out.println("No scheduled tasks.");
            return;
        }

        listScheduledTasks(tasks);
        System.out.print("Enter task ID to execute: ");
        int taskId = scanner.nextInt() - 1;
        scanner.nextLine();

        if (taskId >= 0 && taskId < tasks.size()) {
            ScheduledTask task = tasks.get(taskId);
            System.out.println("\n▶ Running task: " + task.taskType.displayName);
            System.out.println("(In actual implementation, this would execute the task immediately)");
            System.out.println("Task execution would be logged with timestamp and duration.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        } else {
            System.out.println("Invalid task ID!");
        }
    }
}
