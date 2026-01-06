package validators;

/**
 * Single Responsibility Principle (SRP): Dedicated email validation
 * Open/Closed Principle (OCP): Can be extended for custom email rules
 */
public class EmailValidator implements IValidator<String> {
    private String errorMessage;

    @Override
    public boolean validate(String email) {
        errorMessage = "";
        
        if (email == null || email.trim().isEmpty()) {
            errorMessage = "Email cannot be empty";
            return false;
        }
        
        if (!email.contains("@")) {
            errorMessage = "Email must contain @ symbol";
            return false;
        }
        
        if (!email.contains(".")) {
            errorMessage = "Email must contain domain extension";
            return false;
        }
        
        String[] parts = email.split("@");
        if (parts.length != 2) {
            errorMessage = "Email format is invalid";
            return false;
        }
        
        if (parts[0].isEmpty()) {
            errorMessage = "Email local part cannot be empty";
            return false;
        }
        
        if (parts[1].isEmpty()) {
            errorMessage = "Email domain cannot be empty";
            return false;
        }
        
        return true;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}
