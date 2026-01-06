package validators;

/**
 * Single Responsibility Principle (SRP): Dedicated phone validation
 * Open/Closed Principle (OCP): Can be extended for regional phone formats
 */
public class PhoneValidator implements IValidator<String> {
    private String errorMessage;
    private static final int MIN_LENGTH = 10;
    private static final int MAX_LENGTH = 20;

    @Override
    public boolean validate(String phone) {
        errorMessage = "";
        
        if (phone == null || phone.trim().isEmpty()) {
            errorMessage = "Phone cannot be empty";
            return false;
        }
        
        phone = phone.trim();
        
        if (phone.length() < MIN_LENGTH) {
            errorMessage = "Phone number must be at least " + MIN_LENGTH + " characters";
            return false;
        }
        
        if (phone.length() > MAX_LENGTH) {
            errorMessage = "Phone number must not exceed " + MAX_LENGTH + " characters";
            return false;
        }
        
        return true;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}
