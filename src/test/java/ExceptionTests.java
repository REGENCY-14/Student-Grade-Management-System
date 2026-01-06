import exception.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Custom Exception Tests")
class ExceptionTests {

    @Test
    @DisplayName("StudentNotFoundException should be created with message")
    void testStudentNotFoundException() {
        StudentNotFoundException exception = new StudentNotFoundException("Student not found");
        assertEquals("Student not found", exception.getMessage());
    }

    @Test
    @DisplayName("StudentNotFoundException should be created with message and cause")
    void testStudentNotFoundExceptionWithCause() {
        Throwable cause = new RuntimeException("Root cause");
        StudentNotFoundException exception = new StudentNotFoundException("Student not found", cause);

        assertEquals("Student not found", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("InvalidGradeException should be created with message")
    void testInvalidGradeException() {
        InvalidGradeException exception = new InvalidGradeException("Invalid grade value");
        assertEquals("Invalid grade value", exception.getMessage());
    }

    @Test
    @DisplayName("InvalidGradeException should be created with message and cause")
    void testInvalidGradeExceptionWithCause() {
        Throwable cause = new NumberFormatException("Not a number");
        InvalidGradeException exception = new InvalidGradeException("Invalid grade", cause);

        assertEquals("Invalid grade", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("InvalidReportFormatException should be created with message")
    void testInvalidReportFormatException() {
        InvalidReportFormatException exception = new InvalidReportFormatException("Invalid format");
        assertEquals("Invalid format", exception.getMessage());
    }

    @Test
    @DisplayName("InvalidReportFormatException should be created with message and cause")
    void testInvalidReportFormatExceptionWithCause() {
        Throwable cause = new IllegalArgumentException("Bad argument");
        InvalidReportFormatException exception = new InvalidReportFormatException("Invalid format", cause);

        assertEquals("Invalid format", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("InvalidStudentDataException should be created with message")
    void testInvalidStudentDataException() {
        InvalidStudentDataException exception = new InvalidStudentDataException("Invalid data");
        assertEquals("Invalid data", exception.getMessage());
    }

    @Test
    @DisplayName("SubjectNotFoundException should be created with message")
    void testSubjectNotFoundException() {
        SubjectNotFoundException exception = new SubjectNotFoundException("Subject not found");
        assertEquals("Subject not found", exception.getMessage());
    }

    @Test
    @DisplayName("GradeStorageFullException should be created with message")
    void testGradeStorageFullException() {
        GradeStorageFullException exception = new GradeStorageFullException("Storage full");
        assertEquals("Storage full", exception.getMessage());
    }

    @Test
    @DisplayName("FileImportException should be created with message")
    void testFileImportException() {
        FileImportException exception = new FileImportException("File import failed");
        assertEquals("File import failed", exception.getMessage());
    }

    @Test
    @DisplayName("All exceptions should extend Exception")
    void testExceptionsExtendException() {
        assertTrue(Exception.class.isAssignableFrom(StudentNotFoundException.class));
        assertTrue(Exception.class.isAssignableFrom(InvalidGradeException.class));
        assertTrue(Exception.class.isAssignableFrom(InvalidReportFormatException.class));
        assertTrue(Exception.class.isAssignableFrom(InvalidStudentDataException.class));
        assertTrue(Exception.class.isAssignableFrom(SubjectNotFoundException.class));
        assertTrue(Exception.class.isAssignableFrom(GradeStorageFullException.class));
        assertTrue(Exception.class.isAssignableFrom(FileImportException.class));
    }
}
