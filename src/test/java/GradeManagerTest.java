import exception.StudentNotFoundException;
import exception.GradeStorageFullException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("GradeManager Class Tests")
class GradeManagerTest {

    private GradeManager gradeManager;
    private RegularStudent student1;
    private HonorsStudent student2;
    private CoreSubject mathSubject;
    private ElectiveSubject musicSubject;

    @BeforeEach
    void setUp() {
        gradeManager = new GradeManager();
        student1 = new RegularStudent(1001, "John Doe", 18, "john@email.com", "1234567890");
        student2 = new HonorsStudent(1002, "Jane Smith", 19, "jane@email.com", "0987654321");
        mathSubject = new CoreSubject("Mathematics", "C-MATH");
        musicSubject = new ElectiveSubject("Music", "E-MUS");

        Menu.students = new ArrayList<>();
        Menu.students.add(student1);
        Menu.students.add(student2);
        Menu.gradeManager = gradeManager;
    }

    @Test
    @DisplayName("Should add grade successfully for existing student")
    void testAddGrade_Success() throws Exception {
        Grade grade = new Grade(1001, mathSubject, 85.0);
        gradeManager.addGrade(grade);

        assertEquals(1, gradeManager.getGradeCount());
    }

    @Test
    @DisplayName("Should throw StudentNotFoundException when student doesn't exist")
    void testAddGrade_StudentNotFound() {
        Grade grade = new Grade(9999, mathSubject, 85.0);

        assertThrows(StudentNotFoundException.class, () -> {
            gradeManager.addGrade(grade);
        });
    }

    @Test
    @DisplayName("Should update student average after adding grade")
    void testAddGrade_UpdatesAverage() throws Exception {
        Grade grade1 = new Grade(1001, mathSubject, 80.0);
        Grade grade2 = new Grade(1001, musicSubject, 90.0);

        gradeManager.addGrade(grade1);
        gradeManager.addGrade(grade2);

        assertEquals(85.0, student1.getAverageGrade());
    }

    @Test
    @DisplayName("Should calculate core average correctly")
    void testCalculateCoreAverage() throws Exception {
        Grade grade1 = new Grade(1001, mathSubject, 80.0);
        CoreSubject science = new CoreSubject("Science", "C-SCI");
        Grade grade2 = new Grade(1001, science, 90.0);

        gradeManager.addGrade(grade1);
        gradeManager.addGrade(grade2);

        double coreAvg = gradeManager.calculateCoreAverage(1001);
        assertEquals(85.0, coreAvg);
    }

    @Test
    @DisplayName("Should calculate elective average correctly")
    void testCalculateElectiveAverage() throws Exception {
        Grade grade1 = new Grade(1001, musicSubject, 70.0);
        ElectiveSubject art = new ElectiveSubject("Art", "E-ART");
        Grade grade2 = new Grade(1001, art, 80.0);

        gradeManager.addGrade(grade1);
        gradeManager.addGrade(grade2);

        double electiveAvg = gradeManager.calculateElectiveAverage(1001);
        assertEquals(75.0, electiveAvg);
    }

    @Test
    @DisplayName("Should return -1 for core average when no core grades exist")
    void testCalculateCoreAverage_NoGrades() {
        double coreAvg = gradeManager.calculateCoreAverage(1001);
        assertEquals(-1, coreAvg);
    }

    @Test
    @DisplayName("Should return -1 for elective average when no elective grades exist")
    void testCalculateElectiveAverage_NoGrades() {
        double electiveAvg = gradeManager.calculateElectiveAverage(1001);
        assertEquals(-1, electiveAvg);
    }

