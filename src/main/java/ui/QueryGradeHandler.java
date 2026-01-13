package ui;

import context.ApplicationContext;
import core.Student;
import core.Grade;
import manager.GradeManager;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * QueryGradeHandler - Handles grade history queries and filtering
 * Responsibility: Query and filter grades by various criteria
 * Single Responsibility: Grade querying only
 */
public class QueryGradeHandler {
    private final ApplicationContext context;
    private final Scanner scanner;
    private final ArrayList<Student> students;
    private final GradeManager gradeManager;

    public QueryGradeHandler(ApplicationContext context, Scanner scanner, 
                            ArrayList<Student> students, GradeManager gradeManager) {
        this.context = context;
        this.scanner = scanner;
        this.students = students;
        this.gradeManager = gradeManager;
    }

    public void queryGradeHistory() {
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
}
