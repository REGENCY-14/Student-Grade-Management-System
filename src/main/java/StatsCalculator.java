import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Background thread for calculating and caching statistics
 * Thread-safe stats calculation with pause/resume functionality
 */
public class StatsCalculator extends Thread {
    
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int CALC_INTERVAL_MS = 5000; // 5 seconds
    
    private final GradeManager gradeManager;
    private final ArrayList<Student> students;
    private final ConcurrentHashMap<String, Object> statsCache;
    private final AtomicBoolean running;
    private final AtomicBoolean paused;
    private final AtomicLong cacheHits;
    private final AtomicLong cacheMisses;
    private volatile LocalDateTime lastUpdateTime;
    private volatile boolean isCalculating;
    private volatile String lastError;
    
    // Stats data structures (thread-safe)
    private volatile Map<String, Integer> gradeDistribution;
    private volatile List<StudentGradeEntry> topPerformers;
    private volatile double classAverage;
    private volatile double classGPA;
    private volatile int totalGrades;
    private volatile int totalStudents;
    
    // Thread management
    private final Thread calculatorThread;
    
    /**
     * Inner class for holding student grade information
     */
    public static class StudentGradeEntry implements Comparable<StudentGradeEntry> {
        public String studentName;
        public double average;
        public double gpa;
        public int gradeCount;
        
        public StudentGradeEntry(String name, double avg, double gpa, int count) {
            this.studentName = name;
            this.average = avg;
            this.gpa = gpa;
            this.gradeCount = count;
        }
        
        @Override
        public int compareTo(StudentGradeEntry other) {
            return Double.compare(other.average, this.average); // Descending order
        }
    }
    
    public StatsCalculator(GradeManager gradeManager, ArrayList<Student> students) {
        this.gradeManager = gradeManager;
        this.students = students;
        this.statsCache = new ConcurrentHashMap<>();
        this.running = new AtomicBoolean(true);
        this.paused = new AtomicBoolean(false);
        this.cacheHits = new AtomicLong(0);
        this.cacheMisses = new AtomicLong(0);
        this.isCalculating = false;
        this.lastError = null;
        
        // Initialize stats
        this.gradeDistribution = new ConcurrentHashMap<>();
        this.topPerformers = Collections.synchronizedList(new ArrayList<>());
        this.classAverage = 0.0;
        this.classGPA = 0.0;
        this.totalGrades = 0;
        this.totalStudents = 0;
        this.lastUpdateTime = LocalDateTime.now();
        
        // Create and start calculator thread
        this.calculatorThread = new Thread(this::calculateStats, "StatsCalculator-Daemon");
        this.calculatorThread.setDaemon(true);
        this.calculatorThread.setPriority(Thread.NORM_PRIORITY - 1); // Lower priority
    }
    
    /**
     * Start the background calculator thread
     */
    public void startCalculator() {
        if (!calculatorThread.isAlive()) {
            calculatorThread.start();
        }
    }
    
    /**
     * Main calculation loop running on background thread
     */
    private void calculateStats() {
        while (running.get()) {
            try {
                // Sleep or pause
                if (paused.get()) {
                    Thread.sleep(500); // Check pause status every 500ms
                    continue;
                }
                
                // Perform calculation
                isCalculating = true;
                long startTime = System.currentTimeMillis();
                
                performStatsCalculation();
                lastUpdateTime = LocalDateTime.now();
                
                long calcTime = System.currentTimeMillis() - startTime;
                lastError = null;
                
                // Log calculation time
                statsCache.put("last_calc_time_ms", calcTime);
                
                // Sleep until next calculation
                Thread.sleep(Math.max(100, CALC_INTERVAL_MS - calcTime));
                
            } catch (InterruptedException e) {
                if (running.get()) {
                    lastError = "Calculation interrupted: " + e.getMessage();
                }
            } catch (Exception e) {
                lastError = "Stats calculation error: " + e.getMessage();
            } finally {
                isCalculating = false;
            }
        }
    }
    
