import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class temp_methods {
    private static final Scanner scanner = new Scanner(System.in);
    private static final GradeManager gradeManager = new GradeManager();

    // ==================== MULTI-FORMAT IMPORT/EXPORT ====================

    /**
     * Advanced multi-format import with file watching
     */
    public static void advancedImportGrades() {
        FileFormatManager formatManager = new FileFormatManager();
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("ADVANCED MULTI-FORMAT IMPORT");
        System.out.println("=".repeat(70));
        System.out.println("1. Import from CSV");
        System.out.println("2. Import from JSON");
        System.out.println("3. Import from Binary");
        System.out.println("4. Watch Directory for New Imports");
        System.out.println("5. List Files by Format");
        System.out.println("6. Back to Main Menu");
        System.out.print("Enter choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        try {
            switch (choice) {
                case 1 -> importMultiFormat(formatManager, "CSV");
                case 2 -> importMultiFormat(formatManager, "JSON");
                case 3 -> importMultiFormat(formatManager, "BINARY");
                case 4 -> watchDirectoryForNewFiles(formatManager);
                case 5 -> listFilesByFormat(formatManager);
                case 6 -> {}
                default -> System.out.println("Invalid choice.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Advanced multi-format export with comparison
     */
    public static void advancedExportGrades() {
        if (gradeManager.getGradeCount() == 0) {
            System.out.println("No grades to export.");
            return;
        }

        FileFormatManager formatManager = new FileFormatManager();

        System.out.println("\n" + "=".repeat(70));
        System.out.println("ADVANCED MULTI-FORMAT EXPORT");
        System.out.println("=".repeat(70));
        System.out.println("1. Export to CSV");
        System.out.println("2. Export to JSON");
        System.out.println("3. Export to Binary");
        System.out.println("4. Export to All Formats (with comparison)");
        System.out.println("5. Back to Main Menu");
        System.out.print("Enter choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter file name (without extension): ");
        String fileName = scanner.nextLine().trim();

        List<FileFormatManager.FileStats> statsList = new ArrayList<>();

        try {
            ArrayList<Grade> grades = new ArrayList<>();
            for (int i = 0; i < gradeManager.getGradeCount(); i++) {
                grades.add(gradeManager.grades[i]);
            }

            switch (choice) {
                case 1 -> {
                    FileFormatManager.FileStats csvStats = formatManager.exportToCSV(grades, fileName);
                    System.out.println("\n✓ " + csvStats);
                }
                case 2 -> {
                    FileFormatManager.FileStats jsonStats = formatManager.exportToJSON(grades, fileName);
                    System.out.println("\n✓ " + jsonStats);
                }
                case 3 -> {
                    FileFormatManager.FileStats binStats = formatManager.exportToBinary(grades, fileName);
                    System.out.println("\n✓ " + binStats);
                }
                case 4 -> {
                    statsList.add(formatManager.exportToCSV(grades, fileName + "_csv"));
                    statsList.add(formatManager.exportToJSON(grades, fileName + "_json"));
                    statsList.add(formatManager.exportToBinary(grades, fileName + "_bin"));
                    formatManager.displayFormatComparison(statsList);
                }
                case 5 -> {}
                default -> System.out.println("Invalid choice.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void importMultiFormat(FileFormatManager formatManager, String format) {
        System.out.print("\nEnter file name (without extension): ");
        String fileName = scanner.nextLine().trim();

        FileFormatManager.FileStats stats = null;

        try {
            switch (format.toUpperCase()) {
                case "CSV" -> stats = formatManager.importFromCSV(fileName);
                case "JSON" -> stats = formatManager.importFromJSON(fileName);
                case "BINARY" -> stats = formatManager.importFromBinary(fileName);
                default -> {}
            }

            if (stats != null && stats.recordsProcessed > 0) {
                System.out.println("\n" + "=".repeat(120));
                System.out.println("IMPORT STATISTICS");
                System.out.println("=".repeat(120));
                System.out.println(stats);
                System.out.println("=".repeat(120) + "\n");
            }

        } catch (Exception e) {
            System.out.println("❌ Import Error: " + e.getMessage());
        }
    }

    private static void watchDirectoryForNewFiles(FileFormatManager formatManager) {
        System.out.println("Select format to watch:");
        System.out.println("1. CSV");
        System.out.println("2. JSON");
        System.out.println("3. Binary");
        System.out.print("Enter choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        String format = switch (choice) {
            case 1 -> "csv";
            case 2 -> "json";
            case 3 -> "binary";
            default -> null;
        };

        if (format == null) {
            System.out.println("Invalid choice.");
            return;
        }

        System.out.print("Enter watch duration (seconds): ");
        long seconds = scanner.nextLong();
        scanner.nextLine();

        formatManager.watchDirectoryForImports(format, seconds);
    }

    private static void listFilesByFormat(FileFormatManager formatManager) {
        System.out.println("Select format to list:");
        System.out.println("1. CSV");
        System.out.println("2. JSON");
        System.out.println("3. Binary");
        System.out.print("Enter choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        String format = switch (choice) {
            case 1 -> "csv";
            case 2 -> "json";
            case 3 -> "binary";
            default -> null;
        };

        if (format == null) {
            System.out.println("Invalid choice.");
            return;
        }

        formatManager.listFilesByFormat(format);
    }

}
