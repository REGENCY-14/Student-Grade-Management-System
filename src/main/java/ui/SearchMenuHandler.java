package ui;

import context.ApplicationContext;
import core.Student;
import manager.GradeManager;
import search.RegexSearchEngine;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * SearchMenuHandler - Handles all search and pattern-matching operations
 * Responsibility: Search functionality only
 * Single Responsibility: Searching students using various criteria
 */
public class SearchMenuHandler {
    private final ApplicationContext context;
    private final Scanner scanner;
    private final ArrayList<Student> students;
    private final GradeManager gradeManager;

    public SearchMenuHandler(ApplicationContext context, Scanner scanner, 
                            ArrayList<Student> students, GradeManager gradeManager) {
        this.context = context;
        this.scanner = scanner;
        this.students = students;
        this.gradeManager = gradeManager;
    }

    public void searchStudents() {
        System.out.println("============================================");
        System.out.println("||         SEARCH STUDENTS               ||");
        System.out.println("============================================");
        System.out.println("\n--- BASIC SEARCH ---");
        System.out.println("1. Student ID");
        System.out.println("2. Name (Partial Match)");
        System.out.println("3. Grade Range");
        System.out.println("4. Student Type (Regular/Honors)");
        
        System.out.println("\n--- REGEX SEARCH ---");
        System.out.println("5. Search by Email Domain Pattern");
        System.out.println("6. Search by Student ID Pattern (Wildcard)");
        System.out.println("7. Search by Name Pattern (Regex)");
        System.out.println("8. Custom Regex Pattern Search");
        
        System.out.println("\n9. Back to Main Menu");
        System.out.print("Enter choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        ArrayList<Student> results = new ArrayList<>();
        RegexSearchEngine regexEngine = new RegexSearchEngine();

        try {
            switch (choice) {
                case 1:
                    results = searchByStudentId();
                    displaySearchResults(results);
                    break;
                case 2:
                    results = searchByName();
                    displaySearchResults(results);
                    break;
                case 3:
                    results = searchByGradeRange();
                    displaySearchResults(results);
                    break;
                case 4:
                    results = searchByStudentType();
                    displaySearchResults(results);
                    break;
                case 5:
                    regexSearchByEmailDomain(regexEngine);
                    break;
                case 6:
                    regexSearchByIdPattern(regexEngine);
                    break;
                case 7:
                    regexSearchByNamePattern(regexEngine);
                    break;
                case 8:
                    regexCustomPatternSearch(regexEngine);
                    break;
                case 9:
                    return;
                default:
                    System.out.println("Invalid choice!");
                    return;
            }
        } catch (Exception e) {
            System.out.println("Error during search: " + e.getMessage());
        }
    }

    public void advancedPatternSearch() {
        RegexSearchEngine searchEngine = new RegexSearchEngine();
        boolean searching = true;

        while (searching) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("        ADVANCED PATTERN BASED SEARCH");
            System.out.println("=".repeat(60));
            System.out.println("1. Search by Email Domain Pattern");
            System.out.println("2. Search by Phone Area Code Pattern");
            System.out.println("3. Search by Student ID Pattern (with wildcards)");
            System.out.println("4. Search by Name Pattern (Regex)");
            System.out.println("5. Custom Regex Pattern Search");
            System.out.println("6. Back to Main Menu");
            System.out.print("\nSelect option: ");

            try {
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        searchByEmailDomain(searchEngine);
                        break;
                    case 2:
                        searchByPhoneAreaCode(searchEngine);
                        break;
                    case 3:
                        searchByStudentIdPattern(searchEngine);
                        break;
                    case 4:
                        searchByNamePattern(searchEngine);
                        break;
                    case 5:
                        customPatternSearch(searchEngine);
                        break;
                    case 6:
                        searching = false;
                        System.out.println("Returning to main menu...");
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                scanner.nextLine();
            }
        }
    }

