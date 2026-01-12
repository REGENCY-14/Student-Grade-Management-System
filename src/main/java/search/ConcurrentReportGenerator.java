package search;

import manager.GradeManager;
import manager.CacheManager;
import audit.AuditLogger;
import core.Student;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Concurrent batch report generation system using ThreadPool
 * Handles parallel report generation with progress tracking and metrics
 */
public class ConcurrentReportGenerator {
    
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final String REPORTS_DIR = "./reports";
    
    private final GradeManager gradeManager;
    private final ArrayList<Student> students;
    private final int threadCount;
    private final ExecutorService executorService;
    private final List<ReportTask> reportTasks;
    private final ProgressTracker progressTracker;
    private final Object fileLock;
    
    /**
     * Inner class for report generation statistics
     */
    public static class ReportStats {
        public String studentName;
        public int studentId;
        public long generationTimeMs;
        public boolean success;
        public String filePath;
        public String errorMessage;
        public int gradesCount;
        public double averageGrade;
        
        public ReportStats(String name, int id) {
            this.studentName = name;
            this.studentId = id;
            this.success = false;
        }
    }
    
    /**
     * Inner class for individual report generation task
     */
    private class ReportTask implements Runnable {
        private final Student student;
        private final ReportStats stats;
        private final CountDownLatch completionLatch;
        
        ReportTask(Student student, CountDownLatch latch) {
            this.student = student;
            this.stats = new ReportStats(student.getName(), student.getId());
            this.completionLatch = latch;
        }
        
        @Override
        public void run() {
            long startTime = System.currentTimeMillis();
            
            try {
                // audit: report generation start
                try {
                    AuditLogger.getInstance().log(
                            "REPORT_GENERATION_START",
                            "studentId=" + student.getId(),
                            -1,
                            true,
                            ""
                    );
                } catch (Exception ex) { }

                // Generate report content
                StringBuilder reportContent = generateReportContent(student);
                
                // Write report to file (thread-safe)
                String filePath = writeReportToFile(reportContent, student);
                
                // Update statistics (prefer cached report summary when available)
                stats.filePath = filePath;
                stats.success = true;
                stats.generationTimeMs = System.currentTimeMillis() - startTime;
                try {
                    Object rep = CacheManager.getInstance().get("report:" + student.getId());
                    if (rep instanceof CacheManager.CacheReport) {
                        CacheManager.CacheReport cr = (CacheManager.CacheReport) rep;
                        stats.gradesCount = cr.grades.size();
                        stats.averageGrade = cr.overallAvg;
                    } else {
                        stats.gradesCount = ConcurrentReportGenerator.this.countGradesForStudent(student.getId());
                        stats.averageGrade = ConcurrentReportGenerator.this.calculateStudentAverage(student.getId());
                    }
                } catch (Exception ex) {
                    stats.gradesCount = ConcurrentReportGenerator.this.countGradesForStudent(student.getId());
                    stats.averageGrade = ConcurrentReportGenerator.this.calculateStudentAverage(student.getId());
                }

                // audit: report generation success
                try {
                    AuditLogger.getInstance().log(
                            "REPORT_GENERATION_END",
                            "studentId=" + student.getId() + ",file=" + filePath,
                            stats.generationTimeMs,
                            true,
                            ""
                    );
                } catch (Exception ex) { }

                // Update progress
                progressTracker.reportCompleted(student.getName(), stats.generationTimeMs);
                
            } catch (Exception e) {
                stats.success = false;
                stats.errorMessage = e.getMessage();
                stats.generationTimeMs = System.currentTimeMillis() - startTime;

                // audit: report generation failure
                try {
                    AuditLogger.getInstance().log(
                            "REPORT_GENERATION_END",
                            "studentId=" + student.getId(),
                            stats.generationTimeMs,
                            false,
                            e.getMessage()
                    );
                } catch (Exception ex) { }

                progressTracker.reportFailed(student.getName(), e);
            } finally {
                completionLatch.countDown();
            }
        }
        
