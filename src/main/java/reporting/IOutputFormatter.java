package reporting;

/**
 * Single Responsibility Principle (SRP): Dedicated console output formatting
 * Open/Closed Principle (OCP): Can be extended for other output formats
 * Separates presentation logic from business logic
 */
public interface IOutputFormatter {
    /**
     * Format and display report to console
     * @param report report content
     */
    void display(String report);

    /**
     * Format report with specific formatting style
     * @param report report content
     * @param style formatting style (colors, tables, etc.)
     */
    void displayWithStyle(String report, String style);

    /**
     * Get formatted separator line
     * @param width width of separator
     * @return separator string
     */
    String getSeparator(int width);

    /**
     * Get formatted header
     * @param title title text
     * @return formatted header
     */
    String getHeader(String title);
}
