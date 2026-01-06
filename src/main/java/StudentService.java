import java.util.ArrayList;
import java.util.stream.Collectors;

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

    // ------------------ Find Student by ID (Stream-based) ------------------
    public Student findStudentById(int id) {
        return students.stream()
                .filter(student -> student.getId() == id)
                .findFirst()
                .orElse(null);
    }

    // Get all students matching a filter condition (Stream-based)
    public ArrayList<Student> getStudentsByFilter(java.util.function.Predicate<Student> filter) {
        return students.stream()
                .filter(filter)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    // Get all students of a specific type (Stream-based)
    public ArrayList<Student> getStudentsByType(String type) {
        return students.stream()
                .filter(s -> s.getType().equals(type))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    // Get all active/valid students (Stream-based)
    public ArrayList<Student> getValidStudents() {
        return students.stream()
                .filter(s -> isValidStudent(s))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    // Validate entire student list (Stream-based)
    private boolean isValidStudent(Student s) {
        return s != null && isValidAge(s.getAge()) && isValidEmail(s.getEmail()) && isValidPhone(s.getPhone());
    }

    // Count students with condition (Stream-based)
    public long countStudentsByType(String type) {
        return students.stream()
                .filter(s -> s.getType().equals(type))
                .count();
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
