package scheduler;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

/**
 * Represents a scheduled task configuration
 */
public class ScheduledTask {
    
    public enum TaskType {
        DAILY_GPA_RECALC("Daily GPA Recalculation"),
        HOURLY_STATS_REFRESH("Hourly Stats Cache Refresh"),
        WEEKLY_BATCH_REPORTS("Weekly Batch Report Generation"),
        DAILY_BACKUP("Daily Database Backup"),
        CUSTOM("Custom Task");
        
        public final String displayName;
        
        TaskType(String displayName) {
            this.displayName = displayName;
        }
    }
    
    public enum ScheduleType {
        HOURLY("Every hour"),
        DAILY("Once per day"),
        WEEKLY("Once per week"),
        CUSTOM("Custom interval");
        
        public final String displayName;
        
        ScheduleType(String displayName) {
            this.displayName = displayName;
        }
    }
    
    public String taskId;
    public TaskType taskType;
    public ScheduleType scheduleType;
    public int intervalMinutes;
    public LocalTime executionTime; // For daily tasks
    public DayOfWeek executionDay; // For weekly tasks
    public String description;
    public boolean enabled;
    public LocalDateTime createdAt;
    public LocalDateTime lastExecutionTime;
    public long lastExecutionDurationMs;
    public int totalExecutions;
    public int failureCount;
    
    public ScheduledTask(TaskType type, ScheduleType schedule) {
        this.taskId = UUID.randomUUID().toString();
        this.taskType = type;
        this.scheduleType = schedule;
        this.intervalMinutes = 60; // Default 1 hour
        this.executionTime = LocalTime.of(2, 0); // Default 2 AM
        this.executionDay = DayOfWeek.MONDAY; // Default Monday
        this.description = type.displayName;
        this.enabled = true;
        this.createdAt = LocalDateTime.now();
        this.lastExecutionTime = null;
        this.lastExecutionDurationMs = 0;
        this.totalExecutions = 0;
        this.failureCount = 0;
    }
    
    public long getMillisUntilNextExecution() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextExecution = getNextExecutionTime(now);
        return ChronoUnit.MILLIS.between(now, nextExecution);
    }
    
    public LocalDateTime getNextExecutionTime(LocalDateTime from) {
        if (!enabled) return null;
        
        switch (scheduleType) {
            case HOURLY:
                return from.plusMinutes(intervalMinutes);
            case DAILY:
                LocalDateTime nextDaily = from.withHour(executionTime.getHour())
                        .withMinute(executionTime.getMinute())
                        .withSecond(0);
                if (nextDaily.isBefore(from) || nextDaily.isEqual(from)) {
                    nextDaily = nextDaily.plusDays(1);
                }
                return nextDaily;
            case WEEKLY:
                LocalDateTime nextWeekly = from.with(TemporalAdjusters.next(executionDay))
                        .withHour(executionTime.getHour())
                        .withMinute(executionTime.getMinute())
                        .withSecond(0);
                return nextWeekly;
            default:
                return from.plusMinutes(intervalMinutes);
        }
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s - Next: %s (Executions: %d, Failures: %d)",
                taskId.substring(0, 8),
                description,
                getNextExecutionTime(LocalDateTime.now()),
                totalExecutions,
                failureCount);
    }
}
