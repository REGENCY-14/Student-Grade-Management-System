package examples;

import validators.*;
import generators.*;

/**
 * Example demonstrating SOLID principles in action
 * Shows how to use the refactored components
 */
public class SOLIDExample {

    /**
     * Example 1: Using segregated validators (SRP + ISP)
     */
    public static void example1_ValidatorUsage() {
        System.out.println("=== Example 1: Segregated Validators (SRP + ISP) ===\n");

        // Before: Mixed validation logic scattered throughout code
        // String email = "user@example.com";
        // if (!email.contains("@") && !email.contains(".")) { ... }

        // After: Dedicated, single-purpose validators
        String email = "john.doe@example.com";
        EmailValidator emailValidator = new EmailValidator();

        if (emailValidator.validate(email)) {
            System.out.println("✓ Email valid: " + email);
        } else {
            System.out.println("✗ Email invalid: " + emailValidator.getErrorMessage());
        }

        // Each validator has ONE responsibility
        int age = 25;
        AgeValidator ageValidator = new AgeValidator(18, 100); // Configurable bounds
        if (ageValidator.validate(age)) {
            System.out.println("✓ Age valid: " + age);
        } else {
            System.out.println("✗ Age invalid: " + ageValidator.getErrorMessage());
        }

        String phone = "1234567890";
        PhoneValidator phoneValidator = new PhoneValidator();
        if (phoneValidator.validate(phone)) {
            System.out.println("✓ Phone valid: " + phone);
        } else {
            System.out.println("✗ Phone invalid: " + phoneValidator.getErrorMessage());
        }

        System.out.println();
    }

    /**
     * Example 2: Using composite validator (SRP composition)
     */
    public static void example2_CompositeValidator() {
        System.out.println("=== Example 2: Composite Validator (SRP Composition) ===\n");

        StudentDataValidator validator = new StudentDataValidator();

        StudentDataValidator.StudentData validStudent = new StudentDataValidator.StudentData(
            "John Doe",
            25,
            "john@example.com",
            "9876543210"
        );

        if (validator.validate(validStudent)) {
            System.out.println("✓ All student data valid");
        }

        // Invalid student
        StudentDataValidator.StudentData invalidStudent = new StudentDataValidator.StudentData(
            "J",  // Too short
            150, // Invalid age
            "invalid-email", // Missing @
            "123" // Too short
        );

        if (!validator.validate(invalidStudent)) {
            System.out.println("✗ Student data invalid: " + validator.getErrorMessage());
        }

        System.out.println();
    }

    /**
     * Example 3: Using ID generator (SRP + abstraction)
     */
    public static void example3_IDGenerator() {
        System.out.println("=== Example 3: ID Generator (SRP + DIP) ===\n");

        // Before: ID generation scattered, mixed with business logic
        // static int studentIdCounter = 1000;
        // int id = studentIdCounter++;

        // After: Dedicated ID generator, can be swapped
        IIdGenerator studentIdGen = new SequentialIdGenerator(1000);
        
        // Generate multiple IDs - thread-safe
        int id1 = studentIdGen.generateId();
        int id2 = studentIdGen.generateId();
        int id3 = studentIdGen.generateId();

        System.out.println("Generated Student IDs: " + id1 + ", " + id2 + ", " + id3);
        System.out.println("Current counter value: " + studentIdGen.getCurrentValue());

        // Future: Can swap with UUID generator without changing code
        // IIdGenerator uuidGen = new UUIDIdGenerator();

        System.out.println();
    }

    /**
     * Example 4: Demonstrating Open/Closed Principle
     * Extensions without modification
     */
    public static void example4_OpenClosedPrinciple() {
        System.out.println("=== Example 4: Open/Closed Principle (Extension without Modification) ===\n");

        System.out.println("Current implementation: IGradeCalculator interface created");
        System.out.println("Can now create without modifying existing code:");
        System.out.println("  - WeightedGradeCalculator (weights for major/minor courses)");
        System.out.println("  - InternationalGradeCalculator (different scaling systems)");
        System.out.println("  - CurvedGradeCalculator (bell curve adjustments)");
        System.out.println("\nSame for reports:");
        System.out.println("  - PDFReportGenerator");
        System.out.println("  - JSONReportGenerator");
        System.out.println("  - ExcelReportGenerator");
        System.out.println("\nExisting GradeManager remains CLOSED for modification");

        System.out.println();
    }

