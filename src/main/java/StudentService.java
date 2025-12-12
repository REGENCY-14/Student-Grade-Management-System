import java.util.ArrayList;

public class StudentService {

    private ArrayList<Student> students;
    private int studentIdCounter;

    public StudentService(ArrayList<Student> students, int studentIdCounter) {
        this.students = students;
        this.studentIdCounter = studentIdCounter;
    }

    // Generate unique student ID
    private int generateStudentId() {
        return studentIdCounter++;
    }

    // ------------------ Add Student ------------------
    public Student addStudent(String name, int age, String email, String phone) {

        Student student = new Student(generateStudentId(), name, age, email, phone) {
            @Override
            public String getType() {
                return "";
            }

            @Override
            public int getPassingGrade() {
                return 0;
            }

            @Override
            public String getStatus() {
                return "";
            }
        };
        students.add(student);

        return student;
    }

    // ------------------ View All Students ------------------
    public ArrayList<Student> getAllStudents() {
        return students;
    }

    // ------------------ Find Student by ID ------------------
    public Student findStudentById(int id) {
        for (Student student : students) {
            if (student.getId() == id) return student;
        }
        return null;
    }

    // ------------------ Validators ------------------
    public boolean isValidAge(int age) {
        return age >= 10 && age <= 100;
    }

    public boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }

    public boolean isValidPhone(String phone) {
        return phone.length() >= 10;
    }

}