        /**
         * Generate report content for a student
         */
        private StringBuilder generateReportContent(Student student) {
            StringBuilder report = new StringBuilder();
            
            report.append("╔").append("═".repeat(78)).append("╗\n");
            report.append("║").append(" ".repeat(20)).append("STUDENT GRADE REPORT\n");
            report.append("║").append(" ".repeat(78)).append("║\n");
            report.append("╠").append("═".repeat(78)).append("╣\n");
            
            // Student Information
            report.append("║ Student Name: ").append(String.format("%-62s│\n", student.getName()));
            report.append("║ Student ID: ").append(String.format("%-64s│\n", student.getId()));
            report.append("║ Age: ").append(String.format("%-72s│\n", student.getAge()));
            report.append("║ Type: ").append(String.format("%-71s│\n", student.getClass().getSimpleName()));
            
            // Grade Information
            int gradeCount = ConcurrentReportGenerator.this.countGradesForStudent(student.getId());
            report.append("║ Total Grades: ").append(String.format("%-61s│\n", gradeCount));
            
            if (gradeCount > 0) {
                double average = calculateStudentAverage(student.getId());
                report.append("║ Average Grade: ").append(String.format("%-60.2f│\n", average));
                
                // Grade Details
                report.append("╠").append("═".repeat(78)).append("╣\n");
                report.append("║ GRADES\n");
                report.append("╠").append("═".repeat(78)).append("╣\n");
                report.append(String.format("║ %-40s │ %-10s │ %-20s │\n", "Subject", "Grade", "Date"));
                report.append("║").append("─".repeat(78)).append("│\n");
                
                for (int i = 0; i < gradeManager.getGradeCount(); i++) {
                    Grade grade = gradeManager.grades[i];
                    if (grade != null && grade.getStudentId() == student.getId()) {
                        report.append(String.format("║ %-40s │ %8.2f │ %-20s │\n",
                                grade.getSubject().getSubjectName(),
                                grade.getGrade(),
                                grade.getDate().toString()));
                    }
                }
            }
            
            report.append("╠").append("═".repeat(78)).append("╣\n");
            report.append("║ Report Generated: ").append(LocalDateTime.now().format(TIME_FORMAT)).append("\n");
            report.append("╚").append("═".repeat(78)).append("╝\n");
            
            return report;
        }
        
        /**
         * Write report to file with thread-safe locking
         */
        private String writeReportToFile(StringBuilder content, Student student) throws IOException {
            synchronized (fileLock) {
                // Ensure reports directory exists
                Path reportsPath = Paths.get(REPORTS_DIR);
                Files.createDirectories(reportsPath);
                
                // Create timestamped filename
                String timestamp = LocalDateTime.now().format(FILE_DATE_FORMAT);
                String filename = String.format("%s/%s_%d_%s.txt",
                        REPORTS_DIR, student.getName().replaceAll(" ", "_"), student.getId(), timestamp);
                
                // Write file
                Files.write(Paths.get(filename), content.toString().getBytes(), 
                        StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                
                return filename;
            }
        }
    }
    
    /**
     * Progress tracking for concurrent operations
     */
    private class ProgressTracker {
        private final AtomicInteger completedCount;
        private final AtomicInteger failureCount;
        private final ConcurrentHashMap<String, Long> completionTimes;
        private final long startTime;
        
        ProgressTracker(int totalTasks) {
            this.completedCount = new AtomicInteger(0);
            this.failureCount = new AtomicInteger(0);
            this.completionTimes = new ConcurrentHashMap<>();
            this.startTime = System.currentTimeMillis();
        }
        
        void reportCompleted(String studentName, long generationTime) {
            completedCount.incrementAndGet();
            completionTimes.put(studentName, generationTime);
        }
        
        void reportFailed(String studentName, Exception e) {
            failureCount.incrementAndGet();
        }
        
        int getCompletedCount() {
            return completedCount.get();
        }
        
        int getFailureCount() {
            return failureCount.get();
        }
        
        long getElapsedTime() {
            return System.currentTimeMillis() - startTime;
        }
        
        double getThroughput(int total) {
            long elapsed = getElapsedTime();
            return elapsed > 0 ? (completedCount.get() * 1000.0) / elapsed : 0;
        }
        
        ConcurrentHashMap<String, Long> getCompletionTimes() {
            return completionTimes;
        }
    }
    