    /**
     * Example 5: Demonstrating Dependency Inversion
     */
    public static void example5_DependencyInversion() {
        System.out.println("=== Example 5: Dependency Inversion (DIP) ===\n");

        // Before: Direct coupling to concrete classes
        // Student directly references Menu.gradeManager (static)
        // GradeManager hardcoded in Student class

        // After: Depend on abstractions through ApplicationContext
        // ApplicationContext context = ApplicationContext.getInstance();

        // Future: Can inject different implementations
        // context.setGradeManager(new DatabaseGradeManager());
        // OR
        // context.setGradeManager(new MockGradeManager()); // for testing

        System.out.println("ApplicationContext provides abstraction layer");
        System.out.println("Can swap implementations without changing dependent code");
        System.out.println("\nFuture: Replace static coupling with true dependency injection");

        System.out.println();
    }

    /**
     * Example 6: Demonstrating Interface Segregation
     */
    public static void example6_InterfaceSegregation() {
        System.out.println("=== Example 6: Interface Segregation (ISP) ===\n");

        System.out.println("Before: Student implemented 3 large interfaces");
        System.out.println("  - IStudentInfo: 5 methods");
        System.out.println("  - IStudentAcademic: 5 methods");
        System.out.println("  - IStudentEligibility: 2 methods");
        System.out.println("Total: 12 methods, all clients saw all methods");

        System.out.println("\nAfter: Segregated into focused interfaces:");
        System.out.println("  - IStudentIdentity: Only ID, name, age, email, phone");
        System.out.println("    Used by: Registration, Communication");
        System.out.println("  - IStudentType: Only type, passing grade, status");
        System.out.println("    Used by: Classification, Eligibility checking");
        System.out.println("  - IStudentAcademicPerformance: Only grades, GPA, subjects");
        System.out.println("    Used by: Grade calculation, Reports");

        System.out.println("\nBenefits:");
        System.out.println("  ✓ Communication module doesn't see grade methods");
        System.out.println("  ✓ Easier to test - mock only needed interface");
        System.out.println("  ✓ Clear dependencies between components");

        System.out.println();
    }

    /**
     * Example 7: Real-world usage pattern
     */
    public static void example7_RealWorldPattern() {
        System.out.println("=== Example 7: Real-World Usage Pattern ===\n");

        System.out.println("New Student Registration Flow:");
        System.out.println();

        // 1. Get user input
        String name = "Alice Smith";
        int age = 22;
        String email = "alice@university.edu";
        String phone = "5551234567";

        // 2. Validate using segregated validators
        StudentDataValidator validator = new StudentDataValidator();
        StudentDataValidator.StudentData data = new StudentDataValidator.StudentData(name, age, email, phone);

        if (!validator.validate(data)) {
            System.out.println("Validation failed: " + validator.getErrorMessage());
            return;
        }

        // 3. Generate unique ID
        IIdGenerator idGen = new SequentialIdGenerator(1000);
        int studentId = idGen.generateId();

        System.out.println("Student registered successfully!");
        System.out.println("  ID: " + studentId);
        System.out.println("  Name: " + name);
        System.out.println("  Email: " + email);
        System.out.println("  Phone: " + phone);
        System.out.println("\nData is valid, centrally validated, ID generated cleanly");
        System.out.println("No mixing of concerns, each class has single responsibility");

        System.out.println();
    }

    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║   SOLID PRINCIPLES IN PRACTICE - Examples              ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");

        example1_ValidatorUsage();
        example2_CompositeValidator();
        example3_IDGenerator();
        example4_OpenClosedPrinciple();
        example5_DependencyInversion();
        example6_InterfaceSegregation();
        example7_RealWorldPattern();

        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("All examples demonstrate SOLID principles in action!");
        System.out.println("═══════════════════════════════════════════════════════════\n");
    }
}
