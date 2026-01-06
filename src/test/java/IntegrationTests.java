import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Integration Tests")
class IntegrationTests {

    private GradeManager gradeManager;
    private RegularStudent regularStudent;
    private HonorsStudent honorsStudent;

    @BeforeEach
    void setUp() {
        gradeManager = new GradeManager();
        regularStudent = new RegularStudent(1001, "John Doe", 18, "john@email.com", "1234567890");
        honorsStudent = new HonorsStudent(1002, "Jane Smith", 19, "jane@email.com", "0987654321");

        // Initialize ApplicationContext with test data
        ArrayList<Student> students = ApplicationContext.getInstance().getStudents();
        students.clear();
        students.add(regularStudent);
        students.add(honorsStudent);
    }

    @Test
    @DisplayName("Complete workflow: Add student, record grades, calculate averages")
    void testCompleteWorkflow() throws Exception {
        // Add grades for regular student
        CoreSubject math = new CoreSubject("Mathematics", "C-MATH");
        CoreSubject english = new CoreSubject("English", "C-ENG");
        ElectiveSubject music = new ElectiveSubject("Music", "E-MUS");

        Grade grade1 = new Grade(1001, math, 85.0);
        Grade grade2 = new Grade(1001, english, 90.0);
        Grade grade3 = new Grade(1001, music, 80.0);

        gradeManager.addGrade(grade1);
        gradeManager.addGrade(grade2);
        gradeManager.addGrade(grade3);

        // Verify grade count
        assertEquals(3, gradeManager.getGradeCount());

        // Verify averages
        double coreAvg = gradeManager.calculateCoreAverage(1001);
        double electiveAvg = gradeManager.calculateElectiveAverage(1001);
        double overallAvg = gradeManager.calculateOverallAverage(1001);

        assertEquals(87.5, coreAvg); // (85 + 90) / 2
        assertEquals(80.0, electiveAvg);
        assertEquals(85.0, overallAvg); // (85 + 90 + 80) / 3

        // Verify student status
        assertEquals("Passing", regularStudent.getStatus());
    }

    @Test
    @DisplayName("Multiple students with different grades and statuses")
    void testMultipleStudentsIntegration() throws Exception {
        CoreSubject math = new CoreSubject("Mathematics", "C-MATH");

        // Regular student with passing grade
        Grade grade1 = new Grade(1001, math, 80.0);
        gradeManager.addGrade(grade1);

        // Honors student with failing grade
        Grade grade2 = new Grade(1002, math, 55.0);
        gradeManager.addGrade(grade2);

        assertEquals("Passing", regularStudent.getStatus());
        assertEquals("Failing", honorsStudent.getStatus()); // Needs 60 to pass
    }

    @Test
    @DisplayName("GPA calculation integration test")
    void testGPACalculationIntegration() throws Exception {
        CoreSubject math = new CoreSubject("Mathematics", "C-MATH");
        CoreSubject english = new CoreSubject("English", "C-ENG");

        // Grades: 85 (4.0 GPA) and 75 (3.0 GPA)
        Grade grade1 = new Grade(1001, math, 85.0);
        Grade grade2 = new Grade(1001, english, 75.0);

        gradeManager.addGrade(grade1);
        gradeManager.addGrade(grade2);

        double gpa = regularStudent.computeGPA();
        assertEquals(3.5, gpa); // (4.0 + 3.0) / 2
    }

    @Test
    @DisplayName("Letter grade conversion integration")
    void testLetterGradeIntegration() throws Exception {
        CoreSubject math = new CoreSubject("Mathematics", "C-MATH");

        Grade gradeA = new Grade(1001, math, 85.0);
        Grade gradeB = new Grade(1001, math, 75.0);
        Grade gradeC = new Grade(1001, math, 65.0);
        Grade gradeD = new Grade(1001, math, 55.0);
        Grade gradeF = new Grade(1001, math, 45.0);

        assertEquals("A", gradeA.getLetterGrade());
        assertEquals("B", gradeB.getLetterGrade());
        assertEquals("C", gradeC.getLetterGrade());
        assertEquals("D", gradeD.getLetterGrade());
        assertEquals("F", gradeF.getLetterGrade());
    }

