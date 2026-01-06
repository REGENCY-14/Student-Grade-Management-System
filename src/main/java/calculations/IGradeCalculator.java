package calculations;

/**
 * Open/Closed Principle (OCP): Interface for grade average calculations
 * Allows different calculation strategies without modifying existing code
 * Dependency Inversion Principle (DIP): Depend on abstractions, not concrete implementations
 */
public interface IGradeCalculator {
    /**
     * Calculate overall average for a student
     * @param studentId student ID
     * @return average grade or -1 if not found
     */
    double calculateOverallAverage(int studentId);

    /**
     * Calculate core subject average for a student
     * @param studentId student ID
     * @return average grade or -1 if not found
     */
    double calculateCoreAverage(int studentId);

    /**
     * Calculate elective subject average for a student
     * @param studentId student ID
     * @return average grade or -1 if not found
     */
    double calculateElectiveAverage(int studentId);

    /**
     * Calculate GPA from a numeric grade
     * @param grade numeric grade value
     * @return GPA value (0.0 - 4.0)
     */
    double gradeToGPA(double grade);
}
