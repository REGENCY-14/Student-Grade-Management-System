package validators;

/**
 * Single Responsibility Principle (SRP): Composite validator for student data
 * Uses multiple specialized validators to validate complete student information
 * Demonstrates composition over inheritance
 */
public class StudentDataValidator implements IValidator<StudentDataValidator.StudentData> {
    private String errorMessage;
    
    private final NameValidator nameValidator;
    private final AgeValidator ageValidator;
    private final EmailValidator emailValidator;
    private final PhoneValidator phoneValidator;

    public StudentDataValidator() {
        this(new NameValidator(), new AgeValidator(), new EmailValidator(), new PhoneValidator());
    }

    public StudentDataValidator(NameValidator nameValidator, AgeValidator ageValidator, 
                               EmailValidator emailValidator, PhoneValidator phoneValidator) {
        this.nameValidator = nameValidator;
        this.ageValidator = ageValidator;
        this.emailValidator = emailValidator;
        this.phoneValidator = phoneValidator;
    }

    @Override
    public boolean validate(StudentData data) {
        errorMessage = "";
        
        if (data == null) {
            errorMessage = "Student data cannot be null";
            return false;
        }
        
        // Validate name
        if (!nameValidator.validate(data.name)) {
            errorMessage = "Name: " + nameValidator.getErrorMessage();
            return false;
        }
        
        // Validate age
        if (!ageValidator.validate(data.age)) {
            errorMessage = "Age: " + ageValidator.getErrorMessage();
            return false;
        }
        
        // Validate email
        if (!emailValidator.validate(data.email)) {
            errorMessage = "Email: " + emailValidator.getErrorMessage();
            return false;
        }
        
        // Validate phone
        if (!phoneValidator.validate(data.phone)) {
            errorMessage = "Phone: " + phoneValidator.getErrorMessage();
            return false;
        }
        
        return true;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Data class for student information
     */
    public static class StudentData {
        public String name;
        public Integer age;
        public String email;
        public String phone;

        public StudentData(String name, Integer age, String email, String phone) {
            this.name = name;
            this.age = age;
            this.email = email;
            this.phone = phone;
        }
    }
}
