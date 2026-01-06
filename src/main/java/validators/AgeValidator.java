package validators;

/**
 * Single Responsibility Principle (SRP): Dedicated age validation
 * Open/Closed Principle (OCP): Configurable min/max age bounds
 */
public class AgeValidator implements IValidator<Integer> {
    private String errorMessage;
    private final int minAge;
    private final int maxAge;

    public AgeValidator() {
        this(5, 100);  // Default bounds
    }

    public AgeValidator(int minAge, int maxAge) {
        this.minAge = minAge;
        this.maxAge = maxAge;
    }

    @Override
    public boolean validate(Integer age) {
        errorMessage = "";
        
        if (age == null) {
            errorMessage = "Age cannot be null";
            return false;
        }
        
        if (age < minAge) {
            errorMessage = "Age must be at least " + minAge;
            return false;
        }
        
        if (age > maxAge) {
            errorMessage = "Age must not exceed " + maxAge;
            return false;
        }
        
        return true;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}