    @Test
    @DisplayName("Should calculate overall average correctly")
    void testCalculateOverallAverage() throws Exception {
        Grade grade1 = new Grade(1001, mathSubject, 80.0);
        Grade grade2 = new Grade(1001, musicSubject, 90.0);
        Grade grade3 = new Grade(1001, mathSubject, 70.0);

        gradeManager.addGrade(grade1);
        gradeManager.addGrade(grade2);
        gradeManager.addGrade(grade3);

        double overallAvg = gradeManager.calculateOverallAverage(1001);
        assertEquals(80.0, overallAvg);
    }

    @Test
    @DisplayName("Should throw StudentNotFoundException for overall average of non-existent student")
    void testCalculateOverallAverage_StudentNotFound() {
        assertThrows(StudentNotFoundException.class, () -> {
            gradeManager.calculateOverallAverage(9999);
        });
    }

    @Test
    @DisplayName("Should return -1 for overall average when no grades exist")
    void testCalculateOverallAverage_NoGrades() throws Exception {
        double overallAvg = gradeManager.calculateOverallAverage(1001);
        assertEquals(-1, overallAvg);
    }

    @Test
    @DisplayName("Should get correct subject count for student")
    void testGetSubjectCountForStudent() throws Exception {
        Grade grade1 = new Grade(1001, mathSubject, 85.0);
        Grade grade2 = new Grade(1001, musicSubject, 90.0);
        Grade grade3 = new Grade(1002, mathSubject, 75.0);

        gradeManager.addGrade(grade1);
        gradeManager.addGrade(grade2);
        gradeManager.addGrade(grade3);

        assertEquals(2, gradeManager.getSubjectCountForStudent(1001));
        assertEquals(1, gradeManager.getSubjectCountForStudent(1002));
    }

    @Test
    @DisplayName("Should get grades for specific student")
    void testGetGradesForStudent() throws Exception {
        Grade grade1 = new Grade(1001, mathSubject, 85.0);
        Grade grade2 = new Grade(1001, musicSubject, 90.0);
        Grade grade3 = new Grade(1002, mathSubject, 75.0);

        gradeManager.addGrade(grade1);
        gradeManager.addGrade(grade2);
        gradeManager.addGrade(grade3);

        ArrayList<Integer> student1Grades = gradeManager.getGradesForStudent(1001);
        assertEquals(2, student1Grades.size());
        assertTrue(student1Grades.contains(85));
        assertTrue(student1Grades.contains(90));
    }

    @Test
    @DisplayName("Should handle multiple students with different grades")
    void testMultipleStudents() throws Exception {
        Grade grade1 = new Grade(1001, mathSubject, 80.0);
        Grade grade2 = new Grade(1002, mathSubject, 90.0);

        gradeManager.addGrade(grade1);
        gradeManager.addGrade(grade2);

        assertEquals(80.0, student1.getAverageGrade());
        assertEquals(90.0, student2.getAverageGrade());
    }

    @Test
    @DisplayName("Should throw StudentNotFoundException when viewing grades for non-existent student")
    void testViewGradeByStudent_StudentNotFound() {
        assertThrows(StudentNotFoundException.class, () -> {
            gradeManager.viewGradeByStudent(9999);
        });
    }

    @Test
    @DisplayName("Should handle empty grade list when viewing")
    void testViewGradeByStudent_NoGrades() throws Exception {
        // This should not throw exception, just show no grades
        assertDoesNotThrow(() -> {
            gradeManager.viewGradeByStudent(1001);
        });
    }

    @Test
    @DisplayName("Should correctly separate core and elective grades")
    void testSeparateCoreAndElectiveGrades() throws Exception {
        Grade coreGrade = new Grade(1001, mathSubject, 80.0);
        Grade electiveGrade = new Grade(1001, musicSubject, 90.0);

        gradeManager.addGrade(coreGrade);
        gradeManager.addGrade(electiveGrade);

        double coreAvg = gradeManager.calculateCoreAverage(1001);
        double electiveAvg = gradeManager.calculateElectiveAverage(1001);

        assertEquals(80.0, coreAvg);
        assertEquals(90.0, electiveAvg);
    }
}