import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Grade Class Tests")
class GradeTest {

    private Grade grade;
    private Subject coreSubject;
    private Subject electiveSubject;

    @BeforeEach
    void setUp() {
        coreSubject = new CoreSubject("Mathematics", "C-MATH");
        electiveSubject = new ElectiveSubject("Music", "E-MUS");
        grade = new Grade(1001, coreSubject, 85.5);
    }

    @Test
    @DisplayName("Should create grade with auto-incremented ID starting from 5000")
    void testGradeIdGeneration() {
        assertTrue(grade.getGradeId() >= 5000);
    }

    @Test
    @DisplayName("Should create grade with correct student ID")
    void testGradeStudentId() {
        assertEquals(1001, grade.getStudentId());
    }

    @Test
    @DisplayName("Should create grade with correct subject")
    void testGradeSubject() {
        assertEquals(coreSubject, grade.getSubject());
        assertEquals("Mathematics", grade.getSubject().getSubjectName());
    }

    @Test
    @DisplayName("Should create grade with correct numeric value")
    void testGradeValue() {
        assertEquals(85.5, grade.getGrade());
    }

    @Test
    @DisplayName("Should set date to current date")
    void testGradeDate() {
        assertEquals(LocalDate.now(), grade.getDate());
    }

    @Test
    @DisplayName("Should return letter grade A for grade >= 80")
    void testLetterGrade_A() {
        Grade gradeA = new Grade(1001, coreSubject, 85);
        assertEquals("A", gradeA.getLetterGrade());
    }

    @Test
    @DisplayName("Should return letter grade B for grade >= 70")
    void testLetterGrade_B() {
        Grade gradeB = new Grade(1001, coreSubject, 75);
        assertEquals("B", gradeB.getLetterGrade());
    }

    @Test
    @DisplayName("Should return letter grade C for grade >= 60")
    void testLetterGrade_C() {
        Grade gradeC = new Grade(1001, coreSubject, 65);
        assertEquals("C", gradeC.getLetterGrade());
    }

    @Test
    @DisplayName("Should return letter grade D for grade >= 50")
    void testLetterGrade_D() {
        Grade gradeD = new Grade(1001, coreSubject, 55);
        assertEquals("D", gradeD.getLetterGrade());
    }

    @Test
    @DisplayName("Should return letter grade F for grade < 50")
    void testLetterGrade_F() {
        Grade gradeF = new Grade(1001, coreSubject, 45);
        assertEquals("F", gradeF.getLetterGrade());
    }

    @Test
    @DisplayName("Should handle boundary grade 80 as A")
    void testBoundaryGrade_80() {
        Grade boundary = new Grade(1001, coreSubject, 80);
        assertEquals("A", boundary.getLetterGrade());
    }

    @Test
    @DisplayName("Should handle boundary grade 70 as B")
    void testBoundaryGrade_70() {
        Grade boundary = new Grade(1001, coreSubject, 70);
        assertEquals("B", boundary.getLetterGrade());
    }

    @Test
    @DisplayName("Should handle boundary grade 60 as C")
    void testBoundaryGrade_60() {
        Grade boundary = new Grade(1001, coreSubject, 60);
        assertEquals("C", boundary.getLetterGrade());
    }

    @Test
    @DisplayName("Should handle boundary grade 50 as D")
    void testBoundaryGrade_50() {
        Grade boundary = new Grade(1001, coreSubject, 50);
        assertEquals("D", boundary.getLetterGrade());
    }

    @Test
    @DisplayName("Should handle grade 0")
    void testGrade_Zero() {
        Grade zeroGrade = new Grade(1001, coreSubject, 0);
        assertEquals("F", zeroGrade.getLetterGrade());
        assertEquals(0, zeroGrade.getGrade());
    }

    @Test
    @DisplayName("Should handle grade 100")
    void testGrade_Perfect() {
        Grade perfectGrade = new Grade(1001, coreSubject, 100);
        assertEquals("A", perfectGrade.getLetterGrade());
        assertEquals(100, perfectGrade.getGrade());
    }

    @Test
    @DisplayName("Should create multiple grades with unique IDs")
    void testUniqueGradeIds() {
        Grade grade1 = new Grade(1001, coreSubject, 85);
        Grade grade2 = new Grade(1002, electiveSubject, 90);
        assertNotEquals(grade1.getGradeId(), grade2.getGradeId());
    }
}
