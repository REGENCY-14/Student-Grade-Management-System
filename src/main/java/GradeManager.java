import exception.StudentNotFoundException;
import exception.GradeStorageFullException;

import java.util.ArrayList;
import java.util.Comparator;

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
        } else {
            throw new GradeStorageFullException("Cannot add grade. Storage limit of " + grades.length + " reached.");
        }
    }

    @Override
    public void viewGradeByStudent(int studentId) throws StudentNotFoundException {
        // Existing implementation...
    }

    @Override
    public double calculateCoreAverage(int studentId) {
        // Existing implementation...
        return 0;
    }

    @Override
    public double calculateElectiveAverage(int studentId) {
        // Existing implementation...
        return 0;
    }

    @Override
    public double calculateOverallAverage(int studentId) throws StudentNotFoundException {
        // Existing implementation...
        return 0;
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
