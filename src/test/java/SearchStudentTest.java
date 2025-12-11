import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Search Students Feature Tests")
class SearchStudentTest {

    private GradeManager gradeManager;
    private RegularStudent student1;
    private RegularStudent student2;
    private HonorsStudent student3;
    private HonorsStudent student4;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() throws Exception {
        // Setup test data
        gradeManager = new GradeManager();
        Menu.gradeManager = gradeManager;
        Menu.students = new ArrayList<>();

        // Create test students
        student1 = new RegularStudent(1001, "John Doe", 18, "john@email.com", "1234567890");
        student2 = new RegularStudent(1002, "Jane Smith", 19, "jane@email.com", "0987654321");
        student3 = new HonorsStudent(1003, "Alice Johnson", 20, "alice@email.com", "1112223333");
        student4 = new HonorsStudent(1004, "Bob Williams", 18, "bob@email.com", "4445556666");

        Menu.students.add(student1);
        Menu.students.add(student2);
        Menu.students.add(student3);
        Menu.students.add(student4);

        // Add some grades
        CoreSubject math = new CoreSubject("Mathematics", "C-MATH");
        CoreSubject english = new CoreSubject("English", "C-ENG");
        ElectiveSubject music = new ElectiveSubject("Music", "E-MUS");

        gradeManager.addGrade(new Grade(1001, math, 85.0));
        gradeManager.addGrade(new Grade(1001, english, 90.0));

        gradeManager.addGrade(new Grade(1002, math, 75.0));
        gradeManager.addGrade(new Grade(1002, music, 80.0));

        gradeManager.addGrade(new Grade(1003, english, 95.0));
        gradeManager.addGrade(new Grade(1003, math, 92.0));

        gradeManager.addGrade(new Grade(1004, math, 55.0));
        gradeManager.addGrade(new Grade(1004, music, 60.0));

        // Redirect System.out for testing output
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
    }

    // ==================== SEARCH BY ID TESTS ====================

    @Test
    @DisplayName("Should find student by exact ID")
    void testSearchByStudentId_Found() {
        ArrayList<Student> results = new ArrayList<>();
        for (Student s : Menu.students) {
            if (s.id == 1001) {
                results.add(s);
                break;
            }
        }

        assertEquals(1, results.size());
        assertEquals(1001, results.get(0).id);
        assertEquals("John Doe", results.get(0).name);
    }

