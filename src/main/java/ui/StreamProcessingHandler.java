package ui;

import context.ApplicationContext;
import core.Student;
import core.Grade;
import manager.GradeManager;
import models.HonorsStudent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * StreamProcessingHandler - Handles all stream processing operations
 * Responsibility: Stream-based analytics and data processing
 */
public class StreamProcessingHandler {
    private final ApplicationContext context;
    private final Scanner scanner;
    private final ArrayList<Student> students;
    private final GradeManager gradeManager;

    public StreamProcessingHandler(ApplicationContext context, Scanner scanner,
                                   ArrayList<Student> students, GradeManager gradeManager) {
        this.context = context;
        this.scanner = scanner;
        this.students = students;
        this.gradeManager = gradeManager;
    }

    public void openStreamProcessingMenu() {
        boolean inStream = true;

        while (inStream) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("STREAM PROCESSING & ANALYTICS");
            System.out.println("=".repeat(60));
            System.out.println("1. Count students by enrollment status");
            System.out.println("2. Average grade by subject");
            System.out.println("3. Extract unique course codes");
            System.out.println("4. Top 5 students by average grade");
            System.out.println("5. Partition students by honors eligibility");
            System.out.println("6. Process large CSV file (sequential vs parallel)");
            System.out.println("7. Sequential vs parallel GPA comparison");
            System.out.println("8. Back to Main Menu");
            System.out.print("Select option: ");

            try {
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        streamCountEnrollmentStatus();
                        break;
                    case 2:
                        streamAverageGradeBySubject();
                        break;
                    case 3:
                        streamUniqueCourseCodes();
                        break;
                    case 4:
                        streamTop5ByAverageGrade();
                        break;
                    case 5:
                        streamPartitionHonorsEligibility();
                        break;
                    case 6:
                        streamProcessLargeCsv();
                        break;
                    case 7:
                        streamSequentialVsParallelComparison();
                        break;
                    case 8:
                        inStream = false;
                        break;
                    default:
                        System.out.println("Invalid option!");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                scanner.nextLine();
            }
        }
    }

    // 1. Count students by enrollment status using groupingBy
    private void streamCountEnrollmentStatus() {
        if (students.isEmpty()) {
            System.out.println("No students in system.");
            return;
        }

        long start = System.nanoTime();
        Map<String, Long> countByStatus = students.stream()
                .collect(Collectors.groupingBy(Student::getStatus, Collectors.counting()));
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        System.out.println("\nStudent count by enrollment status:");
        countByStatus.forEach((status, count) ->
                System.out.printf("%-20s : %d%n", status, count));
        System.out.println("Execution time: " + durationMs + " ms");
    }

    // 2. Average grade by subject using groupingBy and averaging
    private void streamAverageGradeBySubject() {
        if (gradeManager.getGradeCount() == 0) {
            System.out.println("No grades recorded.");
            return;
        }

        long start = System.nanoTime();
        List<Grade> gradesList = new ArrayList<>();
        for (int i = 0; i < gradeManager.getGradeCount(); i++) {
            if (gradeManager.grades[i] != null) {
                gradesList.add(gradeManager.grades[i]);
            }
        }

        Map<String, Double> result = gradesList.stream()
                .collect(Collectors.groupingBy(
                        g -> g.getSubject().getSubjectName(),
                        Collectors.averagingDouble(Grade::getGrade)
                ));
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        System.out.println("\nAverage grade per subject:");
        result.forEach((subject, avg) ->
                System.out.printf("%-25s : %.2f%n", subject, avg));
        System.out.println("Execution time: " + durationMs + " ms");
    }

    // 3. Extract unique course codes
    private void streamUniqueCourseCodes() {
        if (gradeManager.getGradeCount() == 0) {
            System.out.println("No grades recorded.");
            return;
        }

        long start = System.nanoTime();
        List<Grade> gradesList = new ArrayList<>();
        for (int i = 0; i < gradeManager.getGradeCount(); i++) {
            if (gradeManager.grades[i] != null) {
                gradesList.add(gradeManager.grades[i]);
            }
        }

        Set<String> result = gradesList.stream()
                .filter(g -> g.getSubject() != null)
                .map(g -> g.getSubject().getSubjectCode())
                .filter(code -> code != null && !code.isEmpty())
                .collect(Collectors.toSet());
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        System.out.println("\nUnique course codes used in recorded grades:");
        result.forEach(System.out::println);
        System.out.println("Execution time: " + durationMs + " ms");
    }

    // 4. Find top 5 students by average grade
    private void streamTop5ByAverageGrade() {
        if (students.isEmpty()) {
            System.out.println("No students in system.");
            return;
        }

        long start = System.nanoTime();
        List<Student> top5 = students.stream()
                .filter(s -> s.getEnrolledSubjects() > 0)
                .sorted((a, b) -> Double.compare(b.getAverageGrade(), a.getAverageGrade()))
                .limit(5)
                .collect(Collectors.toList());
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        System.out.println("\nTop 5 students by average grade:");
        int rank = 1;
        for (Student s : top5) {
            System.out.printf("%d. %-20s | ID: %d | Avg Grade: %.2f | GPA: %.2f%n",
                    rank++, s.getName(), s.getId(), s.getAverageGrade(), s.computeGPA());
        }
        System.out.println("Execution time: " + durationMs + " ms");
    }

    // 5. Partition students by honors eligibility
    private void streamPartitionHonorsEligibility() {
        if (students.isEmpty()) {
            System.out.println("No students in system.");
            return;
        }

        long start = System.nanoTime();
        Map<Boolean, List<Student>> partitioned = students.stream()
                .collect(Collectors.partitioningBy(Student::isEligibleForHonors));
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        System.out.println("\nPartitioned students by honors eligibility:");
        System.out.println("Eligible for honors: " + partitioned.get(true).size());
        System.out.println("Not eligible: " + partitioned.get(false).size());
        System.out.println("Execution time: " + durationMs + " ms");
    }

    // 6. Process large CSV file using Files.lines() stream
    private void streamProcessLargeCsv() {
        System.out.print("Enter CSV file name in ./imports (without extension): ");
        String fileName = scanner.nextLine().trim();
        Path path = Path.of("./imports/" + fileName + ".csv");

        if (!path.toFile().exists()) {
            System.out.println("File not found: " + path.toAbsolutePath());
            return;
        }

        try {
            long startSeq = System.nanoTime();
            long validLines;
            try (Stream<String> lines = Files.lines(path)) {
                validLines = lines
                        .skip(1) // skip header
                        .filter(line -> !line.trim().isEmpty())
                        .count();
            }
            long seqMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startSeq);

            long startPar = System.nanoTime();
            long validLinesParallel;
            try (Stream<String> lines = Files.lines(path).parallel()) {
                validLinesParallel = lines
                        .skip(1)
                        .filter(line -> !line.trim().isEmpty())
                        .count();
            }
            long parMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startPar);

            System.out.println("\nCSV processing with Files.lines():");
            System.out.println("Valid (non-empty) data rows: " + validLines);
            System.out.println("Sequential stream time: " + seqMs + " ms");
            System.out.println("Parallel stream time:   " + parMs + " ms");
        } catch (IOException e) {
            System.out.println("Error processing CSV: " + e.getMessage());
        }
    }

    // 7. Compare sequential vs parallel stream processing
    private void streamSequentialVsParallelComparison() {
        if (students.isEmpty()) {
            System.out.println("No students in system.");
            return;
        }

        long startSeq = System.nanoTime();
        double sumSeq = students.stream()
                .mapToDouble(Student::computeGPA)
                .filter(gpa -> gpa > 2.5)
                .sum();
        long seqMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startSeq);

        long startPar = System.nanoTime();
        double sumPar = students.parallelStream()
                .mapToDouble(Student::computeGPA)
                .filter(gpa -> gpa > 2.5)
                .sum();
        long parMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startPar);

        System.out.println("\nSequential vs Parallel stream comparison (GPA > 2.5 sum):");
        System.out.printf("Sequential sum: %.2f (time: %d ms)%n", sumSeq, seqMs);
        System.out.printf("Parallel   sum: %.2f (time: %d ms)%n", sumPar, parMs);
    }
}
