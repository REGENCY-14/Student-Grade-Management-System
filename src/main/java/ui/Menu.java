package ui;

import context.ApplicationContext;
import core.Student;
import core.Grade;
import manager.GradeManager;
import manager.CacheManager;
import models.StudentService;
import models.StudentFactory;
import models.HonorsStudent;
import search.RegexSearchEngine;
import search.ConcurrentReportGenerator;
import manager.FileFormatManager;
import scheduler.ScheduledTask;
import analytics.StatisticsDashboard;
import audit.AuditLogger;
import core.Subject;
import core.CoreSubject;
import core.ElectiveSubject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import exception.FileImportException;
import exception.GradeStorageFullException;
import exception.InvalidGradeException;
import exception.InvalidReportFormatException;
import exception.InvalidStudentDataException;
import exception.StudentNotFoundException;
import exception.SubjectNotFoundException;

/**
 * Menu Service - Main controller for user interactions
 * Instance-based class that receives ApplicationContext for all data access
 * Follows Single Responsibility Principle: Delegates specific responsibilities to handlers
 * Follows Dependency Inversion Principle: depends on ApplicationContext abstraction
 */
public class Menu {
    // Instance variables instead of static
    private final ApplicationContext context;
    private final Scanner scanner;
    private final ArrayList<Student> students;
    private final GradeManager gradeManager;
    private final StudentService studentService;
    private boolean running;
    
    // Handlers for specific responsibilities (SRP)
    private final StudentMenuHandler studentHandler;
    private final GradeMenuHandler gradeHandler;
    private final FileOperationsHandler fileHandler;
    private final SearchMenuHandler searchHandler;
    private final QueryGradeHandler queryHandler;
    private final AdvancedFeaturesHandler advancedHandler;
    private final StreamProcessingHandler streamHandler;

    /**
     * Create a new Menu service with the given application context
     */
    public Menu(ApplicationContext context) {
        this.context = context;
        this.scanner = context.getScanner();
        this.students = context.getStudents();
        this.gradeManager = context.getGradeManager();
        this.studentService = context.getStudentService();
        this.running = false;
        
        // Initialize handlers for different responsibilities
        this.studentHandler = new StudentMenuHandler(context, scanner, students);
        this.gradeHandler = new GradeMenuHandler(context, scanner, students, gradeManager);
        this.fileHandler = new FileOperationsHandler(context, scanner, students, gradeManager);
        this.searchHandler = new SearchMenuHandler(context, scanner, students, gradeManager);
        this.queryHandler = new QueryGradeHandler(context, scanner, students, gradeManager);
        this.advancedHandler = new AdvancedFeaturesHandler(context, scanner, students, gradeManager);
        this.streamHandler = new StreamProcessingHandler(context, scanner, students, gradeManager);
    }

