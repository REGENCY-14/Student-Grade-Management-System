package interfaces;

/**
 * Interface Segregation Principle (ISP): Segregated interface for student type information
 * Clients needing to determine student type depend only on this interface
 */
public interface IStudentType {
    /**
     * Get student type (Regular, Honors, etc.)
     * @return student type string
     */
    String getType();

    /**
     * Get minimum passing grade for this student type
     * @return passing grade threshold
     */
    int getPassingGrade();

    /**
     * Get current student status (Passing, Failing, etc.)
     * @return status string
     */
    String getStatus();

    /**
     * Check if student is eligible for honors
     * @return true if eligible, false otherwise
     */
    boolean isEligibleForHonors();
}