    /**
     * Perform actual statistics calculations - using Stream API exclusively
     */
    private synchronized void performStatsCalculation() {
        try {
            if (gradeManager.getGradeCount() == 0) {
                resetStats();
                return;
            }
            
            // Use Stream API to build student stats map
            Map<String, StudentStats> studentStatsMap = Arrays.stream(gradeManager.grades)
                    .limit(gradeManager.getGradeCount())
                    .filter(g -> g != null)
                    .collect(Collectors.toMap(
                            grade -> {
                                Student student = findStudentById(grade.getStudentId());
                                return student != null ? student.getName() : "Unknown-" + grade.getStudentId();
                            },
                            grade -> {
                                StudentStats stats = new StudentStats();
                                stats.addGrade(grade);
                                return stats;
                            },
                            (existing, newStats) -> {
                                // Merge: add all grades from newStats to existing
                                newStats.totalGrade = existing.totalGrade + newStats.totalGrade;
                                newStats.totalGPA = existing.totalGPA + newStats.totalGPA;
                                newStats.count = existing.count + newStats.count;
                                return newStats;
                            },
                            ConcurrentHashMap::new
                    ));
            
            // Calculate distribution using Stream API
            Map<String, Integer> distribution = Arrays.stream(gradeManager.grades)
                    .limit(gradeManager.getGradeCount())
                    .filter(g -> g != null)
                    .collect(Collectors.toMap(
                            g -> getGradeRange(g.getGrade()),
                            g -> 1,
                            Integer::sum,
                            ConcurrentHashMap::new
                    ));
            
            // Calculate totals using Stream API
            double[] totals = Arrays.stream(gradeManager.grades)
                    .limit(gradeManager.getGradeCount())
                    .filter(g -> g != null)
                    .collect(
                            () -> new double[3],  // [totalAverage, totalGPA, count]
                            (acc, grade) -> {
                                acc[0] += grade.getGrade();
                                acc[1] += convertGradeToGPA(grade.getGrade());
                                acc[2]++;
                            },
                            (acc1, acc2) -> {
                                acc1[0] += acc2[0];
                                acc1[1] += acc2[1];
                                acc1[2] += acc2[2];
                            }
                    );
            
            // Update thread-safe fields
            this.totalGrades = (int) totals[2];
            this.totalStudents = studentStatsMap.size();
            this.classAverage = totals[2] > 0 ? totals[0] / totals[2] : 0;
            this.classGPA = totals[2] > 0 ? totals[1] / totals[2] : 0;
            this.gradeDistribution = distribution;
            
            // Calculate top performers
            List<StudentGradeEntry> topPerfList = studentStatsMap.entrySet().stream()
                    .map(e -> new StudentGradeEntry(
                            e.getKey(),
                            e.getValue().getAverageGrade(),
                            e.getValue().getAverageGPA(),
                            e.getValue().getGradeCount()
                    ))
                    .sorted()
                    .limit(10)
                    .collect(Collectors.toList());
            
            this.topPerformers = Collections.synchronizedList(topPerfList);
            
            // Update cache with computed values
            statsCache.put("class_average", classAverage);
            statsCache.put("class_gpa", classGPA);
            statsCache.put("total_grades", totalGrades);
            statsCache.put("total_students", totalStudents);
            statsCache.put("grade_distribution", new ConcurrentHashMap<>(distribution));
            statsCache.put("update_time", lastUpdateTime.format(TIME_FORMAT));
            
        } catch (Exception e) {
            lastError = "Calculation failed: " + e.getMessage();
        }
    }
    
    /**
     * Find student by ID.
     * Big-O: O(1) average using HashMap-backed index (ApplicationContext.getInstance().getStudentIndex()),
     * instead of O(n) linear scan from the original lab implementation.
     */
    private Student findStudentById(int studentId) {
        if (students == null) return null;
        return ApplicationContext.getInstance().getStudentIndex().get(String.valueOf(studentId));
    }
    
    /**
     * Convert numeric grade to GPA (0-4 scale)
     */
    private double convertGradeToGPA(double grade) {
        if (grade >= 90) return 4.0;
        if (grade >= 80) return 3.0;
        if (grade >= 70) return 2.0;
        if (grade >= 60) return 1.0;
        return 0.0;
    }
    
