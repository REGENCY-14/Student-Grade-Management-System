import exception.GradeStorageFullException;
import exception.InvalidGradeException;
import exception.StudentNotFoundException;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Menu {

    static Scanner scanner = new Scanner(System.in);
    static ArrayList<Student> students = new ArrayList<>();
    static int studentIdCounter = 1000;
    static ArrayList<Grade> grades = new ArrayList<>();
    static GradeManager gradeManager = new GradeManager();


    public static void main(String[] args) throws StudentNotFoundException, GradeStorageFullException, InvalidGradeException {

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
            }else if (choice == 8) {
                viewClassStatistics();
            } else if (choice == 10) {
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

        System.out.println("1. Add Student");
        System.out.println("2. View Students");
        System.out.println("3. Record Grade");
        System.out.println("4. View Grade Report");
        System.out.println("5. Export Grade Report");
        System.out.println("6. Calculate Student GPA");
        System.out.println("7. Bulk Import Grades");
        System.out.println("8. View Student Statistics");
        System.out.println("9. Search Students");
        System.out.println("10. Exit");

        System.out.print("Enter choice: ");
    }


    // ADD STUDENT
    public static void addStudent() {
        System.out.println("-------------- ADD STUDENT ----------------");

        System.out.print("Enter student name: ");
        String name = scanner.nextLine();

        System.out.print("Enter student age: ");
        int age = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter student email: ");
        String email = scanner.nextLine();

        System.out.print("Enter student phone: ");
        String phone = scanner.nextLine();

        System.out.println("Select student type:");
        System.out.println("1. Regular Student");
        System.out.println("2. Honors Student");
        System.out.print("Enter choice: ");
        int type = scanner.nextInt();
        scanner.nextLine();

        int id = studentIdCounter++;

        if (type == 1) {
            students.add(new RegularStudent(id, name, age, email, phone));
            System.out.println("Regular student added!");
        } else {
            students.add(new HonorsStudent(id, name, age, email, phone));
            System.out.println("Honors student added!");
        }

        System.out.println("--------------------------------------------");
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
    public static void recordGrade() throws StudentNotFoundException, GradeStorageFullException, InvalidGradeException {
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
            System.out.println("Student not found!");
            return;
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
            else {
                System.out.println("Invalid subject!");
                return;
            }

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
            else {
                System.out.println("Invalid subject!");
                return;
            }
        }

        System.out.print("Enter grade (0 - 100): ");
        double g = scanner.nextDouble();
        scanner.nextLine();

        if (g < 0 || g > 100) {
            throw new InvalidGradeException("Grade must be between 0 and 100. Received: " + g);
        }

        Grade newGrade = new Grade(id, subject, g);
//        newGrade.displayGradeDetails();
        String demoString = "Hello";

        gradeManager.addGrade(newGrade);

        System.out.println("\n Grade recorded successfully!");
        newGrade.displayGradeDetails();
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
            return;
        }

        // Count number of grades
        int gradeCount = 0;
        for (int i = 0; i < gradeManager.getGradeCount(); i++) {
            if (gradeManager.grades[i].getStudentId() == id) {
                gradeCount++;
            }
        }

        System.out.println("\nSTUDENT INFORMATION");
        System.out.println("Name: " + selected.name);
        System.out.println("Type: " + selected.getType());
        System.out.println("Grades Recorded: " + gradeCount);

        System.out.println("\nChoose export format:");
        System.out.println("1. Summary Report");
        System.out.println("2. Detailed Report");
        System.out.println("3. Both");
        System.out.print("Enter choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter file name (without extension): ");
        String fileName = scanner.nextLine();

        boolean summary = (choice == 1 || choice == 3);
        boolean detailed = (choice == 2 || choice == 3);

        StringBuilder summaryContent = new StringBuilder();
        StringBuilder detailedContent = new StringBuilder();

        // -------- SUMMARY REPORT CONTENT --------
        if (summary) {
            summaryContent.append("STUDENT SUMMARY REPORT\n");
            summaryContent.append("Name: ").append(selected.name).append("\n");
            summaryContent.append("Type: ").append(selected.getType()).append("\n");
            summaryContent.append("Total Grades: ").append(gradeCount).append("\n\n");
        }

        // -------- DETAILED REPORT CONTENT --------
        if (detailed) {
            detailedContent.append("DETAILED GRADE REPORT\n");
            detailedContent.append("Name: ").append(selected.name).append("\n");
            detailedContent.append("Type: ").append(selected.getType()).append("\n\n");

            detailedContent.append(String.format("%-20s %-10s\n", "SUBJECT", "GRADE"));
            detailedContent.append("------------------------------------\n");

            for (int i = 0; i < gradeManager.getGradeCount(); i++) {
                Grade g = gradeManager.grades[i];
                if (g.getStudentId() == id) {
                    detailedContent.append(String.format("%-20s %.2f\n",
                            g.getSubject().getSubjectName(),
                            g.getGrade()
                    ));
                }
            }
        }

        // -------- FILE EXPORT --------
        try {
            if (summary) {
                File file = new File(fileName + "_summary.txt");
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(summaryContent.toString());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                printExportInfo(file, "Summary");
            }

            if (detailed) {
                File file = new File(fileName + "_detailed.txt");
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(detailedContent.toString());
                }
                printExportInfo(file, "Detailed");
            }

        } catch (IOException e) {
            System.out.println("Error exporting report: " + e.getMessage());
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
        System.out.println("------------- BULK IMPORT GRADES ----------------");

        System.out.println("Place your CSV file in: ./imports/");
        System.out.println("\nRequired CSV Format:");
        System.out.println("StudentID,SubjectName,SubjectType,Grade\n");
        System.out.println("Example:");
        System.out.println("1001,Mathematics,Core,85");
        System.out.println("1003,Art,Elective,74\n");

        System.out.print("Enter file name (without extension): ");
        String fileName = scanner.nextLine().trim();

        String filePath = "./imports/" + fileName + ".csv";

        System.out.println("\nValidating file ...");

        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("File not found: " + filePath);
            return;
        }

        System.out.println("Processing grades ...\n");

        int totalRows = 0;
        int successCount = 0;
        int failedCount = 0;

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

                // Validate student ID
                int studentId;
                try {
                    studentId = Integer.parseInt(studentIdStr);
                } catch (Exception e) {
                    failedRecords.add("Row " + rowNum + ": Invalid Student ID → " + studentIdStr);
                    failedCount++;
                    rowNum++;
                    continue;
                }

                // Find student
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

                // Validate subject type
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

                // Validate grade
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

                // SUCCESS: Add grade
                Grade newGrade = new Grade(studentId, subject, gradeValue);
                gradeManager.addGrade(newGrade);
                successCount++;

                rowNum++;
            }

        } catch (Exception e) {
            System.out.println("Error while reading file: " + e.getMessage());
            return;
        }

        // Print Summary
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
        System.out.println("Grades added to the system: " + successCount);
        System.out.println("------------------------------------------------\n");
    }




}