    /**
     * Start the menu loop - the main interaction point
     */
    public void start() throws StudentNotFoundException, GradeStorageFullException, InvalidGradeException, 
                                SubjectNotFoundException, InvalidStudentDataException, FileImportException, InvalidReportFormatException {
        running = true;

        while (running) {
            mainMenu();
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 1) {
                addStudent();
            } else if (choice == 2) {
                viewStudents();
            } else if (choice == 3) {
                recordGrade();
            } else if (choice == 4) {
                viewGradeReport();
            } else if (choice == 5) {
                exportGradeReport();
            } else if (choice == 6) {
                bulkImportGrades();
            } else if (choice == 7) {
                bulkImportStudents();
            } else if (choice == 8) {
                concurrentBatchReportGeneration();
            } else if (choice == 9) {
                System.out.println("Enter student ID: ");
                int studentId = scanner.nextInt();
                viewStudentGPAReport(studentId);
            } else if (choice == 10) {
                viewClassStatistics();
            } else if (choice == 11) {
                launchStatisticsDashboard();
            } else if (choice == 12) {
                searchStudents();
            } else if (choice == 13) {
                advancedPatternSearch();
            } else if (choice == 14) {
                queryGradeHistory();
            } else if (choice == 15) {
                openScheduledTasksMenu();
            } else if (choice == 16) {
                openCacheMenu();
            } else if (choice == 17) {
                openAuditMenu();
            } else if (choice == 18) {
                running = false;
                System.out.println("Thank you for using grade management system!");
                System.out.println("Goodbye");
            } else {
                System.out.println("Invalid choice. Try again!");
            }
        }
    }


    // MAIN MENU
    private void mainMenu() {
        System.out.println("============================================");
        System.out.println("||    STUDENT GRADE MANAGEMENT SYSTEM     ||");
        System.out.println("============================================");
        System.out.println("\n--- STUDENT MANAGEMENT ---");
        System.out.println("1. Add Student");
        System.out.println("2. View Students");
        System.out.println("3. Record Grade");
        System.out.println("4. View Grade Report");

        System.out.println("\n--- FILE OPERATIONS ---");
        System.out.println("5. Export Grade Report");
        System.out.println("6. Bulk Import Grades");
        System.out.println("7. Bulk Import Students");
        System.out.println("8. Concurrent Batch Report Generation");

        System.out.println("\n--- ANALYTICS AND REPORTING ---");
        System.out.println("9. Calculate Student GPA");
        System.out.println("10. View Student Statistics");
        System.out.println("11. Real-Time Statistics Dashboard");

        System.out.println("\n--- SEARCH AND QUERY ---");
        System.out.println("12. Search Students");
        System.out.println("13. Advanced Pattern Based Search");
        System.out.println("14. Query Grade History");

        System.out.println("\n--- ADVANCED FEATURES ---");
        System.out.println("15. Scheduled Automated Tasks");
        System.out.println("16. Cache Management");
        System.out.println("17. Audit Trail");

        System.out.println("18. Exit");

        System.out.print("\nEnter choice: ");
    }


    private void addStudent() {
        studentHandler.addStudent();
    }


    // VIEW STUDENTS
    private void viewStudents() {
        studentHandler.viewStudents();
    }


    // RECORD GRADE
    private void recordGrade() {
        gradeHandler.recordGrade();
    }



    // VIEW GRADE REPORT
    private void viewGradeReport() {
        gradeHandler.viewGradeReport();
    }

    // CALCULATE STUDENT GPA
    private void viewStudentGPAReport(int studentId) {
        gradeHandler.viewStudentGPAReport(studentId);
    }

    // View class statistics
    private void viewClassStatistics() {
        gradeHandler.viewClassStatistics();
    }
    
    //Export Grade Report
    private void exportGradeReport() {
        fileHandler.exportGradeReport();
    }

    //BULK IMPORT GRADES - Multi-format support (CSV, JSON, Binary)
    private void bulkImportGrades() {
        fileHandler.bulkImportGrades();
    }

// ==================== BULK IMPORT STUDENTS ====================

    private void bulkImportStudents() {
        fileHandler.bulkImportStudents();
    }


