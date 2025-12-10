package exception;

public class FileImportException extends Exception {

    // Constructor with message only
    public FileImportException(String message) {
        super(message);
    }

    // Constructor with message and cause
    public FileImportException(String message, Throwable cause) {
        super(message, cause);
    }
}