    /**
     * Get grade range bucket (A, B, C, D, F)
     */
    private String getGradeRange(double grade) {
        if (grade >= 90) return "A (90-100)";
        if (grade >= 80) return "B (80-89)";
        if (grade >= 70) return "C (70-79)";
        if (grade >= 60) return "D (60-69)";
        return "F (0-59)";
    }
    
    /**
     * Reset stats to zero
     */
    private void resetStats() {
        this.gradeDistribution = new ConcurrentHashMap<>();
        this.topPerformers = Collections.synchronizedList(new ArrayList<>());
        this.classAverage = 0;
        this.classGPA = 0;
        this.totalGrades = 0;
        this.totalStudents = 0;
        statsCache.clear();
    }
    
    /**
     * Pause stats calculation
     */
    public void pause() {
        paused.set(true);
    }
    
    /**
     * Resume stats calculation
     */
    public void resume() {
        paused.set(false);
    }
    
    /**
     * Force immediate recalculation
     */
    public void forceRecalculate() {
        try {
            isCalculating = true;
            performStatsCalculation();
            lastUpdateTime = LocalDateTime.now();
        } finally {
            isCalculating = false;
        }
    }
    
    /**
     * Stop the calculator thread gracefully
     */
    public void stopCalculator() {
        running.set(false);
        paused.set(false);
        try {
            if (calculatorThread.isAlive()) {
                calculatorThread.join(2000); // Wait up to 2 seconds
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    // =============== Getters for Dashboard ===============
    
    public Map<String, Integer> getGradeDistribution() {
        cacheMisses.incrementAndGet();
        return new ConcurrentHashMap<>(gradeDistribution);
    }
    
    public List<StudentGradeEntry> getTopPerformers() {
        cacheMisses.incrementAndGet();
        return new ArrayList<>(topPerformers);
    }
    
    public double getClassAverage() {
        cacheMisses.incrementAndGet();
        return classAverage;
    }
    
    public double getClassGPA() {
        cacheMisses.incrementAndGet();
        return classGPA;
    }
    
    public int getTotalGrades() {
        cacheMisses.incrementAndGet();
        return totalGrades;
    }
    
    public int getTotalStudents() {
        cacheMisses.incrementAndGet();
        return totalStudents;
    }
    
    public LocalDateTime getLastUpdateTime() {
        return lastUpdateTime;
    }
    
    public boolean isRunning() {
        return running.get();
    }
    
    public boolean isPaused() {
        return paused.get();
    }
    
    public boolean isCalculating() {
        return isCalculating;
    }
    
    public String getStatus() {
        if (!running.get()) return "STOPPED";
        if (paused.get()) return "PAUSED";
        if (isCalculating) return "CALCULATING";
        return "RUNNING";
    }
    
    public long getCacheHits() {
        return cacheHits.get();
    }
    
    public long getCacheMisses() {
        return cacheMisses.get();
    }
    
    public double getCacheHitRate() {
        long total = cacheHits.get() + cacheMisses.get();
        return total > 0 ? (100.0 * cacheHits.get()) / total : 0;
    }
    
    public String getLastError() {
        return lastError;
    }
    
    public int getActiveThreadCount() {
        return Thread.activeCount();
    }
    
    public long getLastCalcTimeMs() {
        Object val = statsCache.get("last_calc_time_ms");
        return val instanceof Long ? (Long) val : 0;
    }
    
    /**
     * Inner class for tracking per-student statistics
     */
    private static class StudentStats {
        double totalGrade = 0;
        double totalGPA = 0;
        int count = 0;
        
        void addGrade(Grade grade) {
            totalGrade += grade.getGrade();
            // Convert numeric grade to GPA
            double gpa = grade.getGrade() >= 90 ? 4.0 :
                        grade.getGrade() >= 80 ? 3.0 :
                        grade.getGrade() >= 70 ? 2.0 :
                        grade.getGrade() >= 60 ? 1.0 : 0.0;
            totalGPA += gpa;
            count++;
        }
        
        double getAverageGrade() {
            return count > 0 ? totalGrade / count : 0;
        }
        
        double getAverageGPA() {
            return count > 0 ? totalGPA / count : 0;
        }
        
        int getGradeCount() {
            return count;
        }
    }
}

