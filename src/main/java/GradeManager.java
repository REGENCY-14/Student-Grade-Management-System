import java.util.ArrayList;
import java.util.Comparator;

import exception.GradeStorageFullException;
import exception.StudentNotFoundException;

public class GradeManager implements IGradeManager {

    Grade[] grades = new Grade[200];
    private int gradeCount = 0;

    @Override
    public void addGrade(Grade grade) throws StudentNotFoundException, GradeStorageFullException {
        Student s = findStudentById(grade.getStudentId());
        if (s == null) {
            throw new StudentNotFoundException("Student with ID " + grade.getStudentId() + " not found.");
        }

        if (gradeCount < grades.length) {
            grades[gradeCount++] = grade;
            updateStudentAverage(grade.getStudentId());
            // invalidate cache entries related to this student
            try {
                CacheManager.getInstance().invalidate("student:" + grade.getStudentId());
                CacheManager.getInstance().invalidate("report:" + grade.getStudentId());
            } catch (Exception ex) {
                // ignore cache errors
            }
        } else {
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
        ArrayList<Grade> studentGrades = new ArrayList<>();

        // Collect grades
        for (int i = 0; i < gradeCount; i++) {
            if (grades[i].getStudentId() == studentId) {
                studentGrades.add(grades[i]);
            }
        }

        if (studentGrades.isEmpty()) {
            System.out.println("No grades recorded for this student.");
            return;
        }

        studentGrades.sort(Comparator.comparing(Grade::getDate).reversed());

        System.out.println("\n--------- GRADE REPORT FOR STUDENT " + studentId + " ---------");
        System.out.printf("%-8s %-12s %-20s %-10s %-8s\n",
                "GradeID", "Date", "Subject", "Type", "Grade");
        System.out.println("-------------------------------------------------------------");

        for (Grade g : studentGrades) {
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
        }

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
        double total = 0;
        int count = 0;

        for (int i = 0; i < gradeCount; i++) {
            Grade g = grades[i];
            if (g.getStudentId() == studentId &&
                    g.getSubject().getSubjectType().equals("Core")) {
                total += g.getGrade();
                count++;
            }
        }

        return count == 0 ? -1 : total / count;
    }

    @Override
    public double calculateElectiveAverage(int studentId) {
        double total = 0;
        int count = 0;

        for (int i = 0; i < gradeCount; i++) {
            Grade g = grades[i];
            if (g.getStudentId() == studentId &&
                    g.getSubject().getSubjectType().equals("Elective")) {
                total += g.getGrade();
                count++;
            }
        }

        return count == 0 ? -1 : total / count;
    }

    @Override
    public double calculateOverallAverage(int studentId) throws StudentNotFoundException {
        if (findStudentById(studentId) == null) {
            throw new StudentNotFoundException("Student with ID " + studentId + " not found.");
        }

        double total = 0;
        int count = 0;

        for (int i = 0; i < gradeCount; i++) {
            if (grades[i].getStudentId() == studentId) {
                total += grades[i].getGrade();
                count++;
            }
        }

        return count == 0 ? -1 : total / count;
    }

    @Override
    public int getGradeCount() {
        return gradeCount;
    }

    @Override
    public int getSubjectCountForStudent(int studentId) {
        int count = 0;
        for (int i = 0; i < gradeCount; i++) {
            if (grades[i].getStudentId() == studentId) count++;
        }
        return count;
    }

    @Override
    public ArrayList<Integer> getGradesForStudent(int studentId) {
        ArrayList<Integer> studentGrades = new ArrayList<>();
        for (int i = 0; i < gradeCount; i++) {
            if (grades[i].getStudentId() == studentId) {
                studentGrades.add((int) grades[i].getGrade());
            }
        }
        return studentGrades;
    }

    // Safe wrapper for external callers that should not throw
    public double calculateOverallAverageSafe(int studentId) {
        try {
            return calculateOverallAverage(studentId);
        } catch (StudentNotFoundException ex) {
            return -1;
        }
    }

    // ---------------- Helper methods ----------------
    private void updateStudentAverage(int studentId) {
        double total = 0;
        int count = 0;
        for (int i = 0; i < gradeCount; i++) {
            if (grades[i].getStudentId() == studentId) {
                total += grades[i].getGrade();
                count++;
            }
        }

        Student s = findStudentById(studentId);
        if (s != null && count > 0) {
            s.setAverageGrade(total / count);
        }
    }

    private Student findStudentById(int studentId) {
        for (Student s : Menu.students) {
            if (s.id == studentId) return s;
        }
        return null;
    }
}
