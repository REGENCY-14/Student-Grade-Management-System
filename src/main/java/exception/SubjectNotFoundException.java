package exception;

public class SubjectNotFoundException extends Exception {

    // Constructor with message only
    public SubjectNotFoundException(String message) {
        super(message);
    }

    // Constructor with message and cause
    public SubjectNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}