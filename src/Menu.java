import java.util.ArrayList;
import java.util.Scanner;

public class Menu {

    static Scanner scanner = new Scanner(System.in);
    static ArrayList<Student> students = new ArrayList<>();
    static int studentIdCounter = 1000;
    static ArrayList<Grade> grades = new ArrayList<>();
    static GradeManager gradeManager = new GradeManager();



    public static void main(String[] args) {

        addDefaultStudents();

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
        System.out.println("5. Exit");

        System.out.print("Enter choice: ");
    }

    // ADD DEFAULT STUDENTS
    public static void addDefaultStudents() {
        students.add(new RegularStudent(studentIdCounter++, "Alice Brown", 20, "alice@mail.com", "0240000000"));
        students.add(new RegularStudent(studentIdCounter++, "John Mensah", 22, "john@mail.com", "0241111111"));
        students.add(new RegularStudent(studentIdCounter++, "Ama Serwaa", 21, "ama@mail.com", "0242222222"));

        students.add(new HonorsStudent(studentIdCounter++, "Kofi Asare", 23, "kofi@mail.com", "0243333333"));
        students.add(new HonorsStudent(studentIdCounter++, "Sandra Owusu", 19, "sandra@mail.com", "0244444444"));

        students.get(0).setAverageGrade(72);
        students.get(1).setAverageGrade(55);
        students.get(2).setAverageGrade(49);
        students.get(3).setAverageGrade(83);
        students.get(4).setAverageGrade(65);
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
        System.out.println("\nSTUDENT LIST");
        System.out.printf("%-6s %-20s %-10s %-12s %-10s\n",
                "STU ID", "NAME", "TYPE", "AVG GRADE", "STATUS");
        System.out.println("------------------------------------------------------");

        for (Student s : students) {
            System.out.printf("%-6d %-20s %-10s %-12.2f %-10s\n",
                    s.id, s.name, s.getType(), s.getAverageGrade(), s.status);
        }

        System.out.println("------------------------------------------------------\n");
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
            System.out.println("❌ Student not found!");
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

        gradeManager.addGrade(newGrade);

        System.out.println("\n✔ Grade recorded successfully!");
        newGrade.displayGradeDetails();
    }



    // VIEW GRADE REPORT
     public static void viewGradeReport() {
        System.out.print("Enter student ID: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        gradeManager.viewGradeByStudent(id);
    }
}
