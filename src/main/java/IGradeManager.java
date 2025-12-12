import exception.StudentNotFoundException;
import exception.GradeStorageFullException;
import java.util.ArrayList;

public interface IGradeManager {
    void addGrade(Grade grade) throws StudentNotFoundException, GradeStorageFullException;
    void viewGradeByStudent(int studentId) throws StudentNotFoundException;
    double calculateCoreAverage(int studentId);
    double calculateElectiveAverage(int studentId);
    double calculateOverallAverage(int studentId) throws StudentNotFoundException;
    int getGradeCount();
    int getSubjectCountForStudent(int studentId);
    ArrayList<Integer> getGradesForStudent(int studentId);
}