    private void regexSearchByEmailDomain(RegexSearchEngine engine) {
        System.out.println("\n========== REGEX SEARCH: EMAIL DOMAIN ==========");
        System.out.println("Examples: gmail\\.com, .*@.*\\.edu, yahoo.*");
        System.out.print("Enter email domain pattern: ");
        String pattern = scanner.nextLine().trim();

        System.out.print("Case insensitive? (y/n): ");
        boolean caseInsensitive = scanner.nextLine().trim().equalsIgnoreCase("y");

        engine.searchByEmailDomain(pattern, caseInsensitive);
        engine.displayResults();

        if (engine.hasResults()) {
            handleBulkOperations(engine);
        }
    }

    private void regexSearchByIdPattern(RegexSearchEngine engine) {
        System.out.println("\n========== REGEX SEARCH: STUDENT ID PATTERN ==========");
        System.out.println("Examples: 10.*, 1[0-2].*, ^101[0-5]$");
        System.out.print("Enter student ID pattern: ");
        String pattern = scanner.nextLine().trim();

        System.out.print("Case insensitive? (y/n): ");
        boolean caseInsensitive = scanner.nextLine().trim().equalsIgnoreCase("y");

        engine.searchByIdPattern(pattern, caseInsensitive);
        engine.displayResults();

        if (engine.hasResults()) {
            handleBulkOperations(engine);
        }
    }

    private void regexSearchByNamePattern(RegexSearchEngine engine) {
        System.out.println("\n========== REGEX SEARCH: NAME PATTERN ==========");
        System.out.println("Examples: ^A.*, .*Smith$, ^[A-M].*");
        System.out.print("Enter name pattern: ");
        String pattern = scanner.nextLine().trim();

        System.out.print("Case insensitive? (y/n): ");
        boolean caseInsensitive = scanner.nextLine().trim().equalsIgnoreCase("y");

        engine.searchByNamePattern(pattern, caseInsensitive);
        engine.displayResults();

        if (engine.hasResults()) {
            handleBulkOperations(engine);
        }
    }

    private void regexCustomPatternSearch(RegexSearchEngine engine) {
        System.out.println("\n========== CUSTOM REGEX PATTERN SEARCH ==========");
        System.out.println("Available fields: name, email, phone, type, status, id");
        System.out.print("Enter field name: ");
        String fieldName = scanner.nextLine().trim();

        System.out.print("Enter regex pattern: ");
        String pattern = scanner.nextLine().trim();

        System.out.print("Case insensitive? (y/n): ");
        boolean caseInsensitive = scanner.nextLine().trim().equalsIgnoreCase("y");

        engine.searchByCustomPattern(pattern, fieldName, caseInsensitive);
        engine.displayResults();

        if (engine.hasResults()) {
            handleBulkOperations(engine);
        }
    }

    private void handleBulkOperations(RegexSearchEngine engine) {
        boolean bulkOperating = true;
        while (bulkOperating) {
            engine.displayBulkOperationsMenu();
            int bulkChoice = scanner.nextInt();
            scanner.nextLine();

            engine.performBulkOperation(bulkChoice);

            if (bulkChoice == 4) {
                bulkOperating = false;
            }
        }
    }

    private ArrayList<Student> searchByStudentId() {
        ArrayList<Student> results = new ArrayList<>();

        System.out.print("Enter Student ID: ");
        int searchId = scanner.nextInt();
        scanner.nextLine();

        for (Student s : students) {
            if (s.id == searchId) {
                results.add(s);
                break;
            }
        }

        return results;
    }

    private ArrayList<Student> searchByName() {
        ArrayList<Student> results = new ArrayList<>();

        System.out.print("Enter student name (or part of name): ");
        String searchName = scanner.nextLine().toLowerCase().trim();

        if (searchName.isEmpty()) {
            System.out.println("Search name cannot be empty!");
            return results;
        }

        for (Student s : students) {
            if (s.name.toLowerCase().contains(searchName)) {
                results.add(s);
            }
        }

        return results;
    }

