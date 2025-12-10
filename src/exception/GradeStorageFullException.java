package exception;

public class GradeStorageFullException extends Exception {

    // Constructor with message only
    public GradeStorageFullException(String message) {
        super(message);
    }

    // Constructor with message and cause
    public GradeStorageFullException(String message, Throwable cause) {
        super(message, cause);
    }
}