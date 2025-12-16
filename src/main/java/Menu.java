import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import exception.FileImportException;
import exception.GradeStorageFullException;
import exception.InvalidGradeException;
import exception.InvalidReportFormatException;
import exception.InvalidStudentDataException;
import exception.StudentNotFoundException;
import exception.SubjectNotFoundException;

public class Menu {


    static Scanner scanner = new Scanner(System.in);
    static ArrayList<Student> students = new ArrayList<>();
    static int studentIdCounter = 1000;
    static ArrayList<Grade> grades = new ArrayList<>();
    static GradeManager gradeManager = new GradeManager();
    static StudentService studentService = new StudentService(students, 1000);


    public static void main(String[] args) throws StudentNotFoundException, GradeStorageFullException, InvalidGradeException, SubjectNotFoundException, InvalidStudentDataException, FileImportException, InvalidReportFormatException {

        boolean running = true;

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
            }else if(choice == 5){
                exportGradeReport();
            }else if (choice == 6) {
                System.out.println("Enter student ID: ");
                int studentId = scanner.nextInt();
                viewStudentGPAReport(studentId);
            }else if (choice == 7){
                bulkImportGrades();
            }else if (choice == 10){
                concurrentBatchReportGeneration();
            }else if (choice == 8) {
                viewClassStatistics();
            } else if (choice == 9){
                searchStudents();
            } else if (choice == 11) {
                launchStatisticsDashboard();
            } else if (choice == 13) {
                running = false;
                System.out.println("Thank you for using grade management system!");
                System.out.println("Goodbye");
            } else {
                System.out.println("Invalid choice. Try again!");
            }
        }
    }


    // MAIN MENU
    public static void mainMenu() {
        System.out.println("============================================");
        System.out.println("||    STUDENT GRADE MANAGEMENT SYSTEM     ||");
        System.out.println("============================================");

        System.out.println("\n--- STUDENT MANAGEMENT ---");
        System.out.println("1. Add Student");
        System.out.println("2. View Students");
        System.out.println("3. Record Grade");

        System.out.println("\n--- FILE OPERATIONS ---");
        System.out.println("5. Export Grade Report");
        System.out.println("7. Bulk Import Grades");
        System.out.println("10. Concurrent Batch Report Generation");

        System.out.println("\n--- ANALYTICS AND REPORTING ---");
        System.out.println("4. View Grade Report");
        System.out.println("6. Calculate Student GPA");
        System.out.println("8. View Student Statistics");
        System.out.println("11. Real-Time Statistics Dashboard");

        System.out.println("\n--- SEARCH AND QUERY ---");
        System.out.println("9. Search Students");

        System.out.println("13. Exit");

        System.out.print("\nEnter choice: ");
    }


    public static void addStudent() {
        boolean valid = false;
        while (!valid) {
            try {
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

                int id = studentIdCounter++;

                Student newStudent = StudentFactory.createStudent(type, id, name, age, email, phone);
                students.add(newStudent);
                System.out.println(newStudent.getType() + " student added!");


                System.out.println("--------------------------------------------");
                valid = true; // exit loop if successful

            } catch (InvalidStudentDataException e) {
                System.out.println("Error: " + e.getMessage());
                System.out.println("Please try again.\n");
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
                System.out.println("Please try again.\n");
                scanner.nextLine(); // consume invalid input
            }
        }
    }


    // VIEW STUDENTS
    public static void viewStudents() {
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
    public static void recordGrade() {
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
    public static void viewGradeReport() throws StudentNotFoundException {
        System.out.print("Enter student ID: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        gradeManager.viewGradeByStudent(id);
    }

    // CALCULATE STUDENT GPA
    public static void viewStudentGPAReport(int studentId) {
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
    public static void viewClassStatistics() {
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
    public static void exportGradeReport() {
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


    private static void printExportInfo(File file, String type) {
        System.out.println("\nReport exported successfully!");
        System.out.println("File Name: " + file.getName());
        System.out.println("Location: " + file.getAbsolutePath());
        System.out.println("Size: " + file.length() + " bytes");
        System.out.println("What file contains: " + type + " Report\n");
    }

    //BULK IMPORT GRADES
    public static void bulkImportGrades() {
        boolean valid = false;
        while (!valid) {
            try {
                System.out.println("------------- BULK IMPORT GRADES ----------------");

                System.out.print("Enter file name (without extension): ");
                String fileName = scanner.nextLine().trim();
                String filePath = "./imports/" + fileName + ".csv";

                File file = new File(filePath);
                if (!file.exists()) {
                    System.out.println("File not found: " + filePath);
                    continue; // retry
                }

                System.out.println("\nProcessing grades ...\n");

                int totalRows = 0, successCount = 0, failedCount = 0;
                ArrayList<String> failedRecords = new ArrayList<>();

                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    int rowNum = 1;
                    while ((line = br.readLine()) != null) {
                        totalRows++;
                        String[] parts = line.split(",");
                        if (parts.length != 4) {
                            failedRecords.add("Row " + rowNum + ": Invalid column count");
                            failedCount++;
                            rowNum++;
                            continue;
                        }

                        String studentIdStr = parts[0].trim();
                        String subjectName = parts[1].trim();
                        String subjectType = parts[2].trim();
                        String gradeStr = parts[3].trim();

                        int studentId;
                        try {
                            studentId = Integer.parseInt(studentIdStr);
                        } catch (Exception e) {
                            failedRecords.add("Row " + rowNum + ": Invalid Student ID → " + studentIdStr);
                            failedCount++;
                            rowNum++;
                            continue;
                        }

                        Student student = null;
                        for (Student s : students) {
                            if (s.id == studentId) {
                                student = s;
                                break;
                            }
                        }
                        if (student == null) {
                            failedRecords.add("Row " + rowNum + ": Student not found → " + studentId);
                            failedCount++;
                            rowNum++;
                            continue;
                        }

                        Subject subject;
                        if (subjectType.equalsIgnoreCase("Core")) {
                            subject = new CoreSubject(subjectName, "C-" + subjectName.substring(0, 3).toUpperCase());
                        } else if (subjectType.equalsIgnoreCase("Elective")) {
                            subject = new ElectiveSubject(subjectName, "E-" + subjectName.substring(0, 3).toUpperCase());
                        } else {
                            failedRecords.add("Row " + rowNum + ": Invalid subject type → " + subjectType);
                            failedCount++;
                            rowNum++;
                            continue;
                        }

                        double gradeValue;
                        try {
                            gradeValue = Double.parseDouble(gradeStr);
                        } catch (Exception e) {
                            failedRecords.add("Row " + rowNum + ": Grade not numeric → " + gradeStr);
                            failedCount++;
                            rowNum++;
                            continue;
                        }

                        if (gradeValue < 0 || gradeValue > 100) {
                            failedRecords.add("Row " + rowNum + ": Grade out of range → " + gradeValue);
                            failedCount++;
                            rowNum++;
                            continue;
                        }

                        Grade newGrade = new Grade(studentId, subject, gradeValue);
                        gradeManager.addGrade(newGrade);
                        successCount++;
                        rowNum++;
                    }
                }

                System.out.println("---------------- IMPORT SUMMARY ----------------");
                System.out.println("Total Rows: " + totalRows);
                System.out.println("Successfully Imported: " + successCount);
                System.out.println("Failed: " + failedCount);

                if (!failedRecords.isEmpty()) {
                    System.out.println("\nFAILED RECORDS:");
                    for (String error : failedRecords) {
                        System.out.println(error);
                    }
                }

                System.out.println("------------------------------------------------");
                System.out.println("Import completed!");
                valid = true; // exit loop if successful

            } catch (Exception e) {
                System.out.println("Error during import: " + e.getMessage());
                System.out.println("Please try again.\n");
                scanner.nextLine(); // consume invalid input
            }
        }
    }


// ==================== SEARCH STUDENTS (Option 9) ====================

    public static void searchStudents() {
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

    private static void regexSearchByEmailDomain(RegexSearchEngine engine) {
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

    private static void regexSearchByIdPattern(RegexSearchEngine engine) {
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

    private static void regexSearchByNamePattern(RegexSearchEngine engine) {
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

    private static void regexCustomPatternSearch(RegexSearchEngine engine) {
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

    private static void handleBulkOperations(RegexSearchEngine engine) {
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

    private static ArrayList<Student> searchByStudentId() {
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

    private static ArrayList<Student> searchByName() {
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

    private static ArrayList<Student> searchByGradeRange() {
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

    private static ArrayList<Student> searchByStudentType() {
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

    private static void displaySearchResults(ArrayList<Student> results) {
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
    public static void advancedImportGrades() {
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
    public static void advancedExportGrades() {
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

    private static void importMultiFormat(FileFormatManager formatManager, String format) {
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

    private static void watchDirectoryForNewFiles(FileFormatManager formatManager) {
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

    private static void listFilesByFormat(FileFormatManager formatManager) {
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
    public static void launchStatisticsDashboard() {
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
    public static void concurrentBatchReportGeneration() {
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
}




