package ui;

import context.ApplicationContext;
import core.Student;
import models.HonorsStudent;
import models.StudentFactory;
import manager.CacheManager;
import audit.AuditLogger;

import exception.InvalidStudentDataException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * StudentMenuHandler - Handles all student management operations
 * Responsibility: Add and view students
 * Single Responsibility: Student management only
 */
public class StudentMenuHandler {
    private final ApplicationContext context;
    private final Scanner scanner;
    private final ArrayList<Student> students;

    public StudentMenuHandler(ApplicationContext context, Scanner scanner, ArrayList<Student> students) {
        this.context = context;
        this.scanner = scanner;
        this.students = students;
    }

    /**
     * Add a new student to the system
     */
    public void addStudent() {
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

    /**
     * View all students in the system
     */
    public void viewStudents() {
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
}