    @Test
    @DisplayName("Subject type integration - Core vs Elective")
    void testSubjectTypeIntegration() throws Exception {
        CoreSubject math = new CoreSubject("Mathematics", "C-MATH");
        ElectiveSubject music = new ElectiveSubject("Music", "E-MUS");

        Grade coreGrade = new Grade(1001, math, 80.0);
        Grade electiveGrade = new Grade(1001, music, 90.0);

        gradeManager.addGrade(coreGrade);
        gradeManager.addGrade(electiveGrade);

        assertEquals("Core", math.getSubjectType());
        assertEquals("Elective", music.getSubjectType());
        assertTrue(math.isMandatory());
        assertFalse(music.isMandatory());
    }

    @Test
    @DisplayName("Regular vs Honors student passing requirements")
    void testStudentTypePassingRequirements() throws Exception {
        CoreSubject math = new CoreSubject("Mathematics", "C-MATH");

        // Grade 55 - passes regular, fails honors
        Grade regularGrade = new Grade(1001, math, 55.0);
        Grade honorsGrade = new Grade(1002, math, 55.0);

        gradeManager.addGrade(regularGrade);
        gradeManager.addGrade(honorsGrade);

        assertEquals(50, regularStudent.getPassingGrade());
        assertEquals(60, honorsStudent.getPassingGrade());
        assertEquals("Passing", regularStudent.getStatus());
        assertEquals("Failing", honorsStudent.getStatus());
    }

    @Test
    @DisplayName("Average calculation updates correctly after multiple grade additions")
    void testAverageUpdateIntegration() throws Exception {
        CoreSubject math = new CoreSubject("Mathematics", "C-MATH");

        Grade grade1 = new Grade(1001, math, 80.0);
        gradeManager.addGrade(grade1);
        assertEquals(80.0, regularStudent.getAverageGrade());

        Grade grade2 = new Grade(1001, math, 90.0);
        gradeManager.addGrade(grade2);
        assertEquals(85.0, regularStudent.getAverageGrade());

        Grade grade3 = new Grade(1001, math, 70.0);
        gradeManager.addGrade(grade3);
        assertEquals(80.0, regularStudent.getAverageGrade());
    }

    @Test
    @DisplayName("Edge case: Student with only core subjects")
    void testOnlyCoreSubjects() throws Exception {
        CoreSubject math = new CoreSubject("Mathematics", "C-MATH");
        CoreSubject english = new CoreSubject("English", "C-ENG");

        Grade grade1 = new Grade(1001, math, 80.0);
        Grade grade2 = new Grade(1001, english, 90.0);

        gradeManager.addGrade(grade1);
        gradeManager.addGrade(grade2);

        double coreAvg = gradeManager.calculateCoreAverage(1001);
        double electiveAvg = gradeManager.calculateElectiveAverage(1001);

        assertEquals(85.0, coreAvg);
        assertEquals(-1, electiveAvg); // No elective grades
    }

    @Test
    @DisplayName("Edge case: Student with only elective subjects")
    void testOnlyElectiveSubjects() throws Exception {
        ElectiveSubject music = new ElectiveSubject("Music", "E-MUS");
        ElectiveSubject art = new ElectiveSubject("Art", "E-ART");

        Grade grade1 = new Grade(1001, music, 80.0);
        Grade grade2 = new Grade(1001, art, 90.0);

        gradeManager.addGrade(grade1);
        gradeManager.addGrade(grade2);

        double coreAvg = gradeManager.calculateCoreAverage(1001);
        double electiveAvg = gradeManager.calculateElectiveAverage(1001);

        assertEquals(-1, coreAvg); // No core grades
        assertEquals(85.0, electiveAvg);
    }

    @Test
    @DisplayName("Edge case: Zero grade handling")
    void testZeroGradeHandling() throws Exception {
        CoreSubject math = new CoreSubject("Mathematics", "C-MATH");
        Grade zeroGrade = new Grade(1001, math, 0.0);

        gradeManager.addGrade(zeroGrade);

        assertEquals(0.0, regularStudent.getAverageGrade());
        assertEquals("F", zeroGrade.getLetterGrade());
        assertEquals(0.0, regularStudent.gradeToGPA(0));
    }

    @Test
    @DisplayName("Edge case: Perfect score handling")
    void testPerfectScoreHandling() throws Exception {
        CoreSubject math = new CoreSubject("Mathematics", "C-MATH");
        Grade perfectGrade = new Grade(1001, math, 100.0);

        gradeManager.addGrade(perfectGrade);

        assertEquals(100.0, regularStudent.getAverageGrade());
        assertEquals("A", perfectGrade.getLetterGrade());
        assertEquals(4.0, regularStudent.gradeToGPA(100));
    }
}
