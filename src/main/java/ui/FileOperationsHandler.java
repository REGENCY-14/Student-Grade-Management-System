package ui;

import context.ApplicationContext;
import core.Student;
import core.Grade;
import manager.GradeManager;
import manager.FileFormatManager;
import manager.CacheManager;
import models.StudentFactory;
import audit.AuditLogger;

import exception.InvalidStudentDataException;
import exception.FileImportException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * FileOperationsHandler - Handles file import and export operations
 * Responsibility: Import/export grades and students in multiple formats
 * Single Responsibility: File operations only
 */
public class FileOperationsHandler {
    private final ApplicationContext context;
    private final Scanner scanner;
    private final ArrayList<Student> students;
    private final GradeManager gradeManager;

    public FileOperationsHandler(ApplicationContext context, Scanner scanner, 
                                ArrayList<Student> students, GradeManager gradeManager) {
        this.context = context;
        this.scanner = scanner;
        this.students = students;
        this.gradeManager = gradeManager;
    }

    /**
     * Export grade report for a student
     */
    public void exportGradeReport() {
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

    /**
     * Print export information
     */
    private void printExportInfo(File file, String type) {
        System.out.println("\nReport exported successfully!");
        System.out.println("File Name: " + file.getName());
        System.out.println("Location: " + file.getAbsolutePath());
        System.out.println("Size: " + file.length() + " bytes");
        System.out.println("What file contains: " + type + " Report\n");
    }

    /**
     * Bulk import grades from multiple formats
     */
    public void bulkImportGrades() {
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

    /**
     * Bulk import students from CSV file
     */
    public void bulkImportStudents() {
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
}