    private ArrayList<Student> searchByGradeRange() {
        ArrayList<Student> results = new ArrayList<>();

        System.out.print("Enter minimum grade (0-100): ");
        double minGrade = scanner.nextDouble();

        System.out.print("Enter maximum grade (0-100): ");
        double maxGrade = scanner.nextDouble();
        scanner.nextLine();

        if (minGrade < 0 || minGrade > 100 || maxGrade < 0 || maxGrade > 100) {
            System.out.println("Invalid grade range! Grades must be between 0 and 100.");
            return results;
        }

        if (minGrade > maxGrade) {
            System.out.println("Minimum grade cannot be greater than maximum grade!");
            return results;
        }

        for (Student s : students) {
            double avgGrade = s.getAverageGrade();
            if (avgGrade >= minGrade && avgGrade <= maxGrade && avgGrade > 0) {
                results.add(s);
            }
        }

        return results;
    }

    private ArrayList<Student> searchByStudentType() {
        ArrayList<Student> results = new ArrayList<>();

        System.out.println("Select Student Type:");
        System.out.println("1. Regular Students");
        System.out.println("2. Honors Students");
        System.out.print("Enter choice: ");

        int typeChoice = scanner.nextInt();
        scanner.nextLine();

        String targetType = "";
        if (typeChoice == 1) {
            targetType = "Regular";
        } else if (typeChoice == 2) {
            targetType = "Honors";
        } else {
            System.out.println("Invalid student type!");
            return results;
        }

        for (Student s : students) {
            if (s.getType().equals(targetType)) {
                results.add(s);
            }
        }

        return results;
    }

    private void displaySearchResults(ArrayList<Student> results) {
        if (results.isEmpty()) {
            System.out.println("\n❌ No students found matching your search criteria.\n");
            return;
        }

        System.out.println("\n============================================");
        System.out.println("||      SEARCH RESULTS                   ||");
        System.out.println("============================================");
        System.out.println("Found " + results.size() + " student(s)\n");

        System.out.printf("%-6s %-20s %-10s %-8s %-12s %-12s %-10s %-12s\n",
                "ID", "NAME", "AGE", "TYPE", "AVG GRADE", "SUBJECTS", "STATUS", "PASSING GRADE");
        System.out.println("------------------------------------------------------------------------------------------------");

        for (Student s : results) {
            double avg = s.getAverageGrade();
            int subjectCount = gradeManager.getSubjectCountForStudent(s.id);

            System.out.printf("%-6d %-20s %-10d %-8s %-12.2f %-12d %-10s %-12d\n",
                    s.id,
                    s.name,
                    s.age,
                    s.getType(),
                    avg > 0 ? avg : 0.0,
                    subjectCount,
                    s.getStatus(),
                    s.getPassingGrade()
            );
        }

        System.out.println("------------------------------------------------------------------------------------------------");
        System.out.println();
    }

    private void searchByEmailDomain(RegexSearchEngine searchEngine) {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("Search by Email Domain Pattern");
        System.out.println("-".repeat(60));
        System.out.print("Enter domain pattern (e.g., gmail\\.com, yahoo\\..*): ");
        String pattern = scanner.nextLine();

        System.out.println("\nSearching...");
        ArrayList<RegexSearchEngine.SearchResult> results = searchEngine.searchByEmailDomain(pattern);
        displaySearchResults(results, searchEngine, "Email Domain");
    }

    private void searchByPhoneAreaCode(RegexSearchEngine searchEngine) {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("Search by Phone Area Code Pattern");
        System.out.println("-".repeat(60));
        System.out.print("Enter area code pattern (e.g., 212, 555, [2-5]1[0-5]): ");
        String pattern = scanner.nextLine();

        System.out.println("\nSearching...");
        ArrayList<RegexSearchEngine.SearchResult> results = searchEngine.searchByPhoneAreaCode(pattern);
        displaySearchResults(results, searchEngine, "Phone Area Code");
    }

