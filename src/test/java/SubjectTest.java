import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import core.CoreSubject;
import core.ElectiveSubject;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Subject Class Tests")
class SubjectTest {

    private CoreSubject coreSubject;
    private ElectiveSubject electiveSubject;

    @BeforeEach
    void setUp() {
        coreSubject = new CoreSubject("Mathematics", "C-MATH");
        electiveSubject = new ElectiveSubject("Music", "E-MUS");
    }

    @Test
    @DisplayName("Should create core subject with correct name")
    void testCoreSubjectName() {
        assertEquals("Mathematics", coreSubject.getSubjectName());
    }

    @Test
    @DisplayName("Should create core subject with correct code")
    void testCoreSubjectCode() {
        assertEquals("C-MATH", coreSubject.getSubjectCode());
    }

    @Test
    @DisplayName("Core subject type should be 'Core'")
    void testCoreSubjectType() {
        assertEquals("Core", coreSubject.getSubjectType());
    }

    @Test
    @DisplayName("Core subject should be mandatory")
    void testCoreSubjectMandatory() {
        assertTrue(coreSubject.isMandatory());
    }

    @Test
    @DisplayName("Should create elective subject with correct name")
    void testElectiveSubjectName() {
        assertEquals("Music", electiveSubject.getSubjectName());
    }

    @Test
    @DisplayName("Should create elective subject with correct code")
    void testElectiveSubjectCode() {
        assertEquals("E-MUS", electiveSubject.getSubjectCode());
    }

    @Test
    @DisplayName("Elective subject type should be 'Elective'")
    void testElectiveSubjectType() {
        assertEquals("Elective", electiveSubject.getSubjectType());
    }

    @Test
    @DisplayName("Elective subject should not be mandatory")
    void testElectiveSubjectNotMandatory() {
        assertFalse(electiveSubject.isMandatory());
    }

    @Test
    @DisplayName("Should create multiple core subjects")
    void testMultipleCoreSubjects() {
        CoreSubject english = new CoreSubject("English", "C-ENG");
        CoreSubject science = new CoreSubject("Science", "C-SCI");

        assertEquals("English", english.getSubjectName());
        assertEquals("Science", science.getSubjectName());
        assertTrue(english.isMandatory());
        assertTrue(science.isMandatory());
    }

    @Test
    @DisplayName("Should create multiple elective subjects")
    void testMultipleElectiveSubjects() {
        ElectiveSubject art = new ElectiveSubject("Art", "E-ART");
        ElectiveSubject pe = new ElectiveSubject("Physical Education", "E-PE");

        assertEquals("Art", art.getSubjectName());
        assertEquals("Physical Education", pe.getSubjectName());
        assertFalse(art.isMandatory());
        assertFalse(pe.isMandatory());
    }
}
