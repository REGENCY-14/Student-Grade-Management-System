import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Student Class Tests")
class StudentTest {

    private RegularStudent regularStudent;
    private HonorsStudent honorsStudent;

    @BeforeEach
    void setUp() {
        regularStudent = new RegularStudent(1001, "John Doe", 18, "john@email.com", "1234567890");
        honorsStudent = new HonorsStudent(1002, "Jane Smith", 19, "jane@email.com", "0987654321");
        // ApplicationContext is initialized with test data if needed
    }

    @Test
    @DisplayName("Should create regular student with correct attributes")
    void testRegularStudentCreation() {
        assertEquals(1001, regularStudent.id);
        assertEquals("John Doe", regularStudent.name);
        assertEquals(18, regularStudent.age);
        assertEquals("john@email.com", regularStudent.email);
        assertEquals("1234567890", regularStudent.phone);
        assertEquals("Regular", regularStudent.getType());
    }

    @Test
    @DisplayName("Should create honors student with correct attributes")
    void testHonorsStudentCreation() {
        assertEquals(1002, honorsStudent.id);
        assertEquals("Jane Smith", honorsStudent.name);
        assertEquals("Honors", honorsStudent.getType());
    }

    @Test
    @DisplayName("Regular student passing grade should be 50")
    void testRegularStudentPassingGrade() {
        assertEquals(50, regularStudent.getPassingGrade());
    }

    @Test
    @DisplayName("Honors student passing grade should be 60")
    void testHonorsStudentPassingGrade() {
        assertEquals(60, honorsStudent.getPassingGrade());
    }

    @Test
    @DisplayName("Should calculate correct GPA for grade 85")
    void testGradeToGPA_A() {
        assertEquals(4.0, regularStudent.gradeToGPA(85));
    }

    @Test
    @DisplayName("Should calculate correct GPA for grade 75")
    void testGradeToGPA_B() {
        assertEquals(3.0, regularStudent.gradeToGPA(75));
    }

    @Test
    @DisplayName("Should calculate correct GPA for grade 65")
    void testGradeToGPA_C() {
        assertEquals(2.0, regularStudent.gradeToGPA(65));
    }

    @Test
    @DisplayName("Should calculate correct GPA for grade 55")
    void testGradeToGPA_D() {
        assertEquals(1.0, regularStudent.gradeToGPA(55));
    }

    @Test
    @DisplayName("Should calculate correct GPA for grade 45")
    void testGradeToGPA_F() {
        assertEquals(0.0, regularStudent.gradeToGPA(45));
    }

    @Test
    @DisplayName("Should return passing status for regular student with grade >= 50")
    void testRegularStudentPassingStatus() {
        regularStudent.setAverageGrade(60.0);
        assertEquals("Passing", regularStudent.getStatus());
    }

    @Test
    @DisplayName("Should return failing status for regular student with grade < 50")
    void testRegularStudentFailingStatus() {
        regularStudent.setAverageGrade(40.0);
        assertEquals("Failing", regularStudent.getStatus());
    }

    @Test
    @DisplayName("Should return passing status for honors student with grade >= 60")
    void testHonorsStudentPassingStatus() {
        honorsStudent.setAverageGrade(70.0);
        assertEquals("Passing  ", honorsStudent.getStatus());
    }

    @Test
    @DisplayName("Should return failing status for honors student with grade < 60")
    void testHonorsStudentFailingStatus() {
        honorsStudent.setAverageGrade(55.0);
        assertEquals("Failing", honorsStudent.getStatus());
    }

    @Test
    @DisplayName("Should set and get average grade correctly")
    void testSetAndGetAverageGrade() {
        regularStudent.setAverageGrade(75.5);
        assertEquals(75.5, regularStudent.getAverageGrade());
    }

    @Test
    @DisplayName("Should return 0 enrolled subjects initially")
    void testInitialEnrolledSubjects() {
        assertEquals(0, regularStudent.getEnrolledSubjects());
    }

    @Test
    @DisplayName("Should compute GPA as 0 when no grades recorded")
    void testComputeGPA_NoGrades() {
        assertEquals(0.0, regularStudent.computeGPA());
    }
}
