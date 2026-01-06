package validators;

/**
 * Single Responsibility Principle (SRP): Dedicated name validation
 * Open/Closed Principle (OCP): Can be extended with regex patterns
 */
public class NameValidator implements IValidator<String> {
    private String errorMessage;
    private static final int MAX_LENGTH = 100;
    private static final int MIN_LENGTH = 2;

    @Override
    public boolean validate(String name) {
        errorMessage = "";
        
        if (name == null || name.trim().isEmpty()) {
            errorMessage = "Name cannot be empty";
            return false;
        }
        
        name = name.trim();
        
        if (name.length() < MIN_LENGTH) {
            errorMessage = "Name must be at least " + MIN_LENGTH + " characters";
            return false;
        }
        
        if (name.length() > MAX_LENGTH) {
            errorMessage = "Name must not exceed " + MAX_LENGTH + " characters";
            return false;
        }
        
        return true;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}
