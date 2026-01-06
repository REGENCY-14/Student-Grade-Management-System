import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class RegexSearchEngine {

    public static class SearchResult {
        Student student;
        String matchedField;
        String matchedValue;
        String highlightedMatch;
        
        SearchResult(Student student, String field, String value, String highlighted) {
            this.student = student;
            this.matchedField = field;
            this.matchedValue = value;
            this.highlightedMatch = highlighted;
        }
    }

    public static class SearchStats {
        int totalScanned;
        int matchesFound;
        long searchTimeMs;
        int totalStudents;
        Map<String, Integer> distributionByType;
        Map<String, Integer> distributionByStatus;
        
        SearchStats() {
            this.distributionByType = new HashMap<>();
            this.distributionByStatus = new HashMap<>();
        }
    }

    private SearchStats stats;
    private final List<SearchResult> currentResults;
    private String lastPattern;
    private boolean caseInsensitive;

    public RegexSearchEngine() {
        this.currentResults = new ArrayList<>();
        this.caseInsensitive = false;
    }

    /**
     * Search by email domain pattern (e.g., gmail\.com, .*@.*\.edu)
     */
    public List<Student> searchByEmailDomain(String domainPattern, boolean caseInsensitive) {
        long startTime = System.currentTimeMillis();
        this.caseInsensitive = caseInsensitive;
        this.lastPattern = domainPattern;
        
        currentResults.clear();
        stats = new SearchStats();
        stats.totalStudents = ApplicationContext.getInstance().getStudents().size();
        
        try {
            int flags = caseInsensitive ? Pattern.CASE_INSENSITIVE : 0;
            Pattern pattern = Pattern.compile(domainPattern, flags);
            
            for (Student s : ApplicationContext.getInstance().getStudents()) {
                stats.totalScanned++;
                Student student = s;
                try {
                    Object c = CacheManager.getInstance().get("student:" + s.getId());
                    if (c instanceof Student) student = (Student) c;
                } catch (Exception ex) { }
                String emailDomain = extractDomain(student.getEmail());
                Matcher matcher = pattern.matcher(emailDomain);

                if (matcher.find()) {
                    String highlighted = highlightMatch(emailDomain, matcher);
                    SearchResult result = new SearchResult(student, "Email Domain", emailDomain, highlighted);
                    currentResults.add(result);
                    stats.matchesFound++;
                    addDistributionStats(student);
                }
            }
            
        } catch (PatternSyntaxException e) {
            throwPatternError(e);
        }
        
        stats.searchTimeMs = System.currentTimeMillis() - startTime;
        try { AuditLogger.getInstance().log("SEARCH_EMAIL_DOMAIN", "pattern=" + domainPattern, stats.searchTimeMs, true, "matches=" + stats.matchesFound); } catch (Exception ex) { }
        return currentResults.stream().map(r -> r.student).collect(Collectors.toList());
    }

    /**
     * Search by student ID pattern with wildcard support (e.g., 10.*, 1[0-2].*)
     */
    public List<Student> searchByIdPattern(String idPattern, boolean caseInsensitive) {
        long startTime = System.currentTimeMillis();
        this.caseInsensitive = caseInsensitive;
        this.lastPattern = idPattern;
        
        currentResults.clear();
        stats = new SearchStats();
        stats.totalStudents = ApplicationContext.getInstance().getStudents().size();
        
        try {
            int flags = caseInsensitive ? Pattern.CASE_INSENSITIVE : 0;
            Pattern pattern = Pattern.compile(idPattern, flags);
            
            for (Student s : ApplicationContext.getInstance().getStudents()) {
                stats.totalScanned++;
                Student student = s;
                try {
                    Object c = CacheManager.getInstance().get("student:" + s.getId());
                    if (c instanceof Student) student = (Student) c;
                } catch (Exception ex) { }
                String idStr = String.valueOf(student.getId());
                Matcher matcher = pattern.matcher(idStr);

                if (matcher.find()) {
                    String highlighted = highlightMatch(idStr, matcher);
                    SearchResult result = new SearchResult(student, "Student ID", idStr, highlighted);
                    currentResults.add(result);
                    stats.matchesFound++;
                    addDistributionStats(student);
                }
            }
            
        } catch (PatternSyntaxException e) {
            throwPatternError(e);
        }
        
        stats.searchTimeMs = System.currentTimeMillis() - startTime;
        try { AuditLogger.getInstance().log("SEARCH_ID_PATTERN", "pattern=" + idPattern, stats.searchTimeMs, true, "matches=" + stats.matchesFound); } catch (Exception ex) { }
        return currentResults.stream().map(r -> r.student).collect(Collectors.toList());
    }

    /**
     * Search by name pattern (e.g., ^[A-Z].*Smith$, .*john.*)
     */
    public List<Student> searchByNamePattern(String namePattern, boolean caseInsensitive) {
        long startTime = System.currentTimeMillis();
        this.caseInsensitive = caseInsensitive;
        this.lastPattern = namePattern;
        
        currentResults.clear();
        stats = new SearchStats();
        stats.totalStudents = ApplicationContext.getInstance().getStudents().size();
        
        try {
            int flags = caseInsensitive ? Pattern.CASE_INSENSITIVE : 0;
            Pattern pattern = Pattern.compile(namePattern, flags);
            
            for (Student s : ApplicationContext.getInstance().getStudents()) {
                stats.totalScanned++;
                Student student = s;
                try {
                    Object c = CacheManager.getInstance().get("student:" + s.getId());
                    if (c instanceof Student) student = (Student) c;
                } catch (Exception ex) { }
                Matcher matcher = pattern.matcher(student.getName());

                if (matcher.find()) {
                    String highlighted = highlightMatch(student.getName(), matcher);
                    SearchResult result = new SearchResult(student, "Name", student.getName(), highlighted);
                    currentResults.add(result);
                    stats.matchesFound++;
                    addDistributionStats(student);
                }
            }
            
        } catch (PatternSyntaxException e) {
            throwPatternError(e);
        }
        
        stats.searchTimeMs = System.currentTimeMillis() - startTime;
        try { AuditLogger.getInstance().log("SEARCH_NAME_PATTERN", "pattern=" + namePattern, stats.searchTimeMs, true, "matches=" + stats.matchesFound); } catch (Exception ex) { }
        return currentResults.stream().map(r -> r.student).collect(Collectors.toList());
    }

    /**
     * Custom regex pattern search on any field
     */
    public List<Student> searchByCustomPattern(String pattern, String fieldName, boolean caseInsensitive) {
        long startTime = System.currentTimeMillis();
        this.caseInsensitive = caseInsensitive;
        this.lastPattern = pattern;
        
        currentResults.clear();
        stats = new SearchStats();
        stats.totalStudents = ApplicationContext.getInstance().getStudents().size();
        
        try {
            int flags = caseInsensitive ? Pattern.CASE_INSENSITIVE : 0;
            Pattern compiledPattern = Pattern.compile(pattern, flags);
            
            for (Student s : ApplicationContext.getInstance().getStudents()) {
                stats.totalScanned++;
                Student student = s;
                try {
                    Object c = CacheManager.getInstance().get("student:" + s.getId());
                    if (c instanceof Student) student = (Student) c;
                } catch (Exception ex) { }
                String fieldValue = getFieldValue(student, fieldName);

                if (fieldValue != null) {
                    Matcher matcher = compiledPattern.matcher(fieldValue);

                    if (matcher.find()) {
                        String highlighted = highlightMatch(fieldValue, matcher);
                        SearchResult result = new SearchResult(student, fieldName, fieldValue, highlighted);
                        currentResults.add(result);
                        stats.matchesFound++;
                        addDistributionStats(student);
                    }
                }
            }
            
        } catch (PatternSyntaxException e) {
            throwPatternError(e);
        }
        
        stats.searchTimeMs = System.currentTimeMillis() - startTime;
        try { AuditLogger.getInstance().log("SEARCH_CUSTOM", "field=" + fieldName + ",pattern=" + pattern, stats.searchTimeMs, true, "matches=" + stats.matchesFound); } catch (Exception ex) { }
        return currentResults.stream().map(r -> r.student).collect(Collectors.toList());
    }

    /**
     * Display results with highlighted matching text
     */
    public void displayResults() {
        if (currentResults.isEmpty()) {
            System.out.println("\n❌ No students found matching pattern: " + lastPattern + "\n");
            displaySearchStats();
            return;
        }

        System.out.println("\n" + "=".repeat(120));
        System.out.println("||  REGEX SEARCH RESULTS  ||");
        System.out.println("=".repeat(120));
        System.out.println("Pattern: " + lastPattern);
        System.out.println("Found " + currentResults.size() + " student(s)\n");

        System.out.printf("%-6s %-20s %-15s %-20s %-12s %-10s %-10s\n",
                "ID", "NAME", "FIELD", "MATCHED VALUE", "AVG GRADE", "TYPE", "STATUS");
        System.out.println("-".repeat(120));

        for (SearchResult result : currentResults) {
            System.out.printf("%-6d %-20s %-15s %-20s %-12.2f %-10s %-10s\n",
                    result.student.id,
                    result.student.name,
                    result.matchedField,
                    result.matchedValue,
                    result.student.getAverageGrade(),
                    result.student.getType(),
                    result.student.getStatus());
            System.out.println("  ↳ Highlighted: " + result.highlightedMatch);
        }

        System.out.println("-".repeat(120));
        displaySearchStats();
    }

    /**
     * Display detailed statistics about the search
     */
    private void displaySearchStats() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("SEARCH STATISTICS");
        System.out.println("=".repeat(70));
        System.out.printf("Total Students Scanned: %d\n", stats.totalScanned);
        System.out.printf("Matches Found: %d (%.2f%% hit rate)\n", 
                stats.matchesFound, 
                stats.totalStudents > 0 ? (stats.matchesFound * 100.0 / stats.totalStudents) : 0);
        System.out.printf("Search Time: %d ms\n", stats.searchTimeMs);
        System.out.printf("Pattern Complexity: %s\n", getPatternComplexityHint(lastPattern));
        
        // Distribution stats
        System.out.println("\nDISTRIBUTION BY TYPE:");
        stats.distributionByType.forEach((type, count) -> 
            System.out.printf("  - %s: %d students\n", type, count));
        
        System.out.println("\nDISTRIBUTION BY STATUS:");
        stats.distributionByStatus.forEach((status, count) -> 
            System.out.printf("  - %s: %d students\n", status, count));
        
        System.out.println("=".repeat(70) + "\n");
    }

    /**
     * Display bulk operation menu for search results
     */
    public void displayBulkOperationsMenu() {
        if (currentResults.isEmpty()) {
            System.out.println("No results to perform bulk operations on.");
            return;
        }

        System.out.println("\n" + "=".repeat(70));
        System.out.println("BULK OPERATIONS ON SEARCH RESULTS (" + currentResults.size() + " students)");
        System.out.println("=".repeat(70));
        System.out.println("1. Display Full Details");
        System.out.println("2. Export Results to CSV");
        System.out.println("3. Calculate Statistics");
        System.out.println("4. Back to Search Menu");
        System.out.print("Enter choice: ");
    }

    public void performBulkOperation(int choice) {
        if (choice == 1) {
            displayFullDetails();
        } else if (choice == 2) {
            exportToCSV();
        } else if (choice == 3) {
            calculateBulkStatistics();
        } else {
            System.out.println("Invalid choice.");
        }
    }

    private void displayFullDetails() {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("FULL DETAILS - SEARCH RESULTS");
        System.out.println("=".repeat(100));

        for (SearchResult result : currentResults) {
            Student s = result.student;
            System.out.println("\n📋 Student ID: " + s.id);
            System.out.println("   Name: " + s.name);
            System.out.println("   Age: " + s.age);
            System.out.println("   Email: " + s.email);
            System.out.println("   Phone: " + s.phone);
            System.out.println("   Type: " + s.getType());
            System.out.println("   Status: " + s.getStatus());
            System.out.println("   Average Grade: " + String.format("%.2f", s.getAverageGrade()));
            System.out.println("   Subjects Enrolled: " + ApplicationContext.getInstance().getGradeManager().getSubjectCountForStudent(s.id));
            System.out.println("   Matched Field: " + result.matchedField + " → " + result.highlightedMatch);
        }

        System.out.println("\n" + "=".repeat(100) + "\n");
    }

    private void exportToCSV() {
        System.out.println("\n✅ CSV Export Feature:");
        System.out.println("CSV format: ID,Name,Type,Status,AvgGrade,MatchedField,MatchedValue");
        System.out.println("---");

        for (SearchResult result : currentResults) {
            System.out.printf("%d,%s,%s,%s,%.2f,%s,%s\n",
                    result.student.id,
                    result.student.name,
                    result.student.getType(),
                    result.student.getStatus(),
                    result.student.getAverageGrade(),
                    result.matchedField,
                    result.matchedValue);
        }
        System.out.println("---\n");
    }

    private void calculateBulkStatistics() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("BULK STATISTICS");
        System.out.println("=".repeat(70));

        double totalGrade = currentResults.stream()
                .mapToDouble(r -> r.student.getAverageGrade())
                .filter(g -> g > 0)
                .sum();

        long gradedCount = currentResults.stream()
                .map(r -> r.student.getAverageGrade())
                .filter(g -> g > 0)
                .count();

        System.out.printf("Total Students in Results: %d\n", currentResults.size());
        if (gradedCount > 0) {
            System.out.printf("Average Grade (Results): %.2f\n", totalGrade / gradedCount);
        }

        long passingCount = currentResults.stream()
                .filter(r -> r.student.getStatus() != null && r.student.getStatus().equals("Passing"))
                .count();

        System.out.printf("Passing Students: %d (%.2f%%)\n", passingCount, 
                (passingCount * 100.0 / currentResults.size()));
        System.out.printf("Failing Students: %d (%.2f%%)\n", currentResults.size() - passingCount,
                ((currentResults.size() - passingCount) * 100.0 / currentResults.size()));

        System.out.println("=".repeat(70) + "\n");
    }

    // ==================== Helper Methods ====================

    private String extractDomain(String email) {
        if (email == null || !email.contains("@")) return "";
        return email.substring(email.indexOf("@") + 1);
    }

    private String highlightMatch(String text, Matcher matcher) {
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "►" + matcher.group() + "◄");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String getFieldValue(Student s, String fieldName) {
        return switch (fieldName.toLowerCase()) {
            case "name" -> s.name;
            case "email" -> s.email;
            case "phone" -> s.phone;
            case "type" -> s.getType();
            case "status" -> s.getStatus();
            case "id" -> String.valueOf(s.id);
            default -> null;
        };
    }

    private void addDistributionStats(Student s) {
        String type = s.getType();
        stats.distributionByType.put(type, stats.distributionByType.getOrDefault(type, 0) + 1);
        
        String status = s.getStatus();
        stats.distributionByStatus.put(status, stats.distributionByStatus.getOrDefault(status, 0) + 1);
    }

    private String getPatternComplexityHint(String pattern) {
        int complexity = 0;
        if (pattern.contains("(") || pattern.contains(")")) complexity++;
        if (pattern.contains("[") || pattern.contains("]")) complexity++;
        if (pattern.contains("?") || pattern.contains("*") || pattern.contains("+")) complexity++;
        if (pattern.contains("|")) complexity++;
        if (pattern.contains("\\")) complexity++;
        if (pattern.contains("^") || pattern.contains("$")) complexity++;
        if (pattern.contains(".")) complexity++;

        return switch (complexity) {
            case 0, 1 -> "🟢 Simple (Basic literal/wildcard)";
            case 2, 3 -> "🟡 Moderate (Groups, character classes, quantifiers)";
            default -> "🔴 Complex (Advanced regex features)";
        };
    }

    private void throwPatternError(PatternSyntaxException e) {
        System.out.println("\n❌ REGEX PATTERN ERROR ❌");
        System.out.println("Error: " + e.getDescription());
        System.out.println("Pattern: " + lastPattern);
        System.out.println("Position: " + e.getIndex());
        System.out.println("\nCommon Regex Mistakes:");
        System.out.println("  • Unescaped special characters: . * + ? [ ] ( ) { } ^ $ |");
        System.out.println("  • Use backslash (\\) to escape: \\. for literal dot");
        System.out.println("  • Use [0-9]+ for numbers, [a-z]+ for lowercase letters");
        System.out.println("  • Use .* for any characters, .+ for at least one character");
        System.out.println();
    }

    // Overloaded methods for Menu integration (without boolean parameter)
    public ArrayList<SearchResult> searchByEmailDomain(String domainPattern) {
        searchByEmailDomain(domainPattern, false);
        return new ArrayList<>(currentResults);
    }

    public ArrayList<SearchResult> searchByIdPattern(String idPattern) {
        searchByIdPattern(idPattern, false);
        return new ArrayList<>(currentResults);
    }

    public ArrayList<SearchResult> searchByNamePattern(String namePattern) {
        searchByNamePattern(namePattern, false);
        return new ArrayList<>(currentResults);
    }

    public ArrayList<SearchResult> searchByPhoneAreaCode(String areaCodePattern) {
        long startTime = System.currentTimeMillis();
        this.caseInsensitive = false;
        this.lastPattern = areaCodePattern;
        
        currentResults.clear();
        stats = new SearchStats();
        stats.totalStudents = ApplicationContext.getInstance().getStudents().size();
        
        try {
            Pattern pattern = Pattern.compile(areaCodePattern);
            
            for (Student s : ApplicationContext.getInstance().getStudents()) {
                stats.totalScanned++;
                String areaCode = extractAreaCode(s.getPhone());
                Matcher matcher = pattern.matcher(areaCode);
                
                if (matcher.find()) {
                    String highlighted = highlightMatch(areaCode, matcher);
                    SearchResult result = new SearchResult(s, "Phone Area Code", areaCode, highlighted);
                    currentResults.add(result);
                    stats.matchesFound++;
                    addDistributionStats(s);
                }
            }
            
        } catch (PatternSyntaxException e) {
            throwPatternError(e);
        }
        
        stats.searchTimeMs = System.currentTimeMillis() - startTime;
        return new ArrayList<>(currentResults);
    }

    public ArrayList<SearchResult> searchByCustomPattern(String field, String pattern) {
        searchByCustomPattern(pattern, field, false);
        return new ArrayList<>(currentResults);
    }

    private String extractAreaCode(String phone) {
        if (phone == null || phone.isEmpty()) return "";
        // Assuming format like (123) 456-7890
        int start = phone.indexOf('(');
        int end = phone.indexOf(')');
        if (start >= 0 && end > start) {
            return phone.substring(start + 1, end);
        }
        // Fallback: first 3 digits
        String digits = phone.replaceAll("[^0-9]", "");
        return digits.substring(0, Math.min(3, digits.length()));
    }

    public void displayResults(ArrayList<SearchResult> results) {
        if (results.isEmpty()) return;
        
        for (SearchResult result : results) {
            System.out.println("► " + result.student.getName() + " (ID: " + result.student.getId() + ") - " + result.highlightedMatch + " ◄");
        }
    }

    public void displaySearchStatsMenu() {
        if (stats == null) return;
        
        System.out.println("Pattern: " + lastPattern);
        System.out.println("Total Scanned: " + stats.totalScanned + " students");
        System.out.println("Matches Found: " + stats.matchesFound);
        System.out.println("Search Time: " + stats.searchTimeMs + "ms");
        System.out.println("Match Rate: " + String.format("%.2f%%", (stats.matchesFound * 100.0 / stats.totalScanned)));
        System.out.println("Pattern Complexity: " + getPatternComplexityHint(lastPattern));
    }

    public void performBulkOperation(ArrayList<SearchResult> results, String operation) {
        if (operation.equalsIgnoreCase("csv")) {
            System.out.println("\nExporting to CSV format:");
            System.out.println("StudentID,Name,Email,Phone,GPA,Type,Status");
            for (SearchResult result : results) {
                Student s = result.student;
                System.out.println(s.getId() + "," + s.getName() + "," + s.getEmail() + "," + 
                                  s.getPhone() + "," + String.format("%.2f", s.computeGPA()) + "," +
                                  s.getType() + "," + s.getStatus());
            }
        } else if (operation.equalsIgnoreCase("stats")) {
            System.out.println("\nSearch Statistics:");
            System.out.println("Total Matches: " + results.size());
            
            double avgGPA = results.stream()
                .mapToDouble(r -> r.student.computeGPA())
                .average()
                .orElse(0);
            System.out.println("Average GPA: " + String.format("%.2f", avgGPA));
            
            long honors = results.stream()
                .filter(r -> r.student instanceof HonorsStudent)
                .count();
            System.out.println("Honors Students: " + honors);
            
            long regular = results.stream()
                .filter(r -> r.student instanceof RegularStudent)
                .count();
            System.out.println("Regular Students: " + regular);
        }
    }

    public List<Student> getCurrentResults() {
        return currentResults.stream().map(r -> r.student).collect(Collectors.toList());
    }

    public boolean hasResults() {
        return !currentResults.isEmpty();
    }
}

