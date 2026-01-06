package calculations;

/**
 * Single Responsibility Principle (SRP): Dedicated grade calculation logic
 * Open/Closed Principle (OCP): Can be extended with weighted grades, custom scales
 * Decoupled from display and storage logic
 */
public class GradeCalculator implements IGradeCalculator {
    
    @Override
    public double calculateOverallAverage(int studentId) {
        // This would need GradeManager instance injected (DIP)
        // Placeholder - actual implementation will use injected grade provider
        return -1;
    }

    @Override
    public double calculateCoreAverage(int studentId) {
        // Placeholder - actual implementation will filter by Core subjects
        return -1;
    }

    @Override
    public double calculateElectiveAverage(int studentId) {
        // Placeholder - actual implementation will filter by Elective subjects
        return -1;
    }

    /**
     * Standard US GPA Scale conversion
     * Can be overridden in subclasses for different scales
     * @param grade numeric grade (0-100)
     * @return GPA value (0.0-4.0)
     */
    @Override
    public double gradeToGPA(double grade) {
        if (grade >= 93) return 4.0;
        if (grade >= 90) return 3.9;
        if (grade >= 87) return 3.8;
        if (grade >= 83) return 3.7;
        if (grade >= 80) return 3.6;
        if (grade >= 77) return 3.5;
        if (grade >= 73) return 3.4;
        if (grade >= 70) return 3.0;
        if (grade >= 67) return 2.9;
        if (grade >= 63) return 2.8;
        if (grade >= 60) return 2.7;
        if (grade >= 57) return 2.5;
        if (grade >= 53) return 2.4;
        if (grade >= 50) return 2.0;
        return 0.0;
    }
}
