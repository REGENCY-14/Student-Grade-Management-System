package reporting;

/**
 * Single Responsibility Principle (SRP): Dedicated console output formatter
 * Implements IOutputFormatter for console display
 * Can be extended or replaced with other formatters (PDF, HTML, etc.)
 */
public class ConsoleOutputFormatter implements IOutputFormatter {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_BLUE = "\u001B[34m";

    @Override
    public void display(String report) {
        System.out.println(report);
    }

    @Override
    public void displayWithStyle(String report, String style) {
        if ("colored".equalsIgnoreCase(style)) {
            System.out.println(ANSI_CYAN + report + ANSI_RESET);
        } else if ("highlighted".equalsIgnoreCase(style)) {
            System.out.println(ANSI_YELLOW + report + ANSI_RESET);
        } else if ("success".equalsIgnoreCase(style)) {
            System.out.println(ANSI_GREEN + report + ANSI_RESET);
        } else if ("info".equalsIgnoreCase(style)) {
            System.out.println(ANSI_BLUE + report + ANSI_RESET);
        } else {
            display(report);
        }
    }

    @Override
    public String getSeparator(int width) {
        return "-".repeat(Math.max(0, width));
    }

    @Override
    public String getHeader(String title) {
        int width = title.length() + 4;
        String separator = getSeparator(width);
        return String.format("%s\n| %s |\n%s", separator, title, separator);
    }
}
