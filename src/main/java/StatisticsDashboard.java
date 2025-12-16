import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Real-time statistics dashboard with auto-refresh
 * Thread-safe display with pause/resume controls
 */
public class StatisticsDashboard {
    
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int REFRESH_INTERVAL_MS = 1000; // 1 second refresh
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_BLUE = "\u001B[34m";
    
    private final StatsCalculator calculator;
    private final Scanner scanner;
    private final AtomicBoolean dashboardRunning;
    private volatile int displayRefreshCount;
    
    public StatisticsDashboard(GradeManager gradeManager, ArrayList<Student> students, Scanner scanner) {
        this.calculator = new StatsCalculator(gradeManager, students);
        this.scanner = scanner;
        this.dashboardRunning = new AtomicBoolean(false);
        this.displayRefreshCount = 0;
    }
    
    /**
     * Launch the interactive dashboard
     */
    public void launch() {
        // Start background calculator thread
        calculator.startCalculator();
        dashboardRunning.set(true);
        
        // Give it a moment to start calculating
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Display dashboard with control panel
        dashboardLoop();
        
        // Cleanup
        calculator.stopCalculator();
        dashboardRunning.set(false);
    }
    
    /**
     * Main dashboard loop with auto-refresh
     */
    private void dashboardLoop() {
        Thread refreshThread = new Thread(this::autoRefreshLoop, "Dashboard-Refresh");
        refreshThread.setDaemon(true);
        refreshThread.start();
        
        // Command input loop
        boolean running = true;
        while (running && dashboardRunning.get()) {
            try {
                displayControlPanel();
                System.out.print("\n" + ANSI_CYAN + "Dashboard Command: " + ANSI_RESET);
                
                if (scanner.hasNextLine()) {
                    String command = scanner.nextLine().trim().toLowerCase();
                    running = handleCommand(command);
                } else {
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                System.out.println(ANSI_RED + "Error: " + e.getMessage() + ANSI_RESET);
            }
        }
        
        dashboardRunning.set(false);
    }
    
    /**
     * Auto-refresh dashboard display
     */
    private void autoRefreshLoop() {
        while (dashboardRunning.get()) {
            try {
                Thread.sleep(REFRESH_INTERVAL_MS);
                if (dashboardRunning.get()) {
                    displayDashboard();
                }
            } catch (InterruptedException e) {
                if (dashboardRunning.get()) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    
    /**
     * Handle dashboard commands
     */
    private boolean handleCommand(String command) {
        switch (command.toLowerCase()) {
            case "r":
            case "refresh":
                calculator.forceRecalculate();
                System.out.println(ANSI_GREEN + "✓ Manual refresh triggered" + ANSI_RESET);
                return true;
                
            case "p":
            case "pause":
                calculator.pause();
                System.out.println(ANSI_YELLOW + "⏸ Stats calculation paused" + ANSI_RESET);
                return true;
                
            case "s":
            case "resume":
                calculator.resume();
                System.out.println(ANSI_GREEN + "▶ Stats calculation resumed" + ANSI_RESET);
                return true;
                
            case "c":
            case "clear":
                clearScreen();
                return true;
                
            case "h":
            case "help":
                displayHelp();
                return true;
                
            case "q":
            case "exit":
            case "quit":
                System.out.println(ANSI_YELLOW + "Exiting dashboard..." + ANSI_RESET);
                return false;
                
            default:
                System.out.println(ANSI_RED + "Unknown command. Type 'h' for help." + ANSI_RESET);
                return true;
        }
    }
    
    /**
     * Display main dashboard with all statistics
     */
    private synchronized void displayDashboard() {
        clearScreen();
        
        System.out.println(ANSI_CYAN + "╔" + "═".repeat(148) + "╗" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "║" + ANSI_RESET + " " + 
                ANSI_BLUE + "📊 REAL-TIME STATISTICS DASHBOARD" + ANSI_RESET + 
                " ".repeat(110) + ANSI_CYAN + "║" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "╠" + "═".repeat(148) + "╣" + ANSI_RESET);
        
        // Thread Status Section
        displayThreadStatus();
        
        System.out.println(ANSI_CYAN + "╠" + "═".repeat(148) + "╣" + ANSI_RESET);
        
        // Main Statistics Section
        displayMainStats();
        
        System.out.println(ANSI_CYAN + "╠" + "═".repeat(148) + "╣" + ANSI_RESET);
        
        // Grade Distribution
        displayGradeDistribution();
        
        System.out.println(ANSI_CYAN + "╠" + "═".repeat(148) + "╣" + ANSI_RESET);
        
        // Top Performers
        displayTopPerformers();
        
        System.out.println(ANSI_CYAN + "╠" + "═".repeat(148) + "╣" + ANSI_RESET);
        
        // Performance Metrics
        displayPerformanceMetrics();
        
        System.out.println(ANSI_CYAN + "╚" + "═".repeat(148) + "╝" + ANSI_RESET);
        
        displayRefreshCount++;
    }
    
    /**
     * Display thread status and control information
     */
    private void displayThreadStatus() {
        String statusIcon = calculator.getStatus().equals("RUNNING") ? "▶" :
                           calculator.getStatus().equals("PAUSED") ? "⏸" :
                           calculator.getStatus().equals("CALCULATING") ? "⟳" : "⏹";
        
        String statusColor = calculator.getStatus().equals("RUNNING") ? ANSI_GREEN :
                            calculator.getStatus().equals("PAUSED") ? ANSI_YELLOW :
                            calculator.getStatus().equals("CALCULATING") ? ANSI_CYAN : ANSI_RED;
        
        System.out.printf(ANSI_CYAN + "║ " + ANSI_RESET + "%-20s %s%-15s%s | %-25s | %-20s | %-20s%s\n",
                "Status:",
                statusColor, statusIcon + " " + calculator.getStatus(), ANSI_RESET,
                "Last Update: " + calculator.getLastUpdateTime().format(TIME_FORMAT),
                "Active Threads: " + calculator.getActiveThreadCount(),
                "Calculations: " + displayRefreshCount, "");
        
        // Calculating indicator
        if (calculator.isCalculating()) {
            System.out.printf(ANSI_CYAN + "║ " + ANSI_RESET + ANSI_YELLOW + "🔄 CALCULATING..." + ANSI_RESET + 
                    " (Last calc: %dms)%s\n", 
                    calculator.getLastCalcTimeMs(), " ".repeat(110));
        }
        
        // Error display
        if (calculator.getLastError() != null && !calculator.getLastError().isEmpty()) {
            System.out.printf(ANSI_CYAN + "║ " + ANSI_RESET + ANSI_RED + "⚠ Error: %s" + ANSI_RESET + "%s\n",
                    calculator.getLastError(), " ".repeat(120 - calculator.getLastError().length()));
        }
    }
    
    /**
     * Display main statistics
     */
    private void displayMainStats() {
        System.out.printf(ANSI_CYAN + "║ " + ANSI_RESET + 
                ANSI_GREEN + "CLASS STATISTICS" + ANSI_RESET + "%s\n", " ".repeat(130));
        
        System.out.printf(ANSI_CYAN + "║" + ANSI_RESET);
        System.out.printf(" %-30s: %8.2f   |", "Class Average Grade", calculator.getClassAverage());
        System.out.printf(" %-30s: %8.2f   |", "Class Average GPA", calculator.getClassGPA());
        System.out.printf(" %-30s: %8d   |", "Total Grades", calculator.getTotalGrades());
        System.out.printf(" %-30s: %8d%s\n", "Total Students", calculator.getTotalStudents(), " ".repeat(5));
    }
    
    /**
     * Display grade distribution as ASCII bar chart
     */
    private void displayGradeDistribution() {
        System.out.printf(ANSI_CYAN + "║ " + ANSI_RESET + 
                ANSI_GREEN + "GRADE DISTRIBUTION" + ANSI_RESET + "%s\n", " ".repeat(126));
        
        Map<String, Integer> distribution = calculator.getGradeDistribution();
        
        if (distribution.isEmpty()) {
            System.out.printf(ANSI_CYAN + "║ " + ANSI_RESET + "No grade data available%s\n", 
                    " ".repeat(125));
            return;
        }
        
        int maxCount = distribution.values().stream().max(Integer::compare).orElse(1);
        int barWidth = 40;
        
        distribution.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach(entry -> {
                    String gradeRange = entry.getKey();
                    int count = entry.getValue();
                    int barLength = (int) ((double) count / maxCount * barWidth);
                    String bar = "█".repeat(Math.max(1, barLength)) + 
                                "░".repeat(Math.max(0, barWidth - barLength));
                    
                    System.out.printf(ANSI_CYAN + "║ " + ANSI_RESET + 
                            "%-12s [%s] %3d (%.1f%%)%s\n",
                            gradeRange, bar, count,
                            (100.0 * count / calculator.getTotalGrades()),
                            " ".repeat(90));
                });
    }
    
    /**
     * Display top 10 performers
     */
    private void displayTopPerformers() {
        System.out.printf(ANSI_CYAN + "║ " + ANSI_RESET + 
                ANSI_GREEN + "TOP 10 PERFORMERS" + ANSI_RESET + "%s\n", " ".repeat(128));
        
        List<StatsCalculator.StudentGradeEntry> topPerformers = calculator.getTopPerformers();
        
        if (topPerformers.isEmpty()) {
            System.out.printf(ANSI_CYAN + "║ " + ANSI_RESET + "No student data available%s\n", 
                    " ".repeat(125));
            return;
        }
        
        System.out.printf(ANSI_CYAN + "║ " + ANSI_RESET + 
                "%-3s %-25s %-12s %-8s %-10s%s\n",
                "Rank", "Student Name", "Avg Grade", "GPA", "Grades", "");
        
        for (int i = 0; i < topPerformers.size(); i++) {
            StatsCalculator.StudentGradeEntry entry = topPerformers.get(i);
            String rankColor = i < 3 ? ANSI_YELLOW : "";
            String resetColor = i < 3 ? ANSI_RESET : "";
            
            System.out.printf(ANSI_CYAN + "║ " + ANSI_RESET + 
                    "%s%2d.%s %-25s %8.2f   %6.2f   %8d%s\n",
                    rankColor, (i + 1), resetColor,
                    entry.studentName,
                    entry.average,
                    entry.gpa,
                    entry.gradeCount,
                    " ".repeat(80));
        }
    }
    
    /**
     * Display performance and cache metrics
     */
    private void displayPerformanceMetrics() {
        System.out.printf(ANSI_CYAN + "║ " + ANSI_RESET + 
                ANSI_GREEN + "PERFORMANCE METRICS" + ANSI_RESET + "%s\n", " ".repeat(126));
        
        long hits = calculator.getCacheHits();
        long misses = calculator.getCacheMisses();
        double hitRate = calculator.getCacheHitRate();
        long calcTime = calculator.getLastCalcTimeMs();
        
        System.out.printf(ANSI_CYAN + "║" + ANSI_RESET);
        System.out.printf(" %-28s: %8d   |", "Cache Hits", hits);
        System.out.printf(" %-28s: %8d   |", "Cache Misses", misses);
        System.out.printf(" %-28s: %7.2f%%   |", "Cache Hit Rate", hitRate);
        System.out.printf(" %-28s: %6dms%s\n", "Last Calc Time", calcTime, " ".repeat(5));
    }
    
    /**
     * Display control panel with available commands
     */
    private void displayControlPanel() {
        System.out.println(ANSI_CYAN + "\n┌─ DASHBOARD CONTROLS ─────────────────────────────────────────┐" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "│" + ANSI_RESET + 
                ANSI_GREEN + " [R]efresh" + ANSI_RESET + " - Force immediate recalculation" + 
                ANSI_CYAN + "                  │" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "│" + ANSI_RESET + 
                ANSI_YELLOW + " [P]ause" + ANSI_RESET + "   - Pause stats calculation" + 
                ANSI_CYAN + "                        │" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "│" + ANSI_RESET + 
                ANSI_GREEN + " [S]esume" + ANSI_RESET + "  - Resume stats calculation" + 
                ANSI_CYAN + "                      │" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "│" + ANSI_RESET + 
                ANSI_BLUE + " [C]lear" + ANSI_RESET + "   - Clear screen" + 
                ANSI_CYAN + "                               │" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "│" + ANSI_RESET + 
                ANSI_BLUE + " [H]elp" + ANSI_RESET + "    - Show help information" + 
                ANSI_CYAN + "                         │" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "│" + ANSI_RESET + 
                ANSI_RED + " [Q]uit" + ANSI_RESET + "    - Exit dashboard" + 
                ANSI_CYAN + "                              │" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "└───────────────────────────────────────────────────────────────┘" + ANSI_RESET);
    }
    
    /**
     * Display help information
     */
    private void displayHelp() {
        clearScreen();
        System.out.println(ANSI_CYAN + "╔" + "═".repeat(76) + "╗" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "║" + ANSI_RESET + " " + 
                ANSI_BLUE + "DASHBOARD HELP" + ANSI_RESET + " ".repeat(60) + ANSI_CYAN + "║" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "╠" + "═".repeat(76) + "╣" + ANSI_RESET);
        
        String[][] help = {
            {"[R] or refresh", "Force immediate stats recalculation"},
            {"[P] or pause", "Pause background stats calculation"},
            {"[S] or resume", "Resume paused stats calculation"},
            {"[C] or clear", "Clear the screen and redraw dashboard"},
            {"[H] or help", "Display this help information"},
            {"[Q] or quit", "Exit the dashboard and return to menu"},
            {"", ""},
            {"Features:", ""},
            {"• Auto-refresh every 1 second", "Dashboard updates automatically"},
            {"• Background thread", "Calculates stats every 5 seconds"},
            {"• Thread-safe collections", "No data corruption or race conditions"},
            {"• Performance metrics", "Monitor cache hit rate and calc time"},
            {"• Live statistics", "Grade distribution, averages, top performers"},
            {"• Active thread monitoring", "See current thread count"},
        };
        
        for (String[] line : help) {
            if (line[0].isEmpty()) {
                System.out.println(ANSI_CYAN + "║" + ANSI_RESET);
            } else {
                System.out.printf(ANSI_CYAN + "║ " + ANSI_RESET + "%-25s %s%s\n",
                        line[0], line[1], " ".repeat(Math.max(0, 48 - line[1].length())));
            }
        }
        
        System.out.println(ANSI_CYAN + "╚" + "═".repeat(76) + "╝" + ANSI_RESET);
        System.out.print(ANSI_CYAN + "Press Enter to return to dashboard..." + ANSI_RESET);
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
    }
    
    /**
     * Clear screen (cross-platform)
     */
    private void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // Fallback: print empty lines
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }
}
