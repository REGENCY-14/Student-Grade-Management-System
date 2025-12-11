package exception;

public class StudentNotFoundException extends Exception {

    // Constructor with message only
    public StudentNotFoundException(String message) {
        super(message);
    }

    // Constructor with message and cause
    public StudentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}