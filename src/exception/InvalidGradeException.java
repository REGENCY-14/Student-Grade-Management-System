package exception;

public class InvalidGradeException extends Exception {

    // Constructor with message only
    public InvalidGradeException(String message) {
        super(message);
    }

    // Constructor with message and cause
    public InvalidGradeException(String message, Throwable cause) {
        super(message, cause);
    }
}