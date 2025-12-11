package exception;

public class InvalidReportFormatException extends Exception {

    // Constructor with message only
    public InvalidReportFormatException(String message) {
        super(message);
    }

    // Constructor with message and cause
    public InvalidReportFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}