package reporting;

/**
 * Open/Closed Principle (OCP): Interface for report generation
 * Dependency Inversion Principle (DIP): Depend on abstractions
 * Separates report generation from display logic (SRP)
 * Allows different report formats without modifying core logic
 */
public interface IReportGenerator {
    /**
     * Generate a grade report for a student
     * @param studentId student ID
     * @return report content as string
     */
    String generateStudentGradeReport(int studentId);

    /**
     * Generate class statistics report
     * @return report content as string
     */
    String generateClassStatisticsReport();

    /**
     * Generate GPA report for a student
     * @param studentId student ID
     * @return report content as string
     */
    String generateGPAReport(int studentId);

    /**
     * Export report in specified format
     * @param report report content
     * @param format format type (CSV, PDF, JSON, etc.)
     * @return formatted report content
     */
    String formatReport(String report, String format);
}