    /**
     * Constructor
     */
    public ConcurrentReportGenerator(GradeManager gradeManager, ArrayList<Student> students, int threadCount) {
        this.gradeManager = gradeManager;
        this.students = students;
        this.threadCount = Math.max(2, Math.min(8, threadCount)); // Clamp between 2-8
        this.executorService = Executors.newFixedThreadPool(this.threadCount, new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(1);
            
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "ReportGenerator-" + counter.getAndIncrement());
                t.setDaemon(false);
                return t;
            }
        });
        this.reportTasks = Collections.synchronizedList(new ArrayList<>());
        this.progressTracker = new ProgressTracker(students.size());
        this.fileLock = new Object();
    }
    
    /**
     * Generate reports for all students concurrently
     */
    public Map<String, ReportStats> generateReportsParallel() {
        System.out.println("\n" + "═".repeat(80));
        System.out.println("🚀 CONCURRENT BATCH REPORT GENERATION");
        System.out.println("═".repeat(80));
        System.out.println(String.format("Thread Pool Size: %d | Students: %d | Start Time: %s",
                threadCount, students.size(), LocalDateTime.now().format(TIME_FORMAT)));
        System.out.println("═".repeat(80) + "\n");
        
        long overallStartTime = System.currentTimeMillis();
        Map<String, ReportStats> allStats = new ConcurrentHashMap<>();
        CountDownLatch completionLatch = new CountDownLatch(students.size());
        
        try {
            // Submit all tasks (prefer cached Student objects)
            for (Student student : students) {
                Student toUse = student;
                try {
                    Object cached = CacheManager.getInstance().getOrLoad("student:" + student.getId(), k -> student);
                    if (cached instanceof Student) toUse = (Student) cached;
                } catch (Exception ex) {
                    // ignore cache errors and fall back to original
                }
                ReportTask task = new ReportTask(toUse, completionLatch);
                reportTasks.add(task);
                executorService.submit(task);
            }

            // Wait for completion with progress display
            displayProgressLive(completionLatch, students.size());
            
            // Collect results
            for (ReportTask task : reportTasks) {
                allStats.put(task.student.getName(), task.stats);
            }
            
            // Calculate metrics
            long totalTime = System.currentTimeMillis() - overallStartTime;
            long estimatedSequentialTime = estimateSequentialTime();
            
            // Display results
            displayResults(allStats, totalTime, estimatedSequentialTime);
            
            return allStats;
            
        } catch (InterruptedException e) {
            System.err.println("❌ Report generation interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
            return allStats;
        } finally {
            shutdownThreadPool();
        }
    }
    
    /**
     * Display live progress with progress bars
     */
    private void displayProgressLive(CountDownLatch latch, int total) throws InterruptedException {
        Thread progressThread = new Thread(() -> {
            try {
                while (latch.getCount() > 0) {
                    int completed = progressTracker.getCompletedCount();
                    int failed = progressTracker.getFailureCount();
                    int remaining = total - completed - failed;
                    
                    displayProgressBar(completed, failed, remaining, total);
                    Thread.sleep(500);
                }
                // Final display
                displayProgressBar(total, 0, 0, total);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "ProgressDisplay");
        progressThread.setDaemon(true);
        progressThread.start();
        
        // Wait for actual completion
        latch.await();
        Thread.sleep(500); // Allow progress display to finish
    }
    
    /**
     * Display progress bar
     */
    private synchronized void displayProgressBar(int completed, int failed, int remaining, int total) {
        int barWidth = 50;
        int filledWidth = (int) ((double) completed / total * barWidth);
        String bar = "█".repeat(filledWidth) + "░".repeat(barWidth - filledWidth);
        double percentage = (100.0 * completed) / total;
        
        System.out.printf("\r│ Progress: [%s] %3.0f%% │ ✓ %3d │ ✗ %3d │ ⏳ %3d │ ▶ %.2f/s",
                bar, percentage, completed, failed, remaining, progressTracker.getThroughput(total));
        System.out.flush();
    }
    
    /**
     * Display final results and statistics
     */
    private void displayResults(Map<String, ReportStats> stats, long totalTime, long estimatedSequential) {
        System.out.println("\n\n" + "═".repeat(80));
        System.out.println("📊 BATCH GENERATION RESULTS");
        System.out.println("═".repeat(80));
        
        // Summary metrics
        int successful = (int) stats.values().stream().filter(s -> s.success).count();
        int failed = stats.size() - successful;
        double avgGenerationTime = stats.values().stream()
                .filter(s -> s.success)
                .mapToLong(s -> s.generationTimeMs)
                .average().orElse(0);
        
        System.out.printf("├─ Total Time: %,dms\n", totalTime);
        System.out.printf("├─ Estimated Sequential: %,dms\n", estimatedSequential);
        System.out.printf("├─ Time Saved: %,dms (%.1f%% faster)\n", 
                estimatedSequential - totalTime,
                100.0 * (estimatedSequential - totalTime) / estimatedSequential);
        System.out.printf("├─ Throughput: %.2f reports/second\n", 
                (stats.size() * 1000.0) / totalTime);
        System.out.printf("├─ Average Generation Time: %.2fms\n", avgGenerationTime);
        System.out.printf("├─ Successful: %d | Failed: %d\n", successful, failed);
        System.out.println("└─ Thread Pool Shutdown: Complete\n");
        
        // Top performers
        System.out.println("🏆 TOP 5 FASTEST REPORTS:");
        System.out.println("┌─ Rank │ Student │ Time (ms) ─────────────────────────────┐");
        stats.values().stream()
                .filter(s -> s.success)
                .sorted(Comparator.comparingLong(s -> s.generationTimeMs))
                .limit(5)
                .forEachOrdered((s) -> {
                    String bar = "█".repeat((int)(s.generationTimeMs / 10)) + 
                                "░".repeat(Math.max(0, 30 - (int)(s.generationTimeMs / 10)));
                    System.out.printf("│ %5s │ %-25s │ %5dms [%s]│\n",
                            "", s.studentName.substring(0, Math.min(20, s.studentName.length())), 
                            s.generationTimeMs, bar);
                });
        System.out.println("└─────────────────────────────────────────────────────────────┘");
        
        // Failed reports
        if (failed > 0) {
            System.out.println("\n⚠️  FAILED REPORTS:");
            stats.values().stream()
                    .filter(s -> !s.success)
                    .forEachOrdered(s -> 
                            System.out.printf("  ✗ %s (ID: %d) - %s\n", 
                                    s.studentName, s.studentId, s.errorMessage));
        }
        
        System.out.println("\n✓ All reports saved to: " + new File(REPORTS_DIR).getAbsolutePath());
        System.out.println("═".repeat(80) + "\n");
    }
    
    /**
     * Estimate sequential processing time
     */
    private long estimateSequentialTime() {
        // Average time per report based on first few completed tasks
        double avgTimePerReport = progressTracker.getCompletionTimes().values().stream()
                .limit(Math.min(5, progressTracker.getCompletionTimes().size()))
                .mapToLong(Long::longValue)
                .average()
                .orElse(100.0);
        return (long) (avgTimePerReport * students.size());
    }
    
    /**
     * Calculate student's average grade
     */
    private double calculateStudentAverage(int studentId) {
        double total = 0;
        int count = 0;
        
        for (int i = 0; i < gradeManager.getGradeCount(); i++) {
            Grade grade = gradeManager.grades[i];
            if (grade != null && grade.getStudentId() == studentId) {
                total += grade.getGrade();
                count++;
            }
        }
        
        return count > 0 ? total / count : 0;
    }
    
    /**
     * Count grades for a specific student
     */
    private int countGradesForStudent(int studentId) {
        int count = 0;
        for (int i = 0; i < gradeManager.getGradeCount(); i++) {
            Grade grade = gradeManager.grades[i];
            if (grade != null && grade.getStudentId() == studentId) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Shutdown thread pool gracefully
     */
    private void shutdownThreadPool() {
        try {
            System.out.println("⏹ Shutting down thread pool...");
            executorService.shutdown();
            
            // Wait for termination with timeout
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                System.out.println("⚠️  Force shutting down remaining tasks...");
                executorService.shutdownNow();
                
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    System.err.println("❌ Thread pool did not terminate");
                }
            }
            
            System.out.println("✓ Thread pool shutdown complete");
            
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
            System.err.println("❌ Thread pool shutdown interrupted");
        }
    }
    
    /**
     * Get executor service for monitoring
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }
    
    /**
     * Get thread count
     */
    public int getThreadCount() {
        return threadCount;
    }
}
