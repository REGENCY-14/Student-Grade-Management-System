package ui;

import context.ApplicationContext;
import core.Student;
import core.Grade;
import manager.GradeManager;
import manager.CacheManager;
import models.StudentService;
import models.StudentFactory;
import search.RegexSearchEngine;
import search.ConcurrentReportGenerator;
import manager.FileFormatManager;
import scheduler.ScheduledTask;
import analytics.StatisticsDashboard;
import audit.AuditLogger;
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
 * Menu Service - Provides user interface and handles user interactions
 * Instance-based class that receives ApplicationContext for all data access
 * Follows Single Responsibility Principle: UI/Menu logic only
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
        boolean valid = false;
        while (!valid) {
            try {
                long start = System.currentTimeMillis();
                System.out.println("-------------- ADD STUDENT ----------------");

                System.out.print("Enter student name: ");
                String name = scanner.nextLine();

                System.out.print("Enter student age: ");
                int age = scanner.nextInt();
                scanner.nextLine();
                if (age < 5 || age > 100) {
                    throw new InvalidStudentDataException("Age must be between 5 and 100. Received: " + age);
                }

                System.out.print("Enter student email: ");
                String email = scanner.nextLine();
                if (!email.contains("@") || !email.contains(".")) {
                    throw new InvalidStudentDataException("Invalid email format: " + email);
                }

                System.out.print("Enter student phone: ");
                String phone = scanner.nextLine();
                if (phone.length() < 10) {
                    throw new InvalidStudentDataException("Phone number must be at least 10 digits.");
                }

                System.out.println("Select student type:");
                System.out.println("1. Regular Student");
                System.out.println("2. Honors Student");
                System.out.print("Enter choice: ");
                int type = scanner.nextInt();
                scanner.nextLine();

                int id = context.generateStudentId();

                Student newStudent = StudentFactory.createStudent(type, id, name, age, email, phone);
                students.add(newStudent); // O(1) append
                context.getStudentIndex().put(String.valueOf(newStudent.getId()), newStudent); // O(1) average
                // cache newly added student
                try {
                    CacheManager.getInstance().put("student:" + newStudent.getId(), newStudent);
                } catch (Exception ex) {
                    // ignore cache errors
                }
                System.out.println(newStudent.getType() + " student added!");
                long exec = System.currentTimeMillis() - start;
                try { AuditLogger.getInstance().log("ADD_STUDENT", "id=" + newStudent.getId() + ",name=" + newStudent.getName(), exec, true, ""); } catch (Exception ex) { }


                System.out.println("--------------------------------------------");
                valid = true; // exit loop if successful

            } catch (InvalidStudentDataException e) {
                System.out.println("Error: " + e.getMessage());
                System.out.println("Please try again.\n");
                try { AuditLogger.getInstance().log("ADD_STUDENT", "input_validation", 0, false, e.getMessage()); } catch (Exception ex) { }
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
                System.out.println("Please try again.\n");
                scanner.nextLine(); // consume invalid input
                try { AuditLogger.getInstance().log("ADD_STUDENT", "exception", 0, false, e.getMessage()); } catch (Exception ex) { }
            }
        }
    }


    // VIEW STUDENTS
    private void viewStudents() {
        if (students.isEmpty()) {
            System.out.println("\nNo students have been added to the system.\n");
            return;
        }

        System.out.println("\nSTUDENT LIST");
        System.out.printf("%-6s %-20s %-10s %-12s %-10s %-10s %-12s\n",
                "ID", "NAME", "TYPE", "AVG GRADE", "SUBJECTS", "STATUS", "PASSING GRADE");
        System.out.println("----------------------------------------------------------------------------------------");

        double totalGrades = 0;
        int countGrades = 0;

        int displayCount = Math.min(students.size(), 5);
        for (int i = 0; i < displayCount; i++) {
            Student s = students.get(i);
            double avg = s.getAverageGrade();

            if (avg > 0) {
                totalGrades += avg;
                countGrades++;
            }

            System.out.printf("%-6d %-20s %-10s %-12.2f %-10d %-10s %-12d\n",
                    s.id,
                    s.name,
                    s.getType(),
                    avg,
                    s.getEnrolledSubjects(),
                    s.getStatus(),
                    s.getPassingGrade()
            );

            if (s instanceof HonorsStudent && avg >= s.getPassingGrade()) {
                System.out.println("Honors Eligible!");
            }
        }

        System.out.println("----------------------------------------------------------------------------------------");

        System.out.println("Total Students: " + students.size());

        if (countGrades > 0) {
            System.out.printf("Class Average Grade: %.2f\n", (totalGrades / countGrades));
        } else {
            System.out.println("Class Average Grade: N/A");
        }

        System.out.println();
    }


    // RECORD GRADE
    private void recordGrade() {
        boolean valid = false;
        while (!valid) {
            try {
                System.out.println("------------- RECORD GRADE ----------------");

                System.out.print("Enter student ID: ");
                int id = scanner.nextInt();
                scanner.nextLine();

                Student selected = null;
                for (Student s : students) {
                    if (s.id == id) {
                        selected = s;
                        break;
                    }
                }
                if (selected == null) {
                    throw new StudentNotFoundException("Student not found!");
                }

                System.out.println("Select subject category:");
                System.out.println("1. Core Subject");
                System.out.println("2. Elective Subject");
                System.out.print("Enter choice: ");
                int type = scanner.nextInt();
                scanner.nextLine();

                Subject subject = null;
                if (type == 1) {
                    System.out.println("Select Core Subject:");
                    System.out.println("1. Mathematics");
                    System.out.println("2. English");
                    System.out.println("3. Science");
                    int choice = scanner.nextInt();
                    scanner.nextLine();
                    if (choice == 1) subject = new CoreSubject("Mathematics", "C-MATH");
                    else if (choice == 2) subject = new CoreSubject("English", "C-ENG");
                    else if (choice == 3) subject = new CoreSubject("Science", "C-SCI");
                    else throw new SubjectNotFoundException("Invalid subject selection: " + choice);
                } else if (type == 2) {
                    System.out.println("Select Elective Subject:");
                    System.out.println("1. Music");
                    System.out.println("2. Art");
                    System.out.println("3. Physical Education");
                    int choice = scanner.nextInt();
                    scanner.nextLine();
                    if (choice == 1) subject = new ElectiveSubject("Music", "E-MUS");
                    else if (choice == 2) subject = new ElectiveSubject("Art", "E-ART");
                    else if (choice == 3) subject = new ElectiveSubject("Physical Education", "E-PE");
                    else throw new SubjectNotFoundException("Invalid subject selection: " + choice);
                } else {
                    throw new SubjectNotFoundException("Invalid category choice: " + type);
                }

                System.out.print("Enter grade (0 - 100): ");
                double g = scanner.nextDouble();
                scanner.nextLine();
                if (g < 0 || g > 100) {
                    throw new InvalidGradeException("Grade must be between 0 and 100. Received: " + g);
                }

                Grade newGrade = new Grade(id, subject, g);
                gradeManager.addGrade(newGrade);
                System.out.println("\nGrade recorded successfully!");
                newGrade.displayGradeDetails();

                valid = true; // exit loop if successful

            } catch (StudentNotFoundException | SubjectNotFoundException | InvalidGradeException e) {
                System.out.println("Error: " + e.getMessage());
                System.out.println("Please try again.\n");
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
                System.out.println("Please try again.\n");
                scanner.nextLine(); // consume invalid input
            }
        }
    }



    // VIEW GRADE REPORT
    private void viewGradeReport() {
        System.out.print("Enter student ID: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        long start = System.currentTimeMillis();
        try {
            gradeManager.viewGradeByStudent(id);
            long exec = System.currentTimeMillis() - start;
            try { AuditLogger.getInstance().log("VIEW_GRADE_REPORT", "studentId=" + id, exec, true, ""); } catch (Exception ex) { }
        } catch (StudentNotFoundException e) {
            long exec = System.currentTimeMillis() - start;
            try { AuditLogger.getInstance().log("VIEW_GRADE_REPORT", "studentId=" + id, exec, false, e.getMessage()); } catch (Exception ex) { }
            System.out.println("Student not found!");
        }
    }

    // CALCULATE STUDENT GPA
    private void viewStudentGPAReport(int studentId) {
        Student student = null;
        for (Student s : students) {
            if (s.id == studentId) {
                student = s;
                break;
            }
        }

        if (student == null) {
            System.out.println("Student not found!");
            return;
        }

        // Table Header
        System.out.println("\nGPA REPORT FOR " + student.name);
        System.out.printf("%-20s %-10s %-10s\n", "SUBJECT", "GRADE", "GPA POINTS");
        System.out.println("----------------------------------------");

        double totalGPA = 0.0;
        int count = 0;

        // Iterate over all grades in gradeManager
        for (int i = 0; i < gradeManager.getGradeCount(); i++) {
            Grade g = gradeManager.grades[i];
            if (g.getStudentId() == studentId) {
                double gpaPoints = student.gradeToGPA((int) g.getGrade());
                System.out.printf("%-20s %-10.2f %-10.2f\n",
                        g.getSubject().getSubjectName(),
                        g.getGrade(),
                        gpaPoints);
                totalGPA += gpaPoints;
                count++;
            }
        }

        if (count == 0) {
            System.out.println("No grades recorded for this student.");
            return;
        }

        double cumulativeGPA = totalGPA / count;

        // Letter Grade
        String letterGrade;
        if (cumulativeGPA >= 4.0) letterGrade = "A";
        else if (cumulativeGPA >= 3.0) letterGrade = "B";
        else if (cumulativeGPA >= 2.0) letterGrade = "C";
        else if (cumulativeGPA >= 1.0) letterGrade = "D";
        else letterGrade = "F";

        // Rank
        ArrayList<Student> rankedStudents = new ArrayList<>(students);
        rankedStudents.sort((a, b) -> Double.compare(b.computeGPA(), a.computeGPA()));
        int rank = 1;
        for (Student s : rankedStudents) {
            if (s.id == studentId) break;
            rank++;
        }

        // Display Summary
        System.out.println("----------------------------------------");
        System.out.printf("CUMULATIVE GPA: %.2f\n", cumulativeGPA);
        System.out.println("LETTER GRADE: " + letterGrade);
        System.out.println("CLASS RANK: " + rank + "/" + students.size());
        System.out.println("----------------------------------------\n");
    }

    // View class statistics
    private void viewClassStatistics() {
        if (students.isEmpty() || gradeManager.getGradeCount() == 0) {
            System.out.println("No students or grades recorded in the system yet.");
            return;
        }

        System.out.println("===================================");
        System.out.println("||       CLASS STATISTICS        ||");
        System.out.println("===================================\n");

        // 1️⃣ General Info
        System.out.println("Total Students: " + students.size());
        System.out.println("Total Grades Recorded: " + gradeManager.getGradeCount() + "\n");

        // 2️⃣ Grade Distribution
        int[] gradeCounts = new int[5]; // A, B, C, D, F
        for (int i = 0; i < gradeManager.getGradeCount(); i++) {
            double grade = gradeManager.grades[i].getGrade();
            if (grade >= 90) gradeCounts[0]++;
            else if (grade >= 80) gradeCounts[1]++;
            else if (grade >= 70) gradeCounts[2]++;
            else if (grade >= 60) gradeCounts[3]++;
            else gradeCounts[4]++;
        }

        System.out.println("GRADE DISTRIBUTION");
        String[] letters = {"A", "B", "C", "D", "F"};
        int totalGrades = gradeManager.getGradeCount();
        for (int i = 0; i < 5; i++) {
            int barLength = (int) ((gradeCounts[i] / (double) totalGrades) * 40);
            String bar = "█".repeat(barLength);
            double percent = (gradeCounts[i] * 100.0) / totalGrades;
            System.out.printf("%-5s: %-40s %5.1f%% (%d grades)\n",
                    letters[i], bar, percent, gradeCounts[i]);
        }
        System.out.println();

        // 3️⃣ Statistical Analysis
        ArrayList<Double> allGrades = new ArrayList<>();
        double sum = 0;
        double max = -1;
        double min = 101;
        Grade maxGradeObj = null;
        Grade minGradeObj = null;

        for (int i = 0; i < gradeManager.getGradeCount(); i++) {
            Grade g = gradeManager.grades[i];
            double grade = g.getGrade();
            allGrades.add(grade);
            sum += grade;

            if (grade > max) {
                max = grade;
                maxGradeObj = g;
            }
            if (grade < min) {
                min = grade;
                minGradeObj = g;
            }
        }

        allGrades.sort(Double::compare);

        double mean = sum / allGrades.size();
        double median = allGrades.size() % 2 == 0 ?
                (allGrades.get(allGrades.size() / 2 - 1) + allGrades.get(allGrades.size() / 2)) / 2.0 :
                allGrades.get(allGrades.size() / 2);

        // Mode
        double mode = allGrades.get(0);
        int maxCount = 1;
        for (int i = 0; i < allGrades.size(); i++) {
            int count = 0;
            for (double g : allGrades) {
                if (g == allGrades.get(i)) count++;
            }
            if (count > maxCount) {
                maxCount = count;
                mode = allGrades.get(i);
            }
        }

        // Standard deviation
        double variance = 0;
        for (double g : allGrades) {
            variance += Math.pow(g - mean, 2);
        }
        variance /= allGrades.size();
        double stdDev = Math.sqrt(variance);
        double range = max - min;

        System.out.println("STATISTICAL ANALYSIS");
        System.out.printf("Mean: %.2f\n", mean);
        System.out.printf("Median: %.2f\n", median);
        System.out.printf("Mode: %.2f\n", mode);
        System.out.printf("Standard Deviation: %.2f\n", stdDev);
        System.out.printf("Range: %.2f\n", range);
        if (maxGradeObj != null) System.out.printf("Highest Grade: %.2f (Student ID: %d, Subject: %s)\n",
                maxGradeObj.getGrade(), maxGradeObj.getStudentId(), maxGradeObj.getSubject().getSubjectName());
        if (minGradeObj != null) System.out.printf("Lowest Grade: %.2f (Student ID: %d, Subject: %s)\n",
                minGradeObj.getGrade(), minGradeObj.getStudentId(), minGradeObj.getSubject().getSubjectName());

        System.out.println();

        // 4️⃣ Subject Performance
        System.out.println("SUBJECT PERFORMANCE");
        double coreSum = 0, electiveSum = 0;
        int coreCount = 0, electiveCount = 0;

        ArrayList<String> subjects = new ArrayList<>();
        ArrayList<Double> subjectTotals = new ArrayList<>();
        ArrayList<Integer> subjectCounts = new ArrayList<>();

        for (int i = 0; i < gradeManager.getGradeCount(); i++) {
            Grade g = gradeManager.grades[i];
            if (g.getSubject().getSubjectType().equals("Core")) {
                coreSum += g.getGrade();
                coreCount++;
            } else {
                electiveSum += g.getGrade();
                electiveCount++;
            }

            String subjName = g.getSubject().getSubjectName();
            if (!subjects.contains(subjName)) {
                subjects.add(subjName);
                subjectTotals.add(g.getGrade());
                subjectCounts.add(1);
            } else {
                int index = subjects.indexOf(subjName);
                subjectTotals.set(index, subjectTotals.get(index) + g.getGrade());
                subjectCounts.set(index, subjectCounts.get(index) + 1);
            }
        }

        System.out.printf("Average Core Subjects: %.2f\n", coreCount > 0 ? coreSum / coreCount : 0);
        System.out.printf("Average Elective Subjects: %.2f\n", electiveCount > 0 ? electiveSum / electiveCount : 0);
        System.out.println("Individual Subject Averages:");
        for (int i = 0; i < subjects.size(); i++) {
            System.out.printf("%-20s : %.2f\n", subjects.get(i), subjectTotals.get(i) / subjectCounts.get(i));
        }
        System.out.println();

        // 5️⃣ Compare Student Types
        int regularCount = 0, honorsCount = 0;
        double regularSum = 0, honorsSum = 0;

        for (Student s : students) {
            if (s instanceof HonorsStudent) {
                honorsCount++;
                honorsSum += s.getAverageGrade();
            } else {
                regularCount++;
                regularSum += s.getAverageGrade();
            }
        }

        System.out.println("STUDENT TYPE COMPARISON");
        System.out.printf("Regular Students: %d, Average: %.2f\n", regularCount, regularCount > 0 ? regularSum / regularCount : 0);
        System.out.printf("Honors Students: %d, Average: %.2f\n", honorsCount, honorsCount > 0 ? honorsSum / honorsCount : 0);

        System.out.println("===================================\n");
    }
    
    //Export Grade Report
    private void exportGradeReport() {
        boolean valid = false;
        while (!valid) {
            try {
                System.out.print("Enter student ID: ");
                int id = scanner.nextInt();
                scanner.nextLine();

                Student selected = null;
                for (Student s : students) {
                    if (s.id == id) {
                        selected = s;
                        break;
                    }
                }

                if (selected == null) {
                    System.out.println("Student not found!");
                    continue; // retry
                }

                System.out.println("\nChoose export format:");
                System.out.println("1. Summary Report");
                System.out.println("2. Detailed Report");
                System.out.println("3. Both");
                System.out.print("Enter choice: ");
                int choice = scanner.nextInt();
                scanner.nextLine();

                if (choice < 1 || choice > 3) {
                    System.out.println("Invalid export format choice! Try again.\n");
                    continue; // retry
                }

                System.out.print("Enter file name (without extension): ");
                String fileName = scanner.nextLine().trim();

                StringBuilder summaryContent = new StringBuilder();
                StringBuilder detailedContent = new StringBuilder();

                if (choice == 1 || choice == 3) {
                    summaryContent.append("STUDENT SUMMARY REPORT\n");
                    summaryContent.append("Name: ").append(selected.name).append("\n");
                    summaryContent.append("Type: ").append(selected.getType()).append("\n");
                    summaryContent.append("Total Grades: ").append(gradeManager.getSubjectCountForStudent(id)).append("\n\n");
                }

                if (choice == 2 || choice == 3) {
                    detailedContent.append("DETAILED GRADE REPORT\n");
                    detailedContent.append("Name: ").append(selected.name).append("\n");
                    detailedContent.append("Type: ").append(selected.getType()).append("\n\n");

                    detailedContent.append(String.format("%-20s %-10s\n", "SUBJECT", "GRADE"));
                    detailedContent.append("------------------------------------\n");

                    for (int i = 0; i < gradeManager.getGradeCount(); i++) {
                        Grade g = gradeManager.grades[i];
                        if (g.getStudentId() == id) {
                            detailedContent.append(String.format("%-20s %.2f\n",
                                    g.getSubject().getSubjectName(), g.getGrade()));
                        }
                    }
                }

                if (choice == 1 || choice == 3) {
                    File file = new File(fileName + "_summary.txt");
                    try (FileWriter writer = new FileWriter(file)) {
                        writer.write(summaryContent.toString());
                    }
                    printExportInfo(file, "Summary");
                }
                if (choice == 2 || choice == 3) {
                    File file = new File(fileName + "_detailed.txt");
                    try (FileWriter writer = new FileWriter(file)) {
                        writer.write(detailedContent.toString());
                    }
                    printExportInfo(file, "Detailed");
                }

                valid = true; // exit loop if successful

            } catch (Exception e) {
                System.out.println("Error exporting report: " + e.getMessage());
                System.out.println("Please try again.\n");
                scanner.nextLine(); // consume invalid input
            }
        }
    }


    private void printExportInfo(File file, String type) {
        System.out.println("\nReport exported successfully!");
        System.out.println("File Name: " + file.getName());
        System.out.println("Location: " + file.getAbsolutePath());
        System.out.println("Size: " + file.length() + " bytes");
        System.out.println("What file contains: " + type + " Report\n");
    }

    //BULK IMPORT GRADES - Multi-format support (CSV, JSON, Binary)
    private void bulkImportGrades() {
        boolean valid = false;
        while (!valid) {
            try {
                long start = System.currentTimeMillis();
                System.out.println("------------- BULK IMPORT GRADES ----------------");
                
                // Select format
                System.out.println("\nSelect import format:");
                System.out.println("1. CSV");
                System.out.println("2. JSON");
                System.out.println("3. Binary");
                System.out.print("Enter choice: ");
                int formatChoice = scanner.nextInt();
                scanner.nextLine();
                
                String format;
                switch (formatChoice) {
                    case 1:
                        format = "CSV";
                        break;
                    case 2:
                        format = "JSON";
                        break;
                    case 3:
                        format = "BINARY";
                        break;
                    default:
                        System.out.println("Invalid format choice!");
                        continue;
                }

                System.out.print("Enter file name (without extension): ");
                String fileName = scanner.nextLine().trim();
                
                FileFormatManager formatManager = new FileFormatManager();
                FileFormatManager.FileStats stats = null;
                
                try {
                    switch (format) {
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
                    
                    if (stats != null) {
                        System.out.println("\n" + "=".repeat(80));
                        System.out.println("IMPORT SUMMARY");
                        System.out.println("=".repeat(80));
                        System.out.println("Format: " + format);
                        System.out.println("File: " + stats.fileName);
                        System.out.println("File Size: " + stats.fileSize + " bytes");
                        System.out.println("Records Processed: " + stats.recordsProcessed);
                        System.out.println("Successfully Imported: " + stats.successCount);
                        System.out.println("Failed: " + stats.failureCount);
                        System.out.println("Read Time: " + stats.readTime + " ms");
                        System.out.println("=".repeat(80));
                        System.out.println("Import completed!");
                        
                        long exec = System.currentTimeMillis() - start;
                        try { 
                            AuditLogger.getInstance().log("BULK_IMPORT_" + format, 
                                "file=" + stats.fileName + ",total=" + stats.recordsProcessed + 
                                ",success=" + stats.successCount + ",failed=" + stats.failureCount, 
                                exec, true, ""); 
                        } catch (Exception ex) { }
                    }
                    
                    valid = true; // exit loop if successful
                    
                } catch (Exception e) {
                    System.out.println("❌ Import Error: " + e.getMessage());
                    System.out.println("Please try again.\n");
                }

            } catch (Exception e) {
                System.out.println("Error during import: " + e.getMessage());
                System.out.println("Please try again.\n");
                scanner.nextLine(); // consume invalid input
            }
        }
    }


// ==================== BULK IMPORT STUDENTS ====================

    private void bulkImportStudents() {
        boolean valid = false;
        while (!valid) {
            try {
                long start = System.currentTimeMillis();
                System.out.println("------------ BULK IMPORT STUDENTS ----------------");

                System.out.print("Enter file name (without extension): ");
                String fileName = scanner.nextLine().trim();
                String filePath = "./imports/" + fileName + ".csv";

                File file = new File(filePath);
                if (!file.exists()) {
                    System.out.println("File not found: " + filePath);
                    continue; // retry
                }

                System.out.println("\nProcessing students ...\n");

                int totalRows = 0, successCount = 0, failedCount = 0;
                ArrayList<String> failedRecords = new ArrayList<>();

                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    int rowNum = 0;
                    boolean isHeader = true;
                    
                    while ((line = br.readLine()) != null) {
                        // Skip header row
                        if (isHeader) {
                            isHeader = false;
                            continue;
                        }
                        
                        totalRows++;
                        String[] parts = line.split(",");
                        if (parts.length != 5) {
                            failedRecords.add("Row " + (rowNum + 1) + ": Invalid column count (expected 5, got " + parts.length + ")");
                            failedCount++;
                            rowNum++;
                            continue;
                        }

                        String name = parts[0].trim();
                        String ageStr = parts[1].trim();
                        String email = parts[2].trim();
                        String phone = parts[3].trim();
                        String studentType = parts[4].trim();

                        // Validate name
                        if (name.isEmpty() || name.length() > 100) {
                            failedRecords.add("Row " + (rowNum + 1) + ": Invalid name → " + name);
                            failedCount++;
                            rowNum++;
                            continue;
                        }

                        // Validate age
                        int age;
                        try {
                            age = Integer.parseInt(ageStr);
                            if (age < 5 || age > 100) {
                                throw new NumberFormatException("Age out of range");
                            }
                        } catch (Exception e) {
                            failedRecords.add("Row " + (rowNum + 1) + ": Invalid age → " + ageStr);
                            failedCount++;
                            rowNum++;
                            continue;
                        }

                        // Validate email
                        if (!email.contains("@") || !email.contains(".")) {
                            failedRecords.add("Row " + (rowNum + 1) + ": Invalid email format → " + email);
                            failedCount++;
                            rowNum++;
                            continue;
                        }

                        // Validate phone
                        if (phone.length() < 10 || phone.length() > 20) {
                            failedRecords.add("Row " + (rowNum + 1) + ": Invalid phone length → " + phone);
                            failedCount++;
                            rowNum++;
                            continue;
                        }

                        // Validate and parse student type
                        int type;
                        if (studentType.equalsIgnoreCase("Regular Student") || studentType.equalsIgnoreCase("1")) {
                            type = 1;
                        } else if (studentType.equalsIgnoreCase("Honors Student") || studentType.equalsIgnoreCase("2")) {
                            type = 2;
                        } else {
                            failedRecords.add("Row " + (rowNum + 1) + ": Invalid student type → " + studentType + " (use 'Regular Student' or 'Honors Student')");
                            failedCount++;
                            rowNum++;
                            continue;
                        }

                        try {
                            int id = context.generateStudentId();
                            Student newStudent = StudentFactory.createStudent(type, id, name, age, email, phone);
                            students.add(newStudent); // O(1) append
                            context.getStudentIndex().put(String.valueOf(newStudent.getId()), newStudent); // O(1) average
                            
                            // Cache newly added student
                            try {
                                CacheManager.getInstance().put("student:" + newStudent.getId(), newStudent);
                            } catch (Exception ex) {
                                // ignore cache errors
                            }
                            
                            successCount++;
                        } catch (InvalidStudentDataException e) {
                            failedRecords.add("Row " + (rowNum + 1) + ": " + e.getMessage());
                            failedCount++;
                        }
                        
                        rowNum++;
                    }
                }

                System.out.println("------------- IMPORT SUMMARY ----------------");
                System.out.println("Total Rows: " + totalRows);
                System.out.println("Successfully Imported: " + successCount);
                System.out.println("Failed: " + failedCount);

                if (!failedRecords.isEmpty()) {
                    System.out.println("\nFAILED RECORDS:");
                    failedRecords.stream()
                        .limit(20) // Show first 20 errors
                        .forEach(System.out::println);
                    if (failedRecords.size() > 20) {
                        System.out.println("... and " + (failedRecords.size() - 20) + " more errors");
                    }
                }

                System.out.println("---------------------------------------------");
                System.out.println("Import completed!");
                System.out.println("Total Students in System: " + students.size());
                
                long exec = System.currentTimeMillis() - start;
                try { 
                    AuditLogger.getInstance().log("BULK_IMPORT_STUDENTS", 
                        "file=" + filePath + ",total=" + totalRows + ",success=" + successCount + ",failed=" + failedCount, 
                        exec, true, ""); 
                } catch (Exception ex) { }
                
                valid = true; // exit loop if successful

            } catch (Exception e) {
                System.out.println("Error during import: " + e.getMessage());
                e.printStackTrace();
                System.out.println("Please try again.\n");
                try { scanner.nextLine(); } catch (Exception ex) { } // consume invalid input
                try { AuditLogger.getInstance().log("BULK_IMPORT_STUDENTS", "exception", 0, false, e.getMessage()); } catch (Exception ex) { }
            }
        }
    }


// ==================== SEARCH STUDENTS (Option 9) ====================

    private void searchStudents() {
        System.out.println("============================================");
        System.out.println("||         SEARCH STUDENTS               ||");
        System.out.println("============================================");
        System.out.println("\n--- BASIC SEARCH ---");
        System.out.println("1. Student ID");
        System.out.println("2. Name (Partial Match)");
        System.out.println("3. Grade Range");
        System.out.println("4. Student Type (Regular/Honors)");
        
        System.out.println("\n--- REGEX SEARCH ---");
        System.out.println("5. Search by Email Domain Pattern");
        System.out.println("6. Search by Student ID Pattern (Wildcard)");
        System.out.println("7. Search by Name Pattern (Regex)");
        System.out.println("8. Custom Regex Pattern Search");
        
        System.out.println("\n9. Back to Main Menu");
        System.out.print("Enter choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        ArrayList<Student> results = new ArrayList<>();
        RegexSearchEngine regexEngine = new RegexSearchEngine();

        try {
            switch (choice) {
                case 1:
                    results = searchByStudentId();
                    displaySearchResults(results);
                    break;
                case 2:
                    results = searchByName();
                    displaySearchResults(results);
                    break;
                case 3:
                    results = searchByGradeRange();
                    displaySearchResults(results);
                    break;
                case 4:
                    results = searchByStudentType();
                    displaySearchResults(results);
                    break;
                case 5:
                    regexSearchByEmailDomain(regexEngine);
                    break;
                case 6:
                    regexSearchByIdPattern(regexEngine);
                    break;
                case 7:
                    regexSearchByNamePattern(regexEngine);
                    break;
                case 8:
                    regexCustomPatternSearch(regexEngine);
                    break;
                case 9:
                    return;
                default:
                    System.out.println("Invalid choice!");
                    return;
            }

        } catch (Exception e) {
            System.out.println("Error during search: " + e.getMessage());
        }
    }

    private void regexSearchByEmailDomain(RegexSearchEngine engine) {
        System.out.println("\n========== REGEX SEARCH: EMAIL DOMAIN ==========");
        System.out.println("Examples: gmail\\.com, .*@.*\\.edu, yahoo.*");
        System.out.print("Enter email domain pattern: ");
        String pattern = scanner.nextLine().trim();

        System.out.print("Case insensitive? (y/n): ");
        boolean caseInsensitive = scanner.nextLine().trim().equalsIgnoreCase("y");

        engine.searchByEmailDomain(pattern, caseInsensitive);
        engine.displayResults();

        if (engine.hasResults()) {
            handleBulkOperations(engine);
        }
    }

    private void regexSearchByIdPattern(RegexSearchEngine engine) {
        System.out.println("\n========== REGEX SEARCH: STUDENT ID PATTERN ==========");
        System.out.println("Examples: 10.*, 1[0-2].*, ^101[0-5]$");
        System.out.print("Enter student ID pattern: ");
        String pattern = scanner.nextLine().trim();

        System.out.print("Case insensitive? (y/n): ");
        boolean caseInsensitive = scanner.nextLine().trim().equalsIgnoreCase("y");

        engine.searchByIdPattern(pattern, caseInsensitive);
        engine.displayResults();

        if (engine.hasResults()) {
            handleBulkOperations(engine);
        }
    }

    private void regexSearchByNamePattern(RegexSearchEngine engine) {
        System.out.println("\n========== REGEX SEARCH: NAME PATTERN ==========");
        System.out.println("Examples: ^A.*, .*Smith$, ^[A-M].*");
        System.out.print("Enter name pattern: ");
        String pattern = scanner.nextLine().trim();

        System.out.print("Case insensitive? (y/n): ");
        boolean caseInsensitive = scanner.nextLine().trim().equalsIgnoreCase("y");

        engine.searchByNamePattern(pattern, caseInsensitive);
        engine.displayResults();

        if (engine.hasResults()) {
            handleBulkOperations(engine);
        }
    }

    private void regexCustomPatternSearch(RegexSearchEngine engine) {
        System.out.println("\n========== CUSTOM REGEX PATTERN SEARCH ==========");
        System.out.println("Available fields: name, email, phone, type, status, id");
        System.out.print("Enter field name: ");
        String fieldName = scanner.nextLine().trim();

        System.out.print("Enter regex pattern: ");
        String pattern = scanner.nextLine().trim();

        System.out.print("Case insensitive? (y/n): ");
        boolean caseInsensitive = scanner.nextLine().trim().equalsIgnoreCase("y");

        engine.searchByCustomPattern(pattern, fieldName, caseInsensitive);
        engine.displayResults();

        if (engine.hasResults()) {
            handleBulkOperations(engine);
        }
    }

    private void handleBulkOperations(RegexSearchEngine engine) {
        boolean bulkOperating = true;
        while (bulkOperating) {
            engine.displayBulkOperationsMenu();
            int bulkChoice = scanner.nextInt();
            scanner.nextLine();

            engine.performBulkOperation(bulkChoice);

            if (bulkChoice == 4) {
                bulkOperating = false;
            }
        }
    }

    private ArrayList<Student> searchByStudentId() {
        ArrayList<Student> results = new ArrayList<>();

        System.out.print("Enter Student ID: ");
        int searchId = scanner.nextInt();
        scanner.nextLine();

        for (Student s : students) {
            if (s.id == searchId) {
                results.add(s);
                break;
            }
        }

        return results;
    }

    private ArrayList<Student> searchByName() {
        ArrayList<Student> results = new ArrayList<>();

        System.out.print("Enter student name (or part of name): ");
        String searchName = scanner.nextLine().toLowerCase().trim();

        if (searchName.isEmpty()) {
            System.out.println("Search name cannot be empty!");
            return results;
        }

        for (Student s : students) {
            if (s.name.toLowerCase().contains(searchName)) {
                results.add(s);
            }
        }

        return results;
    }

    private ArrayList<Student> searchByGradeRange() {
        ArrayList<Student> results = new ArrayList<>();

        System.out.print("Enter minimum grade (0-100): ");
        double minGrade = scanner.nextDouble();

        System.out.print("Enter maximum grade (0-100): ");
        double maxGrade = scanner.nextDouble();
        scanner.nextLine();

        if (minGrade < 0 || minGrade > 100 || maxGrade < 0 || maxGrade > 100) {
            System.out.println("Invalid grade range! Grades must be between 0 and 100.");
            return results;
        }

        if (minGrade > maxGrade) {
            System.out.println("Minimum grade cannot be greater than maximum grade!");
            return results;
        }

        for (Student s : students) {
            double avgGrade = s.getAverageGrade();
            if (avgGrade >= minGrade && avgGrade <= maxGrade && avgGrade > 0) {
                results.add(s);
            }
        }

        return results;
    }

    private ArrayList<Student> searchByStudentType() {
        ArrayList<Student> results = new ArrayList<>();

        System.out.println("Select Student Type:");
        System.out.println("1. Regular Students");
        System.out.println("2. Honors Students");
        System.out.print("Enter choice: ");

        int typeChoice = scanner.nextInt();
        scanner.nextLine();

        String targetType = "";
        if (typeChoice == 1) {
            targetType = "Regular";
        } else if (typeChoice == 2) {
            targetType = "Honors";
        } else {
            System.out.println("Invalid student type!");
            return results;
        }

        for (Student s : students) {
            if (s.getType().equals(targetType)) {
                results.add(s);
            }
        }

        return results;
    }

    private void displaySearchResults(ArrayList<Student> results) {
        if (results.isEmpty()) {
            System.out.println("\n❌ No students found matching your search criteria.\n");
            return;
        }

        System.out.println("\n============================================");
        System.out.println("||      SEARCH RESULTS                   ||");
        System.out.println("============================================");
        System.out.println("Found " + results.size() + " student(s)\n");

        System.out.printf("%-6s %-20s %-10s %-8s %-12s %-12s %-10s %-12s\n",
                "ID", "NAME", "AGE", "TYPE", "AVG GRADE", "SUBJECTS", "STATUS", "PASSING GRADE");
        System.out.println("------------------------------------------------------------------------------------------------");

        for (Student s : results) {
            double avg = s.getAverageGrade();
            int subjectCount = gradeManager.getSubjectCountForStudent(s.id);

            System.out.printf("%-6d %-20s %-10d %-8s %-12.2f %-12d %-10s %-12d\n",
                    s.id,
                    s.name,
                    s.age,
                    s.getType(),
                    avg > 0 ? avg : 0.0,
                    subjectCount,
                    s.getStatus(),
                    s.getPassingGrade()
            );
        }

        System.out.println("------------------------------------------------------------------------------------------------");


        System.out.println();
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

    /**
     * Launch the real-time statistics dashboard
     */
    private void launchStatisticsDashboard() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("REAL-TIME STATISTICS DASHBOARD");
        System.out.println("=".repeat(70));
        System.out.println("Initializing dashboard...");
        System.out.println("Spawning background calculation thread...\n");
        
        try {
            StatisticsDashboard dashboard = new StatisticsDashboard(gradeManager, students, scanner);
            dashboard.launch();
        } catch (Exception e) {
            System.out.println("Error launching dashboard: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n✓ Dashboard closed. Returning to main menu...");
    }

    /**
     * Launch concurrent batch report generation
     */
    private void concurrentBatchReportGeneration() {
        if (students.isEmpty()) {
            System.out.println("❌ No students to generate reports for.");
            return;
        }
        
        System.out.println("\n" + "═".repeat(70));
        System.out.println("CONCURRENT BATCH REPORT GENERATION");
        System.out.println("═".repeat(70));
        System.out.println(String.format("Students to process: %d", students.size()));
        
        // Get processor count
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        System.out.println(String.format("Available processors: %d", availableProcessors));
        System.out.println(String.format("Recommended thread range: 2-%d", Math.min(8, availableProcessors * 2)));
        
        // Get user's thread count selection
        System.out.print("\nEnter number of threads (2-8, default = " + 
                Math.min(4, availableProcessors) + "): ");
        
        int threadCount = 0;
        try {
            if (scanner.hasNextInt()) {
                threadCount = scanner.nextInt();
                scanner.nextLine();
                
                if (threadCount < 2 || threadCount > 8) {
                    System.out.println("⚠️  Invalid range. Using default: " + Math.min(4, availableProcessors));
                    threadCount = Math.min(4, availableProcessors);
                }
            } else {
                scanner.nextLine();
                threadCount = Math.min(4, availableProcessors);
            }
        } catch (Exception e) {
            threadCount = Math.min(4, availableProcessors);
        }
        
        try {
            // Create and run concurrent report generator
            ConcurrentReportGenerator generator = new ConcurrentReportGenerator(
                    gradeManager, students, threadCount);
            
            Map<String, ConcurrentReportGenerator.ReportStats> results = 
                    generator.generateReportsParallel();
            
            // Display summary
            System.out.println("✓ Batch report generation completed successfully!");
            System.out.println("Press Enter to return to main menu...");
            scanner.nextLine();
            
        } catch (Exception e) {
            System.out.println("❌ Error during concurrent report generation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Open scheduled tasks management menu
     */
    private void openScheduledTasksMenu() {
        boolean running = true;
        
        while (running) {
            System.out.println("\n" + "═".repeat(70));
            System.out.println("⏰ SCHEDULED AUTOMATED TASKS");
            System.out.println("═".repeat(70));
            
            List<ScheduledTask> tasks = context.getTaskScheduler().getActiveTasks();
            
            if (tasks.isEmpty()) {
                System.out.println("No scheduled tasks configured.");
            } else {
                System.out.println("\nActive Tasks:");
                for (int i = 0; i < tasks.size(); i++) {
                    ScheduledTask task = tasks.get(i);
                    System.out.printf("\n[%d] %s\n", i + 1, task.description);
                    System.out.println("    Status: " + (task.enabled ? "✓ Enabled" : "✗ Disabled"));
                    System.out.println("    " + context.getTaskScheduler().getTaskStatus(task));
                }
            }
            
            System.out.println("\n" + "─".repeat(70));
            System.out.println("1. View Task Details");
            System.out.println("2. Enable/Disable Task");
            System.out.println("3. View Task Execution Logs");
            System.out.println("4. Run Task Manually");
            System.out.println("5. Add Custom Task (Simulated)");
            System.out.println("6. Back to Main Menu");
            System.out.print("Enter choice: ");
            
            if (!scanner.hasNextInt()) {
                scanner.nextLine();
                continue;
            }
            
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            switch (choice) {
                case 1:
                    viewTaskDetails(tasks);
                    break;
                case 2:
                    toggleTaskStatus(tasks);
                    break;
                case 3:
                    viewTaskLogs(tasks);
                    break;
                case 4:
                    runTaskManually(tasks);
                    break;
                case 5:
                    System.out.println("Custom task scheduling feature (demonstration)");
                    System.out.println("In production, this would allow creating custom tasks with specific schedules.");
                    System.out.println("Press Enter to continue...");
                    scanner.nextLine();
                    break;
                case 6:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
    
    /**
     * View details of a scheduled task
     */
    private void viewTaskDetails(List<ScheduledTask> tasks) {
        if (tasks.isEmpty()) {
            System.out.println("No tasks available.");
            return;
        }
        
        System.out.println("\nSelect task to view details:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.printf("[%d] %s\n", i + 1, tasks.get(i).description);
        }
        System.out.print("Enter selection: ");
        
        if (!scanner.hasNextInt()) {
            scanner.nextLine();
            return;
        }
        
        int choice = scanner.nextInt() - 1;
        scanner.nextLine();
        
        if (choice >= 0 && choice < tasks.size()) {
            ScheduledTask task = tasks.get(choice);
            System.out.println("\n" + "═".repeat(70));
            System.out.println("TASK DETAILS: " + task.description);
            System.out.println("═".repeat(70));
            System.out.println("Task ID: " + task.taskId);
            System.out.println("Type: " + task.taskType.displayName);
            System.out.println("Schedule: " + task.scheduleType.displayName);
            System.out.println("Status: " + (task.enabled ? "Enabled" : "Disabled"));
            System.out.println("Created: " + task.createdAt);
            System.out.println("Total Executions: " + task.totalExecutions);
            System.out.println("Failures: " + task.failureCount);
            if (task.lastExecutionTime != null) {
                System.out.println("Last Execution: " + task.lastExecutionTime);
                System.out.println("Last Duration: " + task.lastExecutionDurationMs + "ms");
            }
            System.out.println("\n" + context.getTaskScheduler().getTaskStatus(task));
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }
    
    /**
     * Toggle task enabled/disabled status
     */
    private void toggleTaskStatus(List<ScheduledTask> tasks) {
        if (tasks.isEmpty()) {
            System.out.println("No tasks available.");
            return;
        }
        
        System.out.println("\nSelect task to toggle:");
        for (int i = 0; i < tasks.size(); i++) {
            ScheduledTask task = tasks.get(i);
            System.out.printf("[%d] %s (%s)\n", i + 1, task.description, 
                    task.enabled ? "Enabled" : "Disabled");
        }
        System.out.print("Enter selection: ");
        
        if (!scanner.hasNextInt()) {
            scanner.nextLine();
            return;
        }
        
        int choice = scanner.nextInt() - 1;
        scanner.nextLine();
        
        if (choice >= 0 && choice < tasks.size()) {
            ScheduledTask task = tasks.get(choice);
            if (task.enabled) {
                context.getTaskScheduler().disableTask(task.taskId);
            } else {
                context.getTaskScheduler().enableTask(task.taskId);
            }
        }
    }
    
    /**
     * View task execution logs
     */
    private void viewTaskLogs(List<ScheduledTask> tasks) {
        if (tasks.isEmpty()) {
            System.out.println("No tasks available.");
            return;
        }
        
        System.out.println("\nSelect task to view logs:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.printf("[%d] %s\n", i + 1, tasks.get(i).description);
        }
        System.out.print("Enter selection: ");
        
        if (!scanner.hasNextInt()) {
            scanner.nextLine();
            return;
        }
        
        int choice = scanner.nextInt() - 1;
        scanner.nextLine();
        
        if (choice >= 0 && choice < tasks.size()) {
            ScheduledTask task = tasks.get(choice);
            System.out.println("\nTask Execution Logs: " + task.description);
            System.out.println("Total Executions: " + task.totalExecutions);
            System.out.println("Successful: " + (task.totalExecutions - task.failureCount));
            System.out.println("Failed: " + task.failureCount);
            System.out.println("Success Rate: " + 
                    String.format("%.1f%%", 100.0 * (task.totalExecutions - task.failureCount) / 
                            Math.max(1, task.totalExecutions)));
            System.out.println("\nLogs file: ./task_logs/" + task.taskId + ".log");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }
    
    /**
     * Run a task manually
     */
    private void runTaskManually(List<ScheduledTask> tasks) {
        if (tasks.isEmpty()) {
            System.out.println("No tasks available.");
            return;
        }
        
        System.out.println("\nSelect task to run manually:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.printf("[%d] %s\n", i + 1, tasks.get(i).description);
        }
        System.out.print("Enter selection: ");
        
        if (!scanner.hasNextInt()) {
            scanner.nextLine();
            return;
        }
        
        int choice = scanner.nextInt() - 1;
        scanner.nextLine();
        
        if (choice >= 0 && choice < tasks.size()) {
            ScheduledTask task = tasks.get(choice);
            System.out.println("\n▶ Running task: " + task.description);
            System.out.println("(In actual implementation, this would execute the task immediately)");
            System.out.println("Task execution would be logged with timestamp and duration.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }

    private void advancedPatternSearch() {
        RegexSearchEngine searchEngine = new RegexSearchEngine();
        boolean searching = true;

        while (searching) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("        ADVANCED PATTERN BASED SEARCH");
            System.out.println("=".repeat(60));
            System.out.println("1. Search by Email Domain Pattern");
            System.out.println("2. Search by Phone Area Code Pattern");
            System.out.println("3. Search by Student ID Pattern (with wildcards)");
            System.out.println("4. Search by Name Pattern (Regex)");
            System.out.println("5. Custom Regex Pattern Search");
            System.out.println("6. Back to Main Menu");
            System.out.print("\nSelect option: ");

            try {
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        searchByEmailDomain(searchEngine);
                        break;
                    case 2:
                        searchByPhoneAreaCode(searchEngine);
                        break;
                    case 3:
                        searchByStudentIdPattern(searchEngine);
                        break;
                    case 4:
                        searchByNamePattern(searchEngine);
                        break;
                    case 5:
                        customPatternSearch(searchEngine);
                        break;
                    case 6:
                        searching = false;
                        System.out.println("Returning to main menu...");
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                scanner.nextLine();
            }
        }
    }

    private void searchByEmailDomain(RegexSearchEngine searchEngine) {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("Search by Email Domain Pattern");
        System.out.println("-".repeat(60));
        System.out.print("Enter domain pattern (e.g., gmail\\.com, yahoo\\..*): ");
        String pattern = scanner.nextLine();

        System.out.println("\nSearching...");
        ArrayList<RegexSearchEngine.SearchResult> results = searchEngine.searchByEmailDomain(pattern);

        displaySearchResults(results, searchEngine, "Email Domain");
    }

    private void searchByPhoneAreaCode(RegexSearchEngine searchEngine) {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("Search by Phone Area Code Pattern");
        System.out.println("-".repeat(60));
        System.out.print("Enter area code pattern (e.g., 212, 555, [2-5]1[0-5]): ");
        String pattern = scanner.nextLine();

        System.out.println("\nSearching...");
        ArrayList<RegexSearchEngine.SearchResult> results = searchEngine.searchByPhoneAreaCode(pattern);

        displaySearchResults(results, searchEngine, "Phone Area Code");
    }

    private void searchByStudentIdPattern(RegexSearchEngine searchEngine) {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("Search by Student ID Pattern (with wildcards)");
        System.out.println("-".repeat(60));
        System.out.println("Examples:");
        System.out.println("  10.*    - IDs starting with 10");
        System.out.println("  1[0-2].* - IDs starting with 10, 11, or 12");
        System.out.println("  .*5$    - IDs ending with 5");
        System.out.print("\nEnter ID pattern: ");
        String pattern = scanner.nextLine();

        System.out.println("\nSearching...");
        ArrayList<RegexSearchEngine.SearchResult> results = searchEngine.searchByIdPattern(pattern);

        displaySearchResults(results, searchEngine, "Student ID");
    }

    private void searchByNamePattern(RegexSearchEngine searchEngine) {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("Search by Name Pattern (Regex)");
        System.out.println("-".repeat(60));
        System.out.println("Examples:");
        System.out.println("  ^A.*     - Names starting with A");
        System.out.println("  .*Smith$ - Names ending with Smith");
        System.out.println("  ^J.*n$   - Names starting with J and ending with n");
        System.out.print("\nEnter name pattern: ");
        String pattern = scanner.nextLine();

        System.out.println("\nSearching...");
        ArrayList<RegexSearchEngine.SearchResult> results = searchEngine.searchByNamePattern(pattern);

        displaySearchResults(results, searchEngine, "Student Name");
    }

    private void customPatternSearch(RegexSearchEngine searchEngine) {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("Custom Regex Pattern Search");
        System.out.println("-".repeat(60));
        System.out.println("Select field to search:");
        System.out.println("1. Student ID");
        System.out.println("2. Name");
        System.out.println("3. Email");
        System.out.println("4. Phone");
        System.out.print("Select field: ");

        try {
            int fieldChoice = scanner.nextInt();
            scanner.nextLine();

            String field;
            switch (fieldChoice) {
                case 1:
                    field = "id";
                    break;
                case 2:
                    field = "name";
                    break;
                case 3:
                    field = "email";
                    break;
                case 4:
                    field = "phone";
                    break;
                default:
                    System.out.println("Invalid field selection.");
                    return;
            }

            System.out.print("Enter regex pattern: ");
            String pattern = scanner.nextLine();

            System.out.println("\nSearching...");
            ArrayList<RegexSearchEngine.SearchResult> results = searchEngine.searchByCustomPattern(field, pattern);

            displaySearchResults(results, searchEngine, field);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            scanner.nextLine();
        }
    }

    private void displaySearchResults(ArrayList<RegexSearchEngine.SearchResult> results, 
                                              RegexSearchEngine searchEngine, String searchType) {
        if (results.isEmpty()) {
            System.out.println("No matches found.");
            return;
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("SEARCH RESULTS - " + results.size() + " match(es) found");
        System.out.println("=".repeat(60));

        // Display results with highlighting
        searchEngine.displayResults(results);

        // Display statistics
        System.out.println("\n" + "-".repeat(60));
        searchEngine.displaySearchStatsMenu();
        System.out.println("-".repeat(60));

        // Bulk operations
        System.out.println("\nBulk Operations:");
        System.out.println("1. View Full Details");
        System.out.println("2. Export to CSV");
        System.out.println("3. View Statistics");
        System.out.println("4. Back to Pattern Search");
        System.out.print("Select option: ");

        try {
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    for (RegexSearchEngine.SearchResult result : results) {
                        System.out.println("\n" + "-".repeat(40));
                        System.out.println("Student: " + result.student.getName());
                        System.out.println("ID: " + result.student.getId());
                        System.out.println("Email: " + result.student.getEmail());
                        System.out.println("Phone: " + result.student.getPhone());
                        System.out.println("GPA: " + String.format("%.2f", result.student.computeGPA()));
                        System.out.println("Status: " + result.student.getStatus());
                        System.out.println("Matched Field: " + result.matchedField);
                        System.out.println("Match Value: " + result.highlightedMatch);
                    }
                    break;
                case 2:
                    searchEngine.performBulkOperation(results, "csv");
                    break;
                case 3:
                    searchEngine.performBulkOperation(results, "stats");
                    break;
                case 4:
                    return;
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            scanner.nextLine();
        }
    }

    private void openCacheMenu() {
        // Audit menu placed above cache menu in source; openAuditMenu is defined below
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

    //QUERY GRADE HISTORY - Advanced grade search and filtering
    private void queryGradeHistory() {
        boolean inQuery = true;
        while (inQuery) {
            try {
                System.out.println("\n" + "=".repeat(60));
                System.out.println("QUERY GRADE HISTORY");
                System.out.println("=".repeat(60));
                System.out.println("1. View All Grades");
                System.out.println("2. Filter by Student ID");
                System.out.println("3. Filter by Subject");
                System.out.println("4. Filter by Grade Range");
                System.out.println("5. Filter by Date Range");
                System.out.println("6. Filter by Subject Type (Core/Elective)");
                System.out.println("7. Advanced Multi-Filter Query");
                System.out.println("8. Back to Main Menu");
                System.out.print("Select option: ");
                
                int choice = scanner.nextInt();
                scanner.nextLine();
                
                switch (choice) {
                    case 1:
                        displayAllGrades();
                        break;
                    case 2:
                        filterByStudentId();
                        break;
                    case 3:
                        filterBySubject();
                        break;
                    case 4:
                        filterByGradeRange();
                        break;
                    case 5:
                        filterByDateRange();
                        break;
                    case 6:
                        filterBySubjectType();
                        break;
                    case 7:
                        advancedMultiFilter();
                        break;
                    case 8:
                        inQuery = false;
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

    private void displayAllGrades() {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("ALL GRADE RECORDS");
        System.out.println("=".repeat(100));
        System.out.printf("%-8s %-12s %-20s %-20s %-12s %-10s\n",
                "GradeID", "Student ID", "Student Name", "Subject", "Type", "Grade");
        System.out.println("=".repeat(100));
        
        int count = 0;
        for (int i = 0; i < gradeManager.getGradeCount(); i++) {
            Grade g = gradeManager.grades[i];
            if (g != null) {
                Student s = findStudentById(g.getStudentId());
                String studentName = (s != null) ? s.getName() : "Unknown";
                System.out.printf("%-8d %-12d %-20s %-20s %-12s %-10.2f\n",
                        g.getGradeId(),
                        g.getStudentId(),
                        studentName,
                        g.getSubject().getSubjectName(),
                        g.getSubject().getSubjectType(),
                        g.getGrade());
                count++;
            }
        }
        System.out.println("=".repeat(100));
        System.out.println("Total records: " + count);
    }

    private void filterByStudentId() {
        System.out.print("Enter Student ID: ");
        int studentId = scanner.nextInt();
        scanner.nextLine();
        
        Student student = findStudentById(studentId);
        if (student == null) {
            System.out.println("Student not found!");
            return;
        }
        
        System.out.println("\n" + "=".repeat(100));
        System.out.println("GRADES FOR STUDENT: " + student.getName() + " (ID: " + studentId + ")");
        System.out.println("=".repeat(100));
        System.out.printf("%-8s %-12s %-20s %-12s %-10s\n",
                "GradeID", "Date", "Subject", "Type", "Grade");
        System.out.println("=".repeat(100));
        
        int count = 0;
        for (int i = 0; i < gradeManager.getGradeCount(); i++) {
            Grade g = gradeManager.grades[i];
            if (g != null && g.getStudentId() == studentId) {
                System.out.printf("%-8d %-12s %-20s %-12s %-10.2f\n",
                        g.getGradeId(),
                        g.getDate(),
                        g.getSubject().getSubjectName(),
                        g.getSubject().getSubjectType(),
                        g.getGrade());
                count++;
            }
        }
        System.out.println("=".repeat(100));
        System.out.println("Total records: " + count);
    }

    private void filterBySubject() {
        System.out.print("Enter Subject Name: ");
        String subjectName = scanner.nextLine().trim();
        
        System.out.println("\n" + "=".repeat(100));
        System.out.println("GRADES FOR SUBJECT: " + subjectName);
        System.out.println("=".repeat(100));
        System.out.printf("%-8s %-12s %-20s %-12s %-10s\n",
                "GradeID", "Student ID", "Student Name", "Date", "Grade");
        System.out.println("=".repeat(100));
        
        int count = 0;
        for (int i = 0; i < gradeManager.getGradeCount(); i++) {
            Grade g = gradeManager.grades[i];
            if (g != null && g.getSubject().getSubjectName().equalsIgnoreCase(subjectName)) {
                Student s = findStudentById(g.getStudentId());
                String studentName = (s != null) ? s.getName() : "Unknown";
                System.out.printf("%-8d %-12d %-20s %-12s %-10.2f\n",
                        g.getGradeId(),
                        g.getStudentId(),
                        studentName,
                        g.getDate(),
                        g.getGrade());
                count++;
            }
        }
        System.out.println("=".repeat(100));
        System.out.println("Total records: " + count);
    }

    private void filterByGradeRange() {
        System.out.print("Enter minimum grade: ");
        double minGrade = scanner.nextDouble();
        System.out.print("Enter maximum grade: ");
        double maxGrade = scanner.nextDouble();
        scanner.nextLine();
        
        System.out.println("\n" + "=".repeat(100));
        System.out.println("GRADES BETWEEN " + minGrade + " AND " + maxGrade);
        System.out.println("=".repeat(100));
        System.out.printf("%-8s %-12s %-20s %-20s %-12s %-10s\n",
                "GradeID", "Student ID", "Student Name", "Subject", "Type", "Grade");
        System.out.println("=".repeat(100));
        
        int count = 0;
        for (int i = 0; i < gradeManager.getGradeCount(); i++) {
            Grade g = gradeManager.grades[i];
            if (g != null && g.getGrade() >= minGrade && g.getGrade() <= maxGrade) {
                Student s = findStudentById(g.getStudentId());
                String studentName = (s != null) ? s.getName() : "Unknown";
                System.out.printf("%-8d %-12d %-20s %-20s %-12s %-10.2f\n",
                        g.getGradeId(),
                        g.getStudentId(),
                        studentName,
                        g.getSubject().getSubjectName(),
                        g.getSubject().getSubjectType(),
                        g.getGrade());
                count++;
            }
        }
        System.out.println("=".repeat(100));
        System.out.println("Total records: " + count);
    }

    private void filterByDateRange() {
        System.out.print("Enter start date (YYYY-MM-DD): ");
        String startDate = scanner.nextLine().trim();
        System.out.print("Enter end date (YYYY-MM-DD): ");
        String endDate = scanner.nextLine().trim();
        
        System.out.println("\n" + "=".repeat(100));
        System.out.println("GRADES BETWEEN " + startDate + " AND " + endDate);
        System.out.println("=".repeat(100));
        System.out.printf("%-8s %-12s %-20s %-20s %-12s %-12s %-10s\n",
                "GradeID", "Student ID", "Student Name", "Subject", "Type", "Date", "Grade");
        System.out.println("=".repeat(100));
        
        int count = 0;
        for (int i = 0; i < gradeManager.getGradeCount(); i++) {
            Grade g = gradeManager.grades[i];
            if (g != null) {
                String gradeDate = g.getDate().toString();
                if (gradeDate.compareTo(startDate) >= 0 && gradeDate.compareTo(endDate) <= 0) {
                    Student s = findStudentById(g.getStudentId());
                    String studentName = (s != null) ? s.getName() : "Unknown";
                    System.out.printf("%-8d %-12d %-20s %-20s %-12s %-12s %-10.2f\n",
                            g.getGradeId(),
                            g.getStudentId(),
                            studentName,
                            g.getSubject().getSubjectName(),
                            g.getSubject().getSubjectType(),
                            gradeDate,
                            g.getGrade());
                    count++;
                }
            }
        }
        System.out.println("=".repeat(100));
        System.out.println("Total records: " + count);
    }

    private void filterBySubjectType() {
        System.out.println("Select Subject Type:");
        System.out.println("1. Core");
        System.out.println("2. Elective");
        System.out.print("Enter choice: ");
        int typeChoice = scanner.nextInt();
        scanner.nextLine();
        
        String subjectType = (typeChoice == 1) ? "Core" : "Elective";
        
        System.out.println("\n" + "=".repeat(100));
        System.out.println(subjectType.toUpperCase() + " SUBJECT GRADES");
        System.out.println("=".repeat(100));
        System.out.printf("%-8s %-12s %-20s %-20s %-12s %-10s\n",
                "GradeID", "Student ID", "Student Name", "Subject", "Date", "Grade");
        System.out.println("=".repeat(100));
        
        int count = 0;
        for (int i = 0; i < gradeManager.getGradeCount(); i++) {
            Grade g = gradeManager.grades[i];
            if (g != null && g.getSubject().getSubjectType().equalsIgnoreCase(subjectType)) {
                Student s = findStudentById(g.getStudentId());
                String studentName = (s != null) ? s.getName() : "Unknown";
                System.out.printf("%-8d %-12d %-20s %-20s %-12s %-10.2f\n",
                        g.getGradeId(),
                        g.getStudentId(),
                        studentName,
                        g.getSubject().getSubjectName(),
                        g.getDate(),
                        g.getGrade());
                count++;
            }
        }
        System.out.println("=".repeat(100));
        System.out.println("Total records: " + count);
    }

    private void advancedMultiFilter() {
        System.out.println("\n--- ADVANCED MULTI-FILTER QUERY ---");
        
        System.out.print("Filter by Student ID? (y/n): ");
        boolean useStudentId = scanner.nextLine().trim().equalsIgnoreCase("y");
        Integer studentId = null;
        if (useStudentId) {
            System.out.print("Enter Student ID: ");
            studentId = scanner.nextInt();
            scanner.nextLine();
        }
        
        System.out.print("Filter by Subject? (y/n): ");
        boolean useSubject = scanner.nextLine().trim().equalsIgnoreCase("y");
        String subjectName = null;
        if (useSubject) {
            System.out.print("Enter Subject Name: ");
            subjectName = scanner.nextLine().trim();
        }
        
        System.out.print("Filter by Grade Range? (y/n): ");
        boolean useGradeRange = scanner.nextLine().trim().equalsIgnoreCase("y");
        Double minGrade = null, maxGrade = null;
        if (useGradeRange) {
            System.out.print("Enter minimum grade: ");
            minGrade = scanner.nextDouble();
            System.out.print("Enter maximum grade: ");
            maxGrade = scanner.nextDouble();
            scanner.nextLine();
        }
        
        System.out.print("Filter by Subject Type? (y/n): ");
        boolean useSubjectType = scanner.nextLine().trim().equalsIgnoreCase("y");
        String subjectType = null;
        if (useSubjectType) {
            System.out.println("1. Core  2. Elective");
            System.out.print("Enter choice: ");
            int typeChoice = scanner.nextInt();
            scanner.nextLine();
            subjectType = (typeChoice == 1) ? "Core" : "Elective";
        }
        
        System.out.println("\n" + "=".repeat(100));
        System.out.println("FILTERED GRADE RECORDS");
        System.out.println("=".repeat(100));
        System.out.printf("%-8s %-12s %-20s %-20s %-12s %-12s %-10s\n",
                "GradeID", "Student ID", "Student Name", "Subject", "Type", "Date", "Grade");
        System.out.println("=".repeat(100));
        
        int count = 0;
        for (int i = 0; i < gradeManager.getGradeCount(); i++) {
            Grade g = gradeManager.grades[i];
            if (g != null) {
                boolean matches = true;
                
                if (useStudentId && g.getStudentId() != studentId) {
                    matches = false;
                }
                
                if (useSubject && !g.getSubject().getSubjectName().equalsIgnoreCase(subjectName)) {
                    matches = false;
                }
                
                if (useGradeRange && (g.getGrade() < minGrade || g.getGrade() > maxGrade)) {
                    matches = false;
                }
                
                if (useSubjectType && !g.getSubject().getSubjectType().equalsIgnoreCase(subjectType)) {
                    matches = false;
                }
                
                if (matches) {
                    Student s = findStudentById(g.getStudentId());
                    String studentName = (s != null) ? s.getName() : "Unknown";
                    System.out.printf("%-8d %-12d %-20s %-20s %-12s %-12s %-10.2f\n",
                            g.getGradeId(),
                            g.getStudentId(),
                            studentName,
                            g.getSubject().getSubjectName(),
                            g.getSubject().getSubjectType(),
                            g.getDate(),
                            g.getGrade());
                    count++;
                }
            }
        }
        System.out.println("=".repeat(100));
        System.out.println("Total matching records: " + count);
    }

    private Student findStudentById(int studentId) {
        for (Student s : students) {
            if (s.getId() == studentId) {
                return s;
            }
        }
        return null;
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
        boolean inStreams = true;
        while (inStreams) {
            System.out.println("\n" + "=".repeat(70));
            System.out.println("STREAM-BASED DATA PROCESSING (Java Streams API)");
            System.out.println("=".repeat(70));
            System.out.println("1. Find all honors students with GPA > 3.5");
            System.out.println("2. Group students by GPA range");
            System.out.println("3. Calculate average grade per subject");
            System.out.println("4. Extract unique course codes");
            System.out.println("5. Find top 5 students by average grade");
            System.out.println("6. Partition students by honors eligibility");
            System.out.println("7. Process large CSV file with Files.lines()");
            System.out.println("8. Compare sequential vs parallel stream performance");
            System.out.println("9. Back to Main Menu");
            System.out.print("Select option: ");

            try {
                int choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice) {
                    case 1 -> streamHonorsStudents();
                    case 2 -> streamGroupStudentsByGpaRange();
                    case 3 -> streamAverageGradePerSubject();
                    case 4 -> streamUniqueCourseCodes();
                    case 5 -> streamTop5ByAverageGrade();
                    case 6 -> streamPartitionHonorsEligibility();
                    case 7 -> streamProcessLargeCsv();
                    case 8 -> streamSequentialVsParallelComparison();
                    case 9 -> inStreams = false;
                    default -> System.out.println("Invalid option.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                scanner.nextLine();
            }
        }
    }

    // ---------- Stream helpers ----------

    // 1. Find all honors students with GPA > 3.5 using filter, map, sorted, collect,
    //    plus examples of findFirst, findAny, anyMatch, noneMatch
    private void streamHonorsStudents() {
        if (students.isEmpty()) {
            System.out.println("No students in system.");
            return;
        }

        long start = System.nanoTime();
        List<Student> honors = students.stream()
                .filter(s -> s instanceof HonorsStudent)
                .filter(s -> s.computeGPA() > 3.5)
                .sorted((a, b) -> Double.compare(b.computeGPA(), a.computeGPA()))
                .collect(Collectors.toList());
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        System.out.println("\nHonors students with GPA > 3.5 (sequential stream):");
        honors.forEach(s -> System.out.printf("ID: %d | Name: %s | GPA: %.2f%n",
                s.getId(), s.getName(), s.computeGPA()));
        System.out.println("Execution time: " + durationMs + " ms");

        // Demonstrate findFirst / findAny / anyMatch / noneMatch
        students.stream()
                .filter(s -> s instanceof HonorsStudent)
                .filter(s -> s.computeGPA() > 3.5)
                .findFirst()
                .ifPresentOrElse(
                        s -> System.out.println("findFirst(): " + s.getName() + " (GPA " + String.format("%.2f", s.computeGPA()) + ")"),
                        () -> System.out.println("findFirst(): no matching student")
                );

        students.parallelStream()
                .filter(s -> s instanceof HonorsStudent)
                .filter(s -> s.computeGPA() > 3.5)
                .findAny()
                .ifPresentOrElse(
                        s -> System.out.println("findAny() (parallel): " + s.getName()),
                        () -> System.out.println("findAny(): no matching student")
                );

        boolean anyHighGpa = students.stream().anyMatch(s -> s.computeGPA() > 3.8);
        boolean noneBelowZeroGpa = students.stream().noneMatch(s -> s.computeGPA() < 0);
        System.out.println("anyMatch(GPA > 3.8): " + anyHighGpa);
        System.out.println("noneMatch(GPA < 0): " + noneBelowZeroGpa);
    }

    // 2. Group students by GPA range using groupingBy and collect
    private void streamGroupStudentsByGpaRange() {
        if (students.isEmpty()) {
            System.out.println("No students in system.");
            return;
        }

        long start = System.nanoTime();
        Map<String, List<Student>> byRange = students.stream()
                .collect(Collectors.groupingBy(this::gpaRangeLabel));
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        System.out.println("\nStudents grouped by GPA range:");
        byRange.forEach((range, list) -> {
            System.out.printf("%s -> %d students%n", range, list.size());
        });
        System.out.println("Execution time: " + durationMs + " ms");
    }

    private String gpaRangeLabel(Student s) {
        double gpa = s.computeGPA();
        if (gpa >= 3.5) return "Honors (>=3.5)";
        if (gpa >= 3.0) return "Strong (3.0 - 3.49)";
        if (gpa >= 2.0) return "Average (2.0 - 2.99)";
        if (gpa > 0.0) return "Below Average (0 - 1.99)";
        return "No GPA";
    }

    // 3. Average grade per subject using streams over GradeManager
    private void streamAverageGradePerSubject() {
        if (gradeManager.getGradeCount() == 0) {
            System.out.println("No grades recorded.");
            return;
        }

        long start = System.nanoTime();
        Map<String, Double> avgBySubject = java.util.Arrays.stream(gradeManager.grades, 0, gradeManager.getGradeCount())
                .filter(g -> g != null)
                .collect(Collectors.groupingBy(
                        g -> g.getSubject().getSubjectName(),
                        Collectors.averagingDouble(Grade::getGrade)
                ));
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        System.out.println("\nAverage grade per subject:");
        avgBySubject.forEach((subject, avg) ->
                System.out.printf("%-25s : %.2f%n", subject, avg));
        System.out.println("Execution time: " + durationMs + " ms");
    }

    // 4. Extract unique course codes
    private void streamUniqueCourseCodes() {
        if (gradeManager.getGradeCount() == 0) {
            System.out.println("No grades recorded.");
            return;
        }

        long start = System.nanoTime();
        Set<String> uniqueCodes = java.util.Arrays.stream(gradeManager.grades, 0, gradeManager.getGradeCount())
                .filter(g -> g != null && g.getSubject() != null)
                .map(g -> g.getSubject().getSubjectCode())
                .filter(code -> code != null && !code.isEmpty())
                .collect(Collectors.toSet());
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        System.out.println("\nUnique course codes used in recorded grades:");
        uniqueCodes.forEach(System.out::println);
        System.out.println("Execution time: " + durationMs + " ms");
    }

    // 5. Find top 5 students by average grade (chained: filter -> map/sort -> limit -> collect)
    private void streamTop5ByAverageGrade() {
        if (students.isEmpty()) {
            System.out.println("No students in system.");
            return;
        }

        long start = System.nanoTime();
        List<Student> top5 = students.stream()
                .filter(s -> s.getEnrolledSubjects() > 0)
                .sorted((a, b) -> Double.compare(b.getAverageGrade(), a.getAverageGrade()))
                .limit(5)
                .collect(Collectors.toList());
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        System.out.println("\nTop 5 students by average grade:");
        int rank = 1;
        for (Student s : top5) {
            System.out.printf("%d. %-20s | ID: %d | Avg Grade: %.2f | GPA: %.2f%n",
                    rank++, s.getName(), s.getId(), s.getAverageGrade(), s.computeGPA());
        }
        System.out.println("Execution time: " + durationMs + " ms");
    }

    // 6. Partition students by honors eligibility using partitioningBy
    private void streamPartitionHonorsEligibility() {
        if (students.isEmpty()) {
            System.out.println("No students in system.");
            return;
        }

        long start = System.nanoTime();
        Map<Boolean, List<Student>> partitioned = students.stream()
                .collect(Collectors.partitioningBy(Student::isEligibleForHonors));
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        System.out.println("\nPartitioned students by honors eligibility:");
        System.out.println("Eligible for honors: " + partitioned.get(true).size());
        System.out.println("Not eligible: " + partitioned.get(false).size());
        System.out.println("Execution time: " + durationMs + " ms");
    }

    // 7. Process a large CSV file using Files.lines() stream
    private void streamProcessLargeCsv() {
        System.out.print("Enter CSV file name in ./imports (without extension): ");
        String fileName = scanner.nextLine().trim();
        Path path = Path.of("./imports/" + fileName + ".csv");

        if (!path.toFile().exists()) {
            System.out.println("File not found: " + path.toAbsolutePath());
            return;
        }

        try {
            long startSeq = System.nanoTime();
            long validLines;
            try (Stream<String> lines = Files.lines(path)) {
                validLines = lines
                        .skip(1) // skip header
                        .filter(line -> !line.trim().isEmpty())
                        .count();
            }
            long seqMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startSeq);

            long startPar = System.nanoTime();
            long validLinesParallel;
            try (Stream<String> lines = Files.lines(path).parallel()) {
                validLinesParallel = lines
                        .skip(1)
                        .filter(line -> !line.trim().isEmpty())
                        .count();
            }
            long parMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startPar);

            System.out.println("\nCSV processing with Files.lines():");
            System.out.println("Valid (non-empty) data rows: " + validLines);
            System.out.println("Sequential stream time: " + seqMs + " ms");
            System.out.println("Parallel stream time:   " + parMs + " ms");
        } catch (Exception e) {
            System.out.println("Error processing CSV: " + e.getMessage());
        }
    }

    // 8. Compare sequential vs parallel stream processing on in-memory data
    private void streamSequentialVsParallelComparison() {
        if (students.isEmpty()) {
            System.out.println("No students in system.");
            return;
        }

        // Simple heavy-ish operation: sum of GPA for students with GPA > 2.5
        long startSeq = System.nanoTime();
        double sumSeq = students.stream()
                .mapToDouble(Student::computeGPA)
                .filter(gpa -> gpa > 2.5)
                .sum();
        long seqMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startSeq);

        long startPar = System.nanoTime();
        double sumPar = students.parallelStream()
                .mapToDouble(Student::computeGPA)
                .filter(gpa -> gpa > 2.5)
                .sum();
        long parMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startPar);

        System.out.println("\nSequential vs Parallel stream comparison (GPA > 2.5 sum):");
        System.out.printf("Sequential sum: %.2f (time: %d ms)%n", sumSeq, seqMs);
        System.out.printf("Parallel   sum: %.2f (time: %d ms)%n", sumPar, parMs);
    }
}