    @Test
    @DisplayName("Should return empty list when ID not found")
    void testSearchByStudentId_NotFound() {
        ArrayList<Student> results = new ArrayList<>();
        for (Student s : Menu.students) {
            if (s.id == 9999) {
                results.add(s);
                break;
            }
        }

        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should find multiple students with different IDs")
    void testSearchByStudentId_Multiple() {
        int[] searchIds = {1001, 1003};
        ArrayList<Student> results = new ArrayList<>();

        for (int searchId : searchIds) {
            for (Student s : Menu.students) {
                if (s.id == searchId) {
                    results.add(s);
                    break;
                }
            }
        }

        assertEquals(2, results.size());
    }

    // ==================== SEARCH BY NAME TESTS ====================

    @Test
    @DisplayName("Should find student by exact name")
    void testSearchByName_ExactMatch() {
        String searchName = "john doe";
        ArrayList<Student> results = new ArrayList<>();

        for (Student s : Menu.students) {
            if (s.name.toLowerCase().contains(searchName)) {
                results.add(s);
            }
        }

        assertEquals(1, results.size());
        assertEquals("John Doe", results.get(0).name);
    }

    @Test
    @DisplayName("Should find student by partial name")
    void testSearchByName_PartialMatch() {
        String searchName = "john";
        ArrayList<Student> results = new ArrayList<>();

        for (Student s : Menu.students) {
            if (s.name.toLowerCase().contains(searchName.toLowerCase())) {
                results.add(s);
            }
        }

        assertEquals(2, results.size()); // John Doe and Alice Johnson
        assertTrue(results.stream().anyMatch(s -> s.name.equals("John Doe")));
        assertTrue(results.stream().anyMatch(s -> s.name.equals("Alice Johnson")));
    }

    @Test
    @DisplayName("Should find student by last name")
    void testSearchByName_LastName() {
        String searchName = "smith";
        ArrayList<Student> results = new ArrayList<>();

        for (Student s : Menu.students) {
            if (s.name.toLowerCase().contains(searchName.toLowerCase())) {
                results.add(s);
            }
        }

        assertEquals(1, results.size());
        assertEquals("Jane Smith", results.get(0).name);
    }

    @Test
    @DisplayName("Should be case-insensitive")
    void testSearchByName_CaseInsensitive() {
        String[] searchTerms = {"JOHN", "john", "JoHn"};

        for (String term : searchTerms) {
            ArrayList<Student> results = new ArrayList<>();
            for (Student s : Menu.students) {
                if (s.name.toLowerCase().contains(term.toLowerCase())) {
                    results.add(s);
                }
            }
            assertEquals(2, results.size(), "Failed for search term: " + term);
        }
    }

    @Test
    @DisplayName("Should return empty list when name not found")
    void testSearchByName_NotFound() {
        String searchName = "xyz";
        ArrayList<Student> results = new ArrayList<>();

        for (Student s : Menu.students) {
            if (s.name.toLowerCase().contains(searchName.toLowerCase())) {
                results.add(s);
            }
        }

        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should handle single character search")
    void testSearchByName_SingleCharacter() {
        String searchName = "j";
        ArrayList<Student> results = new ArrayList<>();

        for (Student s : Menu.students) {
            if (s.name.toLowerCase().contains(searchName.toLowerCase())) {
                results.add(s);
            }
        }

        assertEquals(3, results.size()); // John, Jane, Alice Johnson
    }

    // ==================== SEARCH BY GRADE RANGE TESTS ====================

    @Test
    @DisplayName("Should find students within grade range")
    void testSearchByGradeRange_ValidRange() {
        double minGrade = 80.0;
        double maxGrade = 95.0;
        ArrayList<Student> results = new ArrayList<>();

        for (Student s : Menu.students) {
            double avgGrade = s.getAverageGrade();
            if (avgGrade >= minGrade && avgGrade <= maxGrade && avgGrade > 0) {
                results.add(s);
            }
        }

        assertEquals(2, results.size()); // John (87.5) and Alice (93.5)
    }

    @Test
    @DisplayName("Should find students with exact boundary grades")
    void testSearchByGradeRange_ExactBoundary() {
        double minGrade = 87.5;
        double maxGrade = 87.5;
        ArrayList<Student> results = new ArrayList<>();

        for (Student s : Menu.students) {
            double avgGrade = s.getAverageGrade();
            if (avgGrade >= minGrade && avgGrade <= maxGrade && avgGrade > 0) {
                results.add(s);
            }
        }

        assertEquals(1, results.size());
        assertEquals("John Doe", results.get(0).name);
    }

    @Test
    @DisplayName("Should exclude students with no grades")
    void testSearchByGradeRange_ExcludeNoGrades() {
        RegularStudent studentNoGrades = new RegularStudent(1005, "Test Student", 18, "test@email.com", "1234567890");
        Menu.students.add(studentNoGrades);

        double minGrade = 0.0;
        double maxGrade = 100.0;
        ArrayList<Student> results = new ArrayList<>();

        for (Student s : Menu.students) {
            double avgGrade = s.getAverageGrade();
            if (avgGrade >= minGrade && avgGrade <= maxGrade && avgGrade > 0) {
                results.add(s);
            }
        }

        assertFalse(results.contains(studentNoGrades));
    }

    @Test
    @DisplayName("Should return empty list for impossible range")
    void testSearchByGradeRange_ImpossibleRange() {
        double minGrade = 99.0;
        double maxGrade = 100.0;
        ArrayList<Student> results = new ArrayList<>();

        for (Student s : Menu.students) {
            double avgGrade = s.getAverageGrade();
            if (avgGrade >= minGrade && avgGrade <= maxGrade && avgGrade > 0) {
                results.add(s);
            }
        }

        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should find failing students")
    void testSearchByGradeRange_FailingStudents() {
        double minGrade = 0.0;
        double maxGrade = 60.0;
        ArrayList<Student> results = new ArrayList<>();

        for (Student s : Menu.students) {
            double avgGrade = s.getAverageGrade();
            if (avgGrade >= minGrade && avgGrade <= maxGrade && avgGrade > 0) {
                results.add(s);
            }
        }

        assertEquals(1, results.size()); // Bob Williams with 57.5
        assertEquals("Bob Williams", results.get(0).name);
    }

    @Test
    @DisplayName("Should find high-performing students")
    void testSearchByGradeRange_HighPerformers() {
        double minGrade = 90.0;
        double maxGrade = 100.0;
        ArrayList<Student> results = new ArrayList<>();

        for (Student s : Menu.students) {
            double avgGrade = s.getAverageGrade();
            if (avgGrade >= minGrade && avgGrade <= maxGrade && avgGrade > 0) {
                results.add(s);
            }
        }

        assertEquals(1, results.size()); // Alice Johnson with 93.5
    }

    // ==================== SEARCH BY STUDENT TYPE TESTS ====================

    @Test
    @DisplayName("Should find all regular students")
    void testSearchByType_Regular() {
        String targetType = "Regular";
        ArrayList<Student> results = new ArrayList<>();

        for (Student s : Menu.students) {
            if (s.getType().equals(targetType)) {
                results.add(s);
            }
        }

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(s -> s instanceof RegularStudent));
    }

    @Test
    @DisplayName("Should find all honors students")
    void testSearchByType_Honors() {
        String targetType = "Honors";
        ArrayList<Student> results = new ArrayList<>();

        for (Student s : Menu.students) {
            if (s.getType().equals(targetType)) {
                results.add(s);
            }
        }

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(s -> s instanceof HonorsStudent));
    }

    @Test
    @DisplayName("Should distinguish between student types correctly")
    void testSearchByType_Distinction() {
        ArrayList<Student> regularResults = new ArrayList<>();
        ArrayList<Student> honorsResults = new ArrayList<>();

        for (Student s : Menu.students) {
            if (s.getType().equals("Regular")) {
                regularResults.add(s);
            } else if (s.getType().equals("Honors")) {
                honorsResults.add(s);
            }
        }

        assertEquals(2, regularResults.size());
        assertEquals(2, honorsResults.size());

        // Verify no overlap
        for (Student regular : regularResults) {
            assertFalse(honorsResults.contains(regular));
        }
    }

    // ==================== COMBINED SEARCH TESTS ====================

    @Test
    @DisplayName("Should combine name and type search")
    void testCombinedSearch_NameAndType() {
        String searchName = "john";
        String targetType = "Regular";
        ArrayList<Student> results = new ArrayList<>();

        for (Student s : Menu.students) {
            if (s.name.toLowerCase().contains(searchName.toLowerCase())
                    && s.getType().equals(targetType)) {
                results.add(s);
            }
        }

        assertEquals(1, results.size());
        assertEquals("John Doe", results.get(0).name);
        assertEquals("Regular", results.get(0).getType());
    }

    @Test
    @DisplayName("Should combine grade range and type search")
    void testCombinedSearch_GradeRangeAndType() {
        double minGrade = 85.0;
        double maxGrade = 100.0;
        String targetType = "Honors";
        ArrayList<Student> results = new ArrayList<>();

        for (Student s : Menu.students) {
            double avgGrade = s.getAverageGrade();
            if (avgGrade >= minGrade && avgGrade <= maxGrade
                    && avgGrade > 0 && s.getType().equals(targetType)) {
                results.add(s);
            }
        }

        assertEquals(1, results.size());
        assertEquals("Alice Johnson", results.get(0).name);
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Should handle empty student list")
    void testSearchWithEmptyList() {
        Menu.students.clear();

        ArrayList<Student> results = new ArrayList<>();
        for (Student s : Menu.students) {
            if (s.id == 1001) {
                results.add(s);
            }
        }

        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should handle search with special characters in name")
    void testSearchByName_SpecialCharacters() {
        RegularStudent specialStudent = new RegularStudent(1005, "O'Brien-Smith", 18, "test@email.com", "1234567890");
        Menu.students.add(specialStudent);

        String searchName = "o'brien";
        ArrayList<Student> results = new ArrayList<>();

        for (Student s : Menu.students) {
            if (s.name.toLowerCase().contains(searchName.toLowerCase())) {
                results.add(s);
            }
        }

        assertEquals(1, results.size());
        assertEquals("O'Brien-Smith", results.get(0).name);
    }

    @Test
    @DisplayName("Should handle students with identical names")
    void testSearchByName_DuplicateNames() {
        RegularStudent duplicate = new RegularStudent(1005, "John Doe", 20, "john2@email.com", "9998887777");
        Menu.students.add(duplicate);

        String searchName = "john doe";
        ArrayList<Student> results = new ArrayList<>();

        for (Student s : Menu.students) {
            if (s.name.toLowerCase().contains(searchName.toLowerCase())) {
                results.add(s);
            }
        }

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(s -> s.name.equals("John Doe")));
    }

    @Test
    @DisplayName("Should handle grade range with zero values")
    void testSearchByGradeRange_ZeroRange() {
        double minGrade = 0.0;
        double maxGrade = 0.0;
        ArrayList<Student> results = new ArrayList<>();

        for (Student s : Menu.students) {
            double avgGrade = s.getAverageGrade();
            if (avgGrade >= minGrade && avgGrade <= maxGrade && avgGrade > 0) {
                results.add(s);
            }
        }

        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should verify search results have correct data")
    void testSearchResults_DataIntegrity() {
        ArrayList<Student> results = new ArrayList<>();
        for (Student s : Menu.students) {
            if (s.id == 1001) {
                results.add(s);
                break;
            }
        }

        Student result = results.get(0);
        assertEquals(1001, result.id);
        assertEquals("John Doe", result.name);
        assertEquals(18, result.age);
        assertEquals("john@email.com", result.email);
        assertEquals("Regular", result.getType());
        assertTrue(result.getAverageGrade() > 0);
    }
}