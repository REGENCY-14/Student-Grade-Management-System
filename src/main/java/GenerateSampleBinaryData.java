import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Utility to generate sample binary grade data for testing
 */
public class GenerateSampleBinaryData {

    static class GradeData implements Serializable {
        private static final long serialVersionUID = 1L;
        int studentId;
        String subjectName;
        String subjectType;
        double grade;

        GradeData(int studentId, String subjectName, String subjectType, double grade) {
            this.studentId = studentId;
            this.subjectName = subjectName;
            this.subjectType = subjectType;
            this.grade = grade;
        }
    }

    public static void main(String[] args) {
        try {
            // Ensure directory exists
            Files.createDirectories(Paths.get("./imports/binary"));
            
            // Sample grade data
            GradeData[] grades = {
                new GradeData(1001, "Mathematics", "Core", 85.5),
                new GradeData(1001, "Physics", "Core", 78.0),
                new GradeData(1001, "Chemistry", "Core", 92.5),
                new GradeData(1002, "Mathematics", "Core", 88.0),
                new GradeData(1002, "Biology", "Core", 91.0),
                new GradeData(1002, "English", "Core", 87.5),
                new GradeData(1003, "History", "Elective", 76.5),
                new GradeData(1003, "Music", "Elective", 95.0),
                new GradeData(1003, "Mathematics", "Core", 82.0),
                new GradeData(1004, "Computer Science", "Elective", 94.5),
                new GradeData(1004, "Mathematics", "Core", 89.0),
                new GradeData(1004, "Physics", "Core", 86.5),
                new GradeData(1005, "Art", "Elective", 90.0),
                new GradeData(1005, "English", "Core", 84.5),
                new GradeData(1005, "Chemistry", "Core", 88.0),
                new GradeData(1006, "Mathematics", "Core", 92.0),
                new GradeData(1006, "Geography", "Elective", 79.5),
                new GradeData(1006, "Physics", "Core", 85.0),
                new GradeData(1007, "Biology", "Core", 93.5),
                new GradeData(1007, "Chemistry", "Core", 91.5),
                new GradeData(1007, "Music", "Elective", 88.5),
                new GradeData(1008, "Mathematics", "Core", 77.0),
                new GradeData(1008, "History", "Elective", 82.0),
                new GradeData(1008, "English", "Core", 80.5),
                new GradeData(1009, "Physics", "Core", 96.0),
                new GradeData(1009, "Computer Science", "Elective", 97.5),
                new GradeData(1009, "Mathematics", "Core", 95.5),
                new GradeData(1010, "Art", "Elective", 86.0),
                new GradeData(1010, "Geography", "Elective", 81.5),
                new GradeData(1010, "English", "Core", 83.0)
            };

            try (ObjectOutputStream oos = new ObjectOutputStream(
                    Files.newOutputStream(Paths.get("./imports/binary/sample_grades_import.bin"),
                            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
                
                // Write the count first
                oos.writeInt(grades.length);
                
                // Write each grade object
                for (GradeData grade : grades) {
                    oos.writeObject(grade);
                }
                
                System.out.println("✓ Successfully created sample_grades_import.bin");
                System.out.println("  Records: " + grades.length);
                System.out.println("  Location: ./imports/binary/sample_grades_import.bin");
            }

        } catch (Exception e) {
            System.err.println("❌ Error creating binary file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
