package interfaces;

/**
 * Interface Segregation Principle (ISP): Segregated interface for student academic performance
 * Clients needing academic information depend only on this interface
 */
public interface IStudentAcademicPerformance {
    /**
     * Get average grade for the student
     * @return average grade
     */
    double getAverageGrade();

    /**
     * Set average grade for the student
     * @param grade average grade value
     */
    void setAverageGrade(double grade);

    /**
     * Get number of enrolled subjects
     * @return subject count
     */
    int getEnrolledSubjects();

    /**
     * Compute GPA from current grades
     * @return computed GPA value
     */
    double computeGPA();

    /**
     * Update average GPA from current grades
     */
    void updateAverageGPA();
}
