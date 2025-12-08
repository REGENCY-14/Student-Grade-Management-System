import java.util.ArrayList;
import java.util.Scanner;

public class Menu {

    static Scanner scanner = new Scanner(System.in);
    static ArrayList<Student> students = new ArrayList<>();
    static int studentIdCounter = 1000;
    static ArrayList<Grade> grades = new ArrayList<>();
    static GradeManager gradeManager = new GradeManager();



    public static void main(String[] args) {

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
            } else if (choice == 5) {
                System.out.println("Enter student ID: ");
                int studentId = scanner.nextInt();
                viewStudentGPAReport(studentId);
            } else if (choice == 6) {
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
        System.out.println("5. Calculate Student GPA");
        System.out.println("6. Exit");

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
    public static void recordGrade() {
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
            System.out.println("Invalid grade! Must be between 0 and 100.");
            return;
        }

        Grade newGrade = new Grade(id, subject, g);
//        newGrade.displayGradeDetails();
        String demoString = "Hello";

        gradeManager.addGrade(newGrade);

        System.out.println("\n Grade recorded successfully!");
        newGrade.displayGradeDetails();
    }



    // VIEW GRADE REPORT
     public static void viewGradeReport() {
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


}