    private void searchByStudentIdPattern(RegexSearchEngine searchEngine) {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("Search by Student ID Pattern (with wildcards)");
        System.out.println("-".repeat(60));
        System.out.println("Examples:");
        System.out.println("  10.*    - IDs starting with 10");
        System.out.println("  1[0-2].* - IDs starting with 10, 11, or 12");
        System.out.println("  .*5$    - IDs ending with 5");
        System.out.print("\nEnter ID pattern: ");
        String pattern = scanner.nextLine();

        System.out.println("\nSearching...");
        ArrayList<RegexSearchEngine.SearchResult> results = searchEngine.searchByIdPattern(pattern);
        displaySearchResults(results, searchEngine, "Student ID");
    }

    private void searchByNamePattern(RegexSearchEngine searchEngine) {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("Search by Name Pattern (Regex)");
        System.out.println("-".repeat(60));
        System.out.println("Examples:");
        System.out.println("  ^A.*     - Names starting with A");
        System.out.println("  .*Smith$ - Names ending with Smith");
        System.out.println("  ^J.*n$   - Names starting with J and ending with n");
        System.out.print("\nEnter name pattern: ");
        String pattern = scanner.nextLine();

        System.out.println("\nSearching...");
        ArrayList<RegexSearchEngine.SearchResult> results = searchEngine.searchByNamePattern(pattern);
        displaySearchResults(results, searchEngine, "Student Name");
    }

    private void customPatternSearch(RegexSearchEngine searchEngine) {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("Custom Regex Pattern Search");
        System.out.println("-".repeat(60));
        System.out.println("Select field to search:");
        System.out.println("1. Student ID");
        System.out.println("2. Name");
        System.out.println("3. Email");
        System.out.println("4. Phone");
        System.out.print("Select field: ");

        try {
            int fieldChoice = scanner.nextInt();
            scanner.nextLine();

            String field;
            switch (fieldChoice) {
                case 1:
                    field = "id";
                    break;
                case 2:
                    field = "name";
                    break;
                case 3:
                    field = "email";
                    break;
                case 4:
                    field = "phone";
                    break;
                default:
                    System.out.println("Invalid field selection.");
                    return;
            }

            System.out.print("Enter regex pattern: ");
            String pattern = scanner.nextLine();

            System.out.println("\nSearching...");
            ArrayList<RegexSearchEngine.SearchResult> results = searchEngine.searchByCustomPattern(field, pattern);
            displaySearchResults(results, searchEngine, field);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            scanner.nextLine();
        }
    }

    private void displaySearchResults(ArrayList<RegexSearchEngine.SearchResult> results, 
                                              RegexSearchEngine searchEngine, String searchType) {
        if (results.isEmpty()) {
            System.out.println("No matches found.");
            return;
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("SEARCH RESULTS - " + results.size() + " match(es) found");
        System.out.println("=".repeat(60));

        searchEngine.displayResults(results);

        System.out.println("\n" + "-".repeat(60));
        searchEngine.displaySearchStatsMenu();
        System.out.println("-".repeat(60));

        System.out.println("\nBulk Operations:");
        System.out.println("1. View Full Details");
        System.out.println("2. Export to CSV");
        System.out.println("3. View Statistics");
        System.out.println("4. Back to Pattern Search");
        System.out.print("Select option: ");

        try {
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    for (RegexSearchEngine.SearchResult result : results) {
                        System.out.println("\n" + "-".repeat(40));
                        System.out.println("Student: " + result.student.getName());
                        System.out.println("ID: " + result.student.getId());
                        System.out.println("Email: " + result.student.getEmail());
                        System.out.println("Phone: " + result.student.getPhone());
                        System.out.println("GPA: " + String.format("%.2f", result.student.computeGPA()));
                        System.out.println("Status: " + result.student.getStatus());
                        System.out.println("Matched Field: " + result.matchedField);
                        System.out.println("Match Value: " + result.highlightedMatch);
                    }
                    break;
                case 2:
                    searchEngine.performBulkOperation(results, "csv");
                    break;
                case 3:
                    searchEngine.performBulkOperation(results, "stats");
                    break;
                case 4:
                    return;
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            scanner.nextLine();
        }
    }
}
