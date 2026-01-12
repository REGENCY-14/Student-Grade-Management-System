package manager;

import core.Grade;
import core.Student;
import context.ApplicationContext;
import audit.AuditLogger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import exception.GradeStorageFullException;
import exception.StudentNotFoundException;

public class GradeManager implements IGradeManager {

    public Grade[] grades = new Grade[200];
    private int gradeCount = 0;

    public void clear() {
        Arrays.fill(grades, null);
        gradeCount = 0;
    }

    @Override
    public void addGrade(Grade grade) throws StudentNotFoundException, GradeStorageFullException {
        Student s = findStudentById(grade.getStudentId());
        if (s == null) {
            throw new StudentNotFoundException("Student with ID " + grade.getStudentId() + " not found.");
        }

        long start = System.currentTimeMillis();
        if (gradeCount < grades.length) {
            try {
                grades[gradeCount++] = grade;
                updateStudentAverage(grade.getStudentId());
                // invalidate cache entries related to this student
                try {
                    CacheManager.getInstance().invalidate("student:" + grade.getStudentId());
                    CacheManager.getInstance().invalidate("report:" + grade.getStudentId());
                } catch (Exception ex) {
                    // ignore cache errors
                }
                long exec = System.currentTimeMillis() - start;
                try { AuditLogger.getInstance().log("ADD_GRADE", "studentId=" + grade.getStudentId() + ",subject=" + grade.getSubject().getSubjectName(), exec, true, ""); } catch (Exception ex) { }
            } catch (RuntimeException rex) {
                long exec = System.currentTimeMillis() - start;
                try { AuditLogger.getInstance().log("ADD_GRADE", "studentId=" + grade.getStudentId(), exec, false, rex.getMessage()); } catch (Exception ex) { }
                throw rex;
            }
        } else {
            long exec = System.currentTimeMillis() - start;
            try { AuditLogger.getInstance().log("ADD_GRADE", "studentId=" + grade.getStudentId(), exec, false, "Storage full"); } catch (Exception ex) { }
            throw new GradeStorageFullException("Cannot add grade. Storage limit of " + grades.length + " reached.");
        }
    }

    @Override
    public void viewGradeByStudent(int studentId) throws StudentNotFoundException {
        // 1️⃣ Check if student exists first
        Student s = findStudentById(studentId);
        if (s == null) {
            throw new StudentNotFoundException("Student with ID " + studentId + " does not exist.");
        }
        
        // Use Stream API to collect and sort grades
        ArrayList<Grade> studentGrades = Arrays.stream(grades)
                .limit(gradeCount)
                .filter(g -> g.getStudentId() == studentId)
                .sorted(Comparator.comparing(Grade::getDate).reversed())
                .collect(ArrayList::new, List::add, List::addAll);

        if (studentGrades.isEmpty()) {
            System.out.println("No grades recorded for this student.");
            return;
        }

        System.out.println("\n--------- GRADE REPORT FOR STUDENT " + studentId + " ---------");
        System.out.printf("%-8s %-12s %-20s %-10s %-8s\n",
                "GradeID", "Date", "Subject", "Type", "Grade");
        System.out.println("-------------------------------------------------------------");

        // Use Stream API to display grades
        Arrays.stream(grades)
                .limit(gradeCount)
                .filter(g -> g.getStudentId() == studentId)
                .sorted(Comparator.comparing(Grade::getDate).reversed())
                .forEach(g -> {
                    Student student = findStudentById(g.getStudentId());
                    String status = (student != null) ? student.getStatus() : "N/A";
                    System.out.printf("%-8d %-12s %-20s %-10s %-8.2f %-8s\n",
                            g.getGradeId(),
                            g.getDate(),
                            g.getSubject().getSubjectName(),
                            g.getSubject().getSubjectType(),
                            g.getGrade(),
                            status
                    );
                });

        System.out.println("-------------------------------------------------------------");

        double coreAvg = calculateCoreAverage(studentId);
        double electAvg = calculateElectiveAverage(studentId);
        double overallAvg = calculateOverallAverage(studentId);

        System.out.println("Core Subjects Average: " +
                (coreAvg == -1 ? "N/A" : String.format("%.2f", coreAvg)));

        System.out.println("Elective Subjects Average: " +
                (electAvg == -1 ? "N/A" : String.format("%.2f", electAvg)));

        System.out.println("Current Average: " +
                (overallAvg == -1 ? "N/A" : String.format("%.2f", overallAvg)));

        System.out.println("-------------------------------------------------------------\n");
    }

    @Override
    public double calculateCoreAverage(int studentId) {
        return Arrays.stream(grades)
                .limit(gradeCount)
                .filter(g -> g.getStudentId() == studentId && g.getSubject().getSubjectType().equals("Core"))
                .mapToDouble(Grade::getGrade)
                .average()
                .orElse(-1);
    }

    @Override
    public double calculateElectiveAverage(int studentId) {
        return Arrays.stream(grades)
                .limit(gradeCount)
                .filter(g -> g.getStudentId() == studentId && g.getSubject().getSubjectType().equals("Elective"))
                .mapToDouble(Grade::getGrade)
                .average()
                .orElse(-1);
    }

    @Override
    public double calculateOverallAverage(int studentId) throws StudentNotFoundException {
        if (findStudentById(studentId) == null) {
            throw new StudentNotFoundException("Student with ID " + studentId + " not found.");
        }

        return Arrays.stream(grades)
                .limit(gradeCount)
                .filter(g -> g.getStudentId() == studentId)
                .mapToDouble(Grade::getGrade)
                .average()
                .orElse(-1);
    }

    @Override
    public int getGradeCount() {
        return gradeCount;
    }

    @Override
    public int getSubjectCountForStudent(int studentId) {
        return (int) Arrays.stream(grades)
                .limit(gradeCount)
                .filter(g -> g.getStudentId() == studentId)
                .count();
    }

    @Override
    public ArrayList<Integer> getGradesForStudent(int studentId) {
        return Arrays.stream(grades)
                .limit(gradeCount)
                .filter(g -> g.getStudentId() == studentId)
                .map(g -> (int) g.getGrade())
                .collect(ArrayList::new, List::add, List::addAll);
    }

    // Safe wrapper for external callers that should not throw
    public double calculateOverallAverageSafe(int studentId) {
        try {
            return calculateOverallAverage(studentId);
        } catch (StudentNotFoundException ex) {
            return -1;
        }
    }

    // Helper methods
    private void updateStudentAverage(int studentId) {
        double average = Arrays.stream(grades)
                .limit(gradeCount)
                .filter(g -> g.getStudentId() == studentId)
                .mapToDouble(Grade::getGrade)
                .average()
                .orElse(-1);

        Student s = findStudentById(studentId);
        if (s != null && average > 0) {
            s.setAverageGrade(average);
        }
    }

    /**
     * Optimized student lookup using Stream API.
     * First tries HashMap-backed index (ApplicationContext.studentIndex) O(1),
     * Falls back to Stream search O(n) if index is empty.
     */
    private Student findStudentById(int studentId) {
        Student fromIndex = ApplicationContext.getInstance().getStudentIndex().get(String.valueOf(studentId));
        if (fromIndex != null) {
            return fromIndex;
        }
        // Fallback: search in ApplicationContext.students using streams
        return ApplicationContext.getInstance().getStudents().stream()
                .filter(s -> s.getId() == studentId)
                .findFirst()
                .orElse(null);
    }
}
