package exception;

public class InvalidStudentDataException extends Exception {

    // Constructor with message only
    public InvalidStudentDataException(String message) {
        super(message);
    }

    // Constructor with message and cause
    public InvalidStudentDataException(String message, Throwable cause) {
        super(message, cause);
    }
}