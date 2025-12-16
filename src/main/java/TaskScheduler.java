import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages scheduled task execution using ScheduledExecutorService
 */
public class TaskScheduler {
    
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String LOGS_DIR = "./task_logs";
    
    private final ScheduledExecutorService scheduler;
    private final GradeManager gradeManager;
    private final ArrayList<Student> students;
    private final Map<String, ScheduledFuture<?>> activeSchedules;
    private final ConcurrentHashMap<String, ScheduledTask> taskConfigs;
    private final ConcurrentHashMap<String, TaskExecutionLog> executionLogs;
    
    /**
     * Task execution log entry
     */
    public static class TaskExecutionLog {
        public String taskId;
        public LocalDateTime startTime;
        public LocalDateTime endTime;
        public long durationMs;
        public boolean success;
        public String errorMessage;
        
        public TaskExecutionLog(String taskId) {
            this.taskId = taskId;
            this.startTime = LocalDateTime.now();
        }
        
        public void complete(boolean success, String error) {
            this.endTime = LocalDateTime.now();
            this.durationMs = ChronoUnit.MILLIS.between(startTime, endTime);
            this.success = success;
            this.errorMessage = error;
        }
    }
    
    public TaskScheduler(GradeManager gradeManager, ArrayList<Student> students) {
        this.gradeManager = gradeManager;
        this.students = students;
        this.scheduler = Executors.newScheduledThreadPool(4, new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(1);
            
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "TaskScheduler-" + counter.getAndIncrement());
                t.setDaemon(true);
                return t;
            }
        });
        this.activeSchedules = new ConcurrentHashMap<>();
        this.taskConfigs = new ConcurrentHashMap<>();
        this.executionLogs = new ConcurrentHashMap<>();
        
        // Initialize logs directory
        initializeLogsDirectory();
    }
    
    /**
     * Initialize logs directory
     */
    private void initializeLogsDirectory() {
        try {
            Files.createDirectories(Paths.get(LOGS_DIR));
        } catch (IOException e) {
            System.err.println("Failed to create logs directory: " + e.getMessage());
        }
    }
    
    /**
     * Schedule a new task
     */
    public void scheduleTask(ScheduledTask task) {
        if (activeSchedules.containsKey(task.taskId)) {
            System.out.println("⚠️  Task already scheduled: " + task.taskId);
            return;
        }
        
        taskConfigs.put(task.taskId, task);
        
        // Calculate initial delay
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextExecution = task.getNextExecutionTime(now);
        long initialDelay = ChronoUnit.MILLIS.between(now, nextExecution);
        
        // Determine period based on schedule type
        long period;
        TimeUnit unit = TimeUnit.MILLISECONDS;
        
        switch (task.scheduleType) {
            case HOURLY:
                period = task.intervalMinutes * 60 * 1000L;
                break;
            case DAILY:
                period = 24 * 60 * 60 * 1000L;
                break;
            case WEEKLY:
                period = 7 * 24 * 60 * 60 * 1000L;
                break;
            default:
                period = task.intervalMinutes * 60 * 1000L;
        }
        
        // Schedule the task
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
                () -> executeTask(task),
                initialDelay,
                period,
                unit
        );
        
        activeSchedules.put(task.taskId, future);
        
        System.out.println("✓ Task scheduled: " + task.description + 
                " (Next execution: " + nextExecution.format(TIME_FORMAT) + ")");
    }
    
    /**
     * Execute a task and log results
     */
    private synchronized void executeTask(ScheduledTask task) {
        if (!task.enabled) return;
        
        TaskExecutionLog log = new TaskExecutionLog(task.taskId);
        
        try {
            System.out.println("\n▶ Executing task: " + task.description);
            
            // Execute based on task type
            switch (task.taskType) {
                case DAILY_GPA_RECALC:
                    executeDailyGPARecalculation();
                    break;
                case HOURLY_STATS_REFRESH:
                    executeHourlyStatsRefresh();
                    break;
                case WEEKLY_BATCH_REPORTS:
                    executeWeeklyBatchReports();
                    break;
                case DAILY_BACKUP:
                    executeDailyBackup();
                    break;
                case CUSTOM:
                    System.out.println("  Custom task execution (placeholder)");
                    break;
            }
            
            log.complete(true, null);
            task.totalExecutions++;
            task.lastExecutionTime = log.startTime;
            task.lastExecutionDurationMs = log.durationMs;
            
            // Send notification
            sendTaskNotification(task, log, true);
            
            System.out.println("✓ Task completed: " + task.description + 
                    " (Duration: " + log.durationMs + "ms)");
            
        } catch (Exception e) {
            log.complete(false, e.getMessage());
            task.failureCount++;
            task.totalExecutions++;
            
            sendTaskNotification(task, log, false);
            
            System.err.println("✗ Task failed: " + task.description + 
                    " - " + e.getMessage());
        } finally {
            executionLogs.put(task.taskId, log);
            logTaskExecution(log, task);
        }
    }
    
    /**
     * Execute daily GPA recalculation
     */
    private void executeDailyGPARecalculation() {
        System.out.println("  [GPA] Recalculating GPAs for " + students.size() + " students...");
        
        for (Student student : students) {
            double totalGPA = 0;
            int count = 0;
            
            for (int i = 0; i < gradeManager.getGradeCount(); i++) {
                Grade grade = gradeManager.grades[i];
                if (grade != null && grade.getStudentId() == student.getId()) {
                    double gpa = grade.getGrade() >= 90 ? 4.0 :
                                grade.getGrade() >= 80 ? 3.0 :
                                grade.getGrade() >= 70 ? 2.0 :
                                grade.getGrade() >= 60 ? 1.0 : 0.0;
                    totalGPA += gpa;
                    count++;
                }
            }
            
            if (count > 0) {
                student.setAverageGrade(totalGPA / count);
            }
        }
        
        System.out.println("  [GPA] Completed for " + students.size() + " students");
    }
    
    /**
     * Execute hourly stats refresh
     */
    private void executeHourlyStatsRefresh() {
        System.out.println("  [Stats] Refreshing statistics cache...");
        
        long totalGrades = gradeManager.getGradeCount();
        double avgGrade = 0;
        
        if (totalGrades > 0) {
            double sum = 0;
            for (int i = 0; i < gradeManager.getGradeCount(); i++) {
                Grade grade = gradeManager.grades[i];
                if (grade != null) {
                    sum += grade.getGrade();
                }
            }
            avgGrade = sum / totalGrades;
        }
        
        System.out.println("  [Stats] Total grades: " + totalGrades + 
                ", Average: " + String.format("%.2f", avgGrade));
    }
    
    /**
     * Execute weekly batch reports (simulated)
     */
    private void executeWeeklyBatchReports() {
        System.out.println("  [Reports] Generating batch reports for " + students.size() + " students...");
        
        try {
            Path reportsDir = Paths.get("./reports");
            Files.createDirectories(reportsDir);
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path weeklyArchive = reportsDir.resolve("weekly_reports_" + timestamp + ".zip");
            
            System.out.println("  [Reports] Archive location: " + weeklyArchive);
            System.out.println("  [Reports] Generated reports for " + students.size() + " students");
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate reports: " + e.getMessage());
        }
    }
    
    /**
     * Execute daily backup
     */
    private void executeDailyBackup() {
        System.out.println("  [Backup] Starting database backup...");
        
        try {
            Path backupDir = Paths.get("./backups");
            Files.createDirectories(backupDir);
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path backupFile = backupDir.resolve("backup_" + timestamp + ".dat");
            
            // Simulate backup
            StringBuilder backupData = new StringBuilder();
            backupData.append("Students: ").append(students.size()).append("\n");
            backupData.append("Grades: ").append(gradeManager.getGradeCount()).append("\n");
            backupData.append("Timestamp: ").append(LocalDateTime.now().format(TIME_FORMAT)).append("\n");
            
            Files.write(backupFile, backupData.toString().getBytes());
            
            System.out.println("  [Backup] File: " + backupFile);
            System.out.println("  [Backup] Size: " + Files.size(backupFile) + " bytes");
            
        } catch (IOException e) {
            throw new RuntimeException("Backup failed: " + e.getMessage());
        }
    }
    
    /**
     * Send task completion notification (simulated email)
     */
    private void sendTaskNotification(ScheduledTask task, TaskExecutionLog log, boolean success) {
        String subject = (success ? "✓ COMPLETED" : "✗ FAILED") + ": " + task.description;
        String message = String.format(
                "Task: %s\nStatus: %s\nDuration: %dms\nTime: %s",
                task.description,
                success ? "Success" : "Failed",
                log.durationMs,
                log.endTime.format(TIME_FORMAT)
        );
        
        System.out.println("\n📧 [EMAIL NOTIFICATION]");
        System.out.println("Subject: " + subject);
        System.out.println("Message: " + message);
        System.out.println();
    }
    
    /**
     * Log task execution to file
     */
    private void logTaskExecution(TaskExecutionLog log, ScheduledTask task) {
        try {
            Path logFile = Paths.get(LOGS_DIR, task.taskId + ".log");
            String logEntry = String.format(
                    "[%s] %s - Duration: %dms - %s\n",
                    log.startTime.format(TIME_FORMAT),
                    log.success ? "SUCCESS" : "FAILED",
                    log.durationMs,
                    log.errorMessage != null ? "Error: " + log.errorMessage : ""
            );
            
            Files.write(logFile, logEntry.getBytes(), 
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Failed to log task execution: " + e.getMessage());
        }
    }
    
    /**
     * Get all active scheduled tasks
     */
    public List<ScheduledTask> getActiveTasks() {
        return new ArrayList<>(taskConfigs.values());
    }
    
    /**
     * Get task with countdown to next execution
     */
    public String getTaskStatus(ScheduledTask task) {
        LocalDateTime next = task.getNextExecutionTime(LocalDateTime.now());
        if (next == null) return "Task disabled";
        
        long millisUntil = ChronoUnit.MILLIS.between(LocalDateTime.now(), next);
        long seconds = millisUntil / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        String countdown;
        if (days > 0) {
            countdown = String.format("%dd %02dh", days, hours % 24);
        } else if (hours > 0) {
            countdown = String.format("%dh %02dm", hours, minutes % 60);
        } else if (minutes > 0) {
            countdown = String.format("%dm %02ds", minutes, seconds % 60);
        } else {
            countdown = String.format("%ds", seconds % 60);
        }
        
        return String.format("Next: %s | Countdown: %s | Executions: %d | Failures: %d | Last Duration: %dms",
                next.format(TIME_FORMAT),
                countdown,
                task.totalExecutions,
                task.failureCount,
                task.lastExecutionDurationMs);
    }
    
    /**
     * Create default scheduled tasks
     */
    public void createDefaultTasks() {
        // Daily GPA recalculation at 2 AM
        ScheduledTask gpaTask = new ScheduledTask(
                ScheduledTask.TaskType.DAILY_GPA_RECALC,
                ScheduledTask.ScheduleType.DAILY);
        gpaTask.executionTime = LocalTime.of(2, 0);
        gpaTask.description = "Daily GPA Recalculation (2:00 AM)";
        
        // Hourly stats refresh
        ScheduledTask statsTask = new ScheduledTask(
                ScheduledTask.TaskType.HOURLY_STATS_REFRESH,
                ScheduledTask.ScheduleType.HOURLY);
        statsTask.intervalMinutes = 60;
        statsTask.description = "Hourly Stats Cache Refresh";
        
        // Weekly batch reports (Monday 3 AM)
        ScheduledTask reportsTask = new ScheduledTask(
                ScheduledTask.TaskType.WEEKLY_BATCH_REPORTS,
                ScheduledTask.ScheduleType.WEEKLY);
        reportsTask.executionDay = DayOfWeek.MONDAY;
        reportsTask.executionTime = LocalTime.of(3, 0);
        reportsTask.description = "Weekly Batch Report Generation (Monday 3:00 AM)";
        
        // Daily backup at 1 AM
        ScheduledTask backupTask = new ScheduledTask(
                ScheduledTask.TaskType.DAILY_BACKUP,
                ScheduledTask.ScheduleType.DAILY);
        backupTask.executionTime = LocalTime.of(1, 0);
        backupTask.description = "Daily Database Backup (1:00 AM)";
        
        scheduleTask(gpaTask);
        scheduleTask(statsTask);
        scheduleTask(reportsTask);
        scheduleTask(backupTask);
    }
    
    /**
     * Disable a task
     */
    public void disableTask(String taskId) {
        ScheduledTask task = taskConfigs.get(taskId);
        if (task != null) {
            task.enabled = false;
            System.out.println("✓ Task disabled: " + task.description);
        }
    }
    
    /**
     * Enable a task
     */
    public void enableTask(String taskId) {
        ScheduledTask task = taskConfigs.get(taskId);
        if (task != null) {
            task.enabled = true;
            System.out.println("✓ Task enabled: " + task.description);
        }
    }
    
    /**
     * Remove a task
     */
    public void removeTask(String taskId) {
        ScheduledFuture<?> future = activeSchedules.remove(taskId);
        if (future != null) {
            future.cancel(false);
            taskConfigs.remove(taskId);
            System.out.println("✓ Task removed: " + taskId);
        }
    }
    
    /**
     * Shutdown scheduler gracefully
     */
    public void shutdown() {
        System.out.println("\n⏹ Shutting down task scheduler...");
        try {
            scheduler.shutdown();
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                System.out.println("⚠️  Force shutting down remaining tasks...");
                scheduler.shutdownNow();
            }
            System.out.println("✓ Task scheduler shutdown complete");
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