// ==================== SEARCH STUDENTS (Option 9) ====================

    private void searchStudents() {
        searchHandler.searchStudents();
    }


    // ==================== MULTI-FORMAT IMPORT/EXPORT ====================

    /**
     * Advanced multi-format import with file watching
     */
    private void advancedImportGrades() {
        FileFormatManager formatManager = new FileFormatManager();
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("ADVANCED MULTI-FORMAT IMPORT");
        System.out.println("=".repeat(70));
        System.out.println("1. Import from CSV");
        System.out.println("2. Import from JSON");
        System.out.println("3. Import from Binary");
        System.out.println("4. Watch Directory for New Imports");
        System.out.println("5. List Files by Format");
        System.out.println("6. Back to Main Menu");
        System.out.print("Enter choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        try {
            switch (choice) {
                case 1:
                    importMultiFormat(formatManager, "CSV");
                    break;
                case 2:
                    importMultiFormat(formatManager, "JSON");
                    break;
                case 3:
                    importMultiFormat(formatManager, "BINARY");
                    break;
                case 4:
                    watchDirectoryForNewFiles(formatManager);
                    break;
                case 5:
                    listFilesByFormat(formatManager);
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Advanced multi-format export with comparison
     */
    private void advancedExportGrades() {
        if (gradeManager.getGradeCount() == 0) {
            System.out.println("No grades to export.");
            return;
        }

        FileFormatManager formatManager = new FileFormatManager();

        System.out.println("\n" + "=".repeat(70));
        System.out.println("ADVANCED MULTI-FORMAT EXPORT");
        System.out.println("=".repeat(70));
        System.out.println("1. Export to CSV");
        System.out.println("2. Export to JSON");
        System.out.println("3. Export to Binary");
        System.out.println("4. Export to All Formats (with comparison)");
        System.out.println("5. Back to Main Menu");
        System.out.print("Enter choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter file name (without extension): ");
        String fileName = scanner.nextLine().trim();

        List<FileFormatManager.FileStats> statsList = new ArrayList<>();

        try {
            ArrayList<Grade> grades = new ArrayList<>();
            for (int i = 0; i < gradeManager.getGradeCount(); i++) {
                grades.add(gradeManager.grades[i]);
            }

            switch (choice) {
                case 1:
                    FileFormatManager.FileStats csvStats = formatManager.exportToCSV(grades, fileName);
                    System.out.println("\n✓ " + csvStats);
                    break;
                case 2:
                    FileFormatManager.FileStats jsonStats = formatManager.exportToJSON(grades, fileName);
                    System.out.println("\n✓ " + jsonStats);
                    break;
                case 3:
                    FileFormatManager.FileStats binStats = formatManager.exportToBinary(grades, fileName);
                    System.out.println("\n✓ " + binStats);
                    break;
                case 4:
                    statsList.add(formatManager.exportToCSV(grades, fileName + "_csv"));
                    statsList.add(formatManager.exportToJSON(grades, fileName + "_json"));
                    statsList.add(formatManager.exportToBinary(grades, fileName + "_bin"));
                    formatManager.displayFormatComparison(statsList);
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void importMultiFormat(FileFormatManager formatManager, String format) {
        System.out.print("\nEnter file name (without extension): ");
        String fileName = scanner.nextLine().trim();

        FileFormatManager.FileStats stats = null;

        try {
            switch (format.toUpperCase()) {
                case "CSV":
                    stats = formatManager.importFromCSV(fileName);
                    break;
                case "JSON":
                    stats = formatManager.importFromJSON(fileName);
                    break;
                case "BINARY":
                    stats = formatManager.importFromBinary(fileName);
                    break;
            }

            if (stats != null && stats.recordsProcessed > 0) {
                System.out.println("\n" + "=".repeat(120));
                System.out.println("IMPORT STATISTICS");
                System.out.println("=".repeat(120));
                System.out.println(stats);
                System.out.println("=".repeat(120) + "\n");
            }

        } catch (Exception e) {
            System.out.println("❌ Import Error: " + e.getMessage());
        }
    }

    private void watchDirectoryForNewFiles(FileFormatManager formatManager) {
        System.out.println("Select format to watch:");
        System.out.println("1. CSV");
        System.out.println("2. JSON");
        System.out.println("3. Binary");
        System.out.print("Enter choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        String format = switch (choice) {
            case 1 -> "csv";
            case 2 -> "json";
            case 3 -> "binary";
            default -> null;
        };

        if (format == null) {
            System.out.println("Invalid choice.");
            return;
        }

        System.out.print("Enter watch duration (seconds): ");
        long seconds = scanner.nextLong();
        scanner.nextLine();

        formatManager.watchDirectoryForImports(format, seconds);
    }

    private void listFilesByFormat(FileFormatManager formatManager) {
        System.out.println("Select format to list:");
        System.out.println("1. CSV");
        System.out.println("2. JSON");
        System.out.println("3. Binary");
        System.out.print("Enter choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        String format = switch (choice) {
            case 1 -> "csv";
            case 2 -> "json";
            case 3 -> "binary";
            default -> null;
        };

        if (format == null) {
            System.out.println("Invalid choice.");
            return;
        }

        formatManager.listFilesByFormat(format);
    }

    private void launchStatisticsDashboard() {
        advancedHandler.launchStatisticsDashboard();
    }

    private void concurrentBatchReportGeneration() {
        advancedHandler.concurrentBatchReportGeneration();
    }

    /**
     * Open scheduled tasks management menu
     */
    private void openScheduledTasksMenu() {
        advancedHandler.openScheduledTasksMenu();
    }

    private void openCacheMenu() {
        CacheManager cache = CacheManager.getInstance();
        boolean inCacheMenu = true;
        while (inCacheMenu) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("CACHE MANAGEMENT");
            System.out.println("=".repeat(50));
            System.out.println("1. Display Cache Statistics");
            System.out.println("2. Display Cache Contents");
            System.out.println("3. Clear Cache");
            System.out.println("4. Warm Cache (top N students)");
            System.out.println("5. Back to Main Menu");
            System.out.print("Select option: ");
            try {
                int choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice) {
                    case 1:
                        cache.displayStats();
                        break;
                    case 2:
                        cache.displayContents();
                        break;
                    case 3:
                        cache.clear();
                        System.out.println("Cache cleared.");
                        break;
                    case 4:
                        System.out.print("Enter number of students to warm (e.g., 50): ");
                        int n = scanner.nextInt();
                        scanner.nextLine();
                        cache.warmUpStudents(students, gradeManager, n);
                        System.out.println("Cache warmed for top " + n + " students.");
                        break;
                    case 5:
                        inCacheMenu = false;
                        break;
                    default:
                        System.out.println("Invalid option.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                scanner.nextLine();
            }
        }
    }

    private void advancedPatternSearch() {
        searchHandler.advancedPatternSearch();
    }

    private void queryGradeHistory() {
        queryHandler.queryGradeHistory();
    }

    private void openAuditMenu() {
        AuditLogger logger = AuditLogger.getInstance();
        boolean inAudit = true;
        while (inAudit) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("AUDIT TRAIL");
            System.out.println("=".repeat(50));
            System.out.println("1. View Recent Entries");
            System.out.println("2. Search Entries (date range / operation / thread)");
            System.out.println("3. Audit Statistics (date range)");
            System.out.println("4. Back to Main Menu");
            System.out.print("Select option: ");
            try {
                int choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice) {
                    case 1: {
                        System.out.print("How many recent entries to show? ");
                        int n = scanner.nextInt(); scanner.nextLine();
                        var entries = logger.readRecentEntries(n);
                        for (var e : entries) {
                            System.out.printf("%s | TID:%d | %s | %s | %dms | %s | %s\n",
                                    e.timestamp.toString(), e.threadId, e.operationType, e.userAction, e.executionTimeMs, e.success ? "OK" : "FAIL", e.message);
                        }
                        break;
                    }
                    case 2: {
                        System.out.print("Start date (yyyy-MM-dd) or blank: ");
                        String sFrom = scanner.nextLine().trim();
                        System.out.print("End date (yyyy-MM-dd) or blank: ");
                        String sTo = scanner.nextLine().trim();
                        System.out.print("Operation type (or blank): ");
                        String op = scanner.nextLine().trim();
                        System.out.print("Thread ID (or blank): ");
                        String tid = scanner.nextLine().trim();
                        java.time.Instant from = null, to = null; Long threadId = null;
                        try { if (!sFrom.isEmpty()) from = java.time.LocalDate.parse(sFrom).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant(); } catch (Exception ex) { }
                        try { if (!sTo.isEmpty()) to = java.time.LocalDate.parse(sTo).plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant(); } catch (Exception ex) { }
                        try { if (!tid.isEmpty()) threadId = Long.parseLong(tid); } catch (Exception ex) { }
                        var results = logger.searchEntries(from, to, op.isEmpty() ? null : op, threadId);
                        for (var e : results) {
                            System.out.printf("%s | TID:%d | %s | %s | %dms | %s | %s\n",
                                    e.timestamp.toString(), e.threadId, e.operationType, e.userAction, e.executionTimeMs, e.success ? "OK" : "FAIL", e.message);
                        }
                        break;
                    }
                    case 3: {
                        System.out.print("Start date (yyyy-MM-dd) or blank: ");
                        String sFrom = scanner.nextLine().trim();
                        System.out.print("End date (yyyy-MM-dd) or blank: ");
                        String sTo = scanner.nextLine().trim();
                        java.time.Instant from = null, to = null;
                        try { if (!sFrom.isEmpty()) from = java.time.LocalDate.parse(sFrom).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant(); } catch (Exception ex) { }
                        try { if (!sTo.isEmpty()) to = java.time.LocalDate.parse(sTo).plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant(); } catch (Exception ex) { }
                        var st = logger.computeStats(from, to);
                        System.out.println("Operations per hour:");
                        st.opsPerHour.forEach((hour, c) -> System.out.println(java.time.Instant.ofEpochSecond(hour).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() + " -> " + c));
                        System.out.println("Average execution time (ms): " + String.format("%.2f", st.avgExecTime));
                        break;
                    }
                    case 4:
                        inAudit = false; break;
                    default:
                        System.out.println("Invalid option.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                scanner.nextLine();
            }
        }
    }

    /**
     * Stream API based data processing lab/demo.
     * Demonstrates filtering, mapping, reducing, grouping, partitioning,
     * sequential vs parallel streams and Files.lines() processing.
     */
    private void openStreamProcessingMenu() {
        streamHandler.openStreamProcessingMenu();
    }
}



