package ui;

import context.ApplicationContext;
import core.Student;
import core.Grade;
import core.Subject;
import core.CoreSubject;
import core.ElectiveSubject;
import models.HonorsStudent;
import manager.GradeManager;
import audit.AuditLogger;

import exception.StudentNotFoundException;
import exception.SubjectNotFoundException;
import exception.InvalidGradeException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * GradeMenuHandler - Handles all grade-related operations
 * Responsibility: Record grades, view grade reports, calculate GPA, and view statistics
 * Single Responsibility: Grade management and reporting
 */
public class GradeMenuHandler {
    private final ApplicationContext context;
    private final Scanner scanner;
    private final ArrayList<Student> students;
    private final GradeManager gradeManager;

    public GradeMenuHandler(ApplicationContext context, Scanner scanner, 
                           ArrayList<Student> students, GradeManager gradeManager) {
        this.context = context;
        this.scanner = scanner;
        this.students = students;
        this.gradeManager = gradeManager;
    }

    /**
     * Record a grade for a student
     */
    public void recordGrade() {
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

    /**
     * View grade report for a student
     */
    public void viewGradeReport() {
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

    /**
     * Calculate and display GPA report for a student
     */
    public void viewStudentGPAReport(int studentId) {
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
                double gpaPoints = (g.getGrade() >= 80 ? 4.0 :
                                    g.getGrade() >= 70 ? 3.0 :
                                    g.getGrade() >= 60 ? 2.0 :
                                    g.getGrade() >= 50 ? 1.0 : 0.0);
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

    /**
     * View class-wide statistics
     */
    public void viewClassStatistics() {
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
            if (s instanceof models.HonorsStudent) {
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
}
