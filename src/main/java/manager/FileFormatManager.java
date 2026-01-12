package manager;

import core.Grade;
import core.Student;
import core.CoreSubject;
import core.ElectiveSubject;
import context.ApplicationContext;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import core.Subject;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Comprehensive file format manager supporting CSV, JSON, and Binary formats
 * with streaming, watching, and statistics
 */
public class FileFormatManager {

    private static final String BASE_DIR = "./imports";
    private static final String CSV_DIR = BASE_DIR + "/csv";
    private static final String JSON_DIR = BASE_DIR + "/json";
    private static final String BINARY_DIR = BASE_DIR + "/binary";
    
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static class FileStats {
        public String fileName;
        public String format;
        public long fileSize;
        public long readTime;
        public long writeTime;
        public int recordsProcessed;
        public int successCount;
        public int failureCount;
        
        @Override
        public String toString() {
            return String.format(
                "File: %s | Format: %s | Size: %s | Read: %dms | Write: %dms | Processed: %d | Success: %d | Failed: %d",
                fileName, format, formatBytes(fileSize), readTime, writeTime, recordsProcessed, successCount, failureCount
            );
        }
        
        private String formatBytes(long bytes) {
            if (bytes <= 0) return "0 B";
            final String[] units = new String[]{"B", "KB", "MB", "GB"};
            int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
            return String.format("%.2f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
        }
    }

    public FileFormatManager() {
        initializeDirectories();
    }

    /**
     * Initialize required directories for each format
     */
    private void initializeDirectories() {
        try {
            Files.createDirectories(Paths.get(CSV_DIR));
            Files.createDirectories(Paths.get(JSON_DIR));
            Files.createDirectories(Paths.get(BINARY_DIR));
            System.out.println("✓ File format directories initialized\n");
        } catch (IOException e) {
            System.err.println("Error initializing directories: " + e.getMessage());
        }
    }

    /**
     * Import grades from CSV with streaming support for large files
     */
    public FileStats importFromCSV(String filePath) {
        FileStats stats = new FileStats();
        stats.format = "CSV";
        
        try {
            Path path = resolvePath(filePath, CSV_DIR);
            
            if (!Files.exists(path)) {
                throw new FileNotFoundException("File not found: " + path);
            }

            stats.fileName = path.getFileName().toString();
            stats.fileSize = Files.size(path);
            
            long startTime = System.currentTimeMillis();

            try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stats.recordsProcessed++;
                    try {
                        processCSVLine(line);
                        stats.successCount++;
                    } catch (Exception e) {
                        stats.failureCount++;
                        System.out.println("  ⚠ Row " + stats.recordsProcessed + ": " + e.getMessage());
                    }
                }
            }

            stats.readTime = System.currentTimeMillis() - startTime;

        } catch (FileNotFoundException e) {
            System.out.println("❌ File Error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("❌ I/O Error reading CSV: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }

        return stats;
    }

    /**
     * Export grades to CSV format with streaming
     */
    public FileStats exportToCSV(ArrayList<Grade> grades, String fileName) {
        FileStats stats = new FileStats();
        stats.format = "CSV";
        stats.fileName = fileName + ".csv";
        
        try {
            Path path = Paths.get(CSV_DIR, fileName + ".csv");
            long startTime = System.currentTimeMillis();

            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                
                writer.write("StudentID,SubjectName,SubjectType,Grade\n");
                
                for (Grade grade : grades) {
                    String line = String.format("%d,%s,%s,%.2f\n",
                            grade.getStudentId(),
                            grade.getSubject().getSubjectName(),
                            grade.getSubject().getSubjectType(),
                            grade.getGrade());
                    writer.write(line);
                    stats.recordsProcessed++;
                }
            }

            stats.writeTime = System.currentTimeMillis() - startTime;
            stats.fileSize = Files.size(path);
            stats.successCount = stats.recordsProcessed;

        } catch (IOException e) {
            System.out.println("❌ I/O Error writing CSV: " + e.getMessage());
            stats.failureCount = stats.recordsProcessed;
        }

        return stats;
    }

    /**
     * Import grades from JSON format
     */
    public FileStats importFromJSON(String filePath) {
        FileStats stats = new FileStats();
        stats.format = "JSON";
        
        try {
            Path path = resolvePath(filePath, JSON_DIR);
            
            if (!Files.exists(path)) {
                throw new FileNotFoundException("File not found: " + path);
            }

            stats.fileName = path.getFileName().toString();
            stats.fileSize = Files.size(path);
            
            long startTime = System.currentTimeMillis();

            String jsonContent = Files.readString(path, StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            
            List<Map<String, Object>> grades = mapper.readValue(jsonContent,
                    mapper.getTypeFactory().constructCollectionType(List.class, Map.class));

            for (Map<String, Object> gradeMap : grades) {
                stats.recordsProcessed++;
                try {
                    processJSONGrade(gradeMap);
                    stats.successCount++;
                } catch (Exception e) {
                    stats.failureCount++;
                    System.out.println("  ⚠ Record " + stats.recordsProcessed + ": " + e.getMessage());
                }
            }

            stats.readTime = System.currentTimeMillis() - startTime;

        } catch (FileNotFoundException e) {
            System.out.println("❌ File Error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("❌ I/O Error reading JSON: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Error parsing JSON: " + e.getMessage());
        }

        return stats;
    }

    /**
     * Export grades to JSON format
     */
    public FileStats exportToJSON(ArrayList<Grade> grades, String fileName) {
        FileStats stats = new FileStats();
        stats.format = "JSON";
        stats.fileName = fileName + ".json";
        
        try {
            Path path = Paths.get(JSON_DIR, fileName + ".json");
            long startTime = System.currentTimeMillis();

            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> gradesList = new ArrayList<>();

            for (Grade grade : grades) {
                Map<String, Object> gradeMap = new LinkedHashMap<>();
                gradeMap.put("studentId", grade.getStudentId());
                gradeMap.put("subjectName", grade.getSubject().getSubjectName());
                gradeMap.put("subjectType", grade.getSubject().getSubjectType());
                gradeMap.put("grade", grade.getGrade());
                gradeMap.put("date", grade.getDate().toString());
                gradesList.add(gradeMap);
                stats.recordsProcessed++;
            }

            String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(gradesList);
            Files.writeString(path, jsonString, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            stats.writeTime = System.currentTimeMillis() - startTime;
            stats.fileSize = Files.size(path);
            stats.successCount = stats.recordsProcessed;

        } catch (IOException e) {
            System.out.println("❌ I/O Error writing JSON: " + e.getMessage());
            stats.failureCount = stats.recordsProcessed;
        }

        return stats;
    }

    /**
     * Import grades from Binary serialization format
     */
    public FileStats importFromBinary(String filePath) {
        FileStats stats = new FileStats();
        stats.format = "Binary";
        
        try {
            Path path = resolvePath(filePath, BINARY_DIR);
            
            if (!Files.exists(path)) {
                throw new FileNotFoundException("File not found: " + path);
            }

            stats.fileName = path.getFileName().toString();
            stats.fileSize = Files.size(path);
            
            long startTime = System.currentTimeMillis();

            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
                int count = ois.readInt();
                for (int i = 0; i < count; i++) {
                    try {
                        GradeData gradeData = (GradeData) ois.readObject();
                        processBinaryGrade(gradeData);
                        stats.successCount++;
                    } catch (Exception e) {
                        stats.failureCount++;
                        System.out.println("  ⚠ Record " + (i + 1) + ": " + e.getMessage());
                    }
                    stats.recordsProcessed++;
                }
            }

            stats.readTime = System.currentTimeMillis() - startTime;

        } catch (FileNotFoundException e) {
            System.out.println("❌ File Error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("❌ I/O Error reading Binary: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }

        return stats;
    }

    /**
     * Export grades to Binary serialization format
     */
    public FileStats exportToBinary(ArrayList<Grade> grades, String fileName) {
        FileStats stats = new FileStats();
        stats.format = "Binary";
        stats.fileName = fileName + ".bin";
        
        try {
            Path path = Paths.get(BINARY_DIR, fileName + ".bin");
            long startTime = System.currentTimeMillis();

            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(path,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
                
                oos.writeInt(grades.size());
                for (Grade grade : grades) {
                    GradeData gradeData = new GradeData(
                            grade.getStudentId(),
                            grade.getSubject().getSubjectName(),
                            grade.getSubject().getSubjectType(),
                            grade.getGrade()
                    );
                    oos.writeObject(gradeData);
                    stats.recordsProcessed++;
                }
            }

            stats.writeTime = System.currentTimeMillis() - startTime;
            stats.fileSize = Files.size(path);
            stats.successCount = stats.recordsProcessed;

        } catch (IOException e) {
            System.out.println("❌ I/O Error writing Binary: " + e.getMessage());
            stats.failureCount = stats.recordsProcessed;
        }

        return stats;
    }

    /**
     * Display format comparison statistics
     */
    public void displayFormatComparison(List<FileStats> statsList) {
        if (statsList.isEmpty()) {
            System.out.println("No file operations to compare.");
            return;
        }

        System.out.println("\n" + "=".repeat(150));
        System.out.println("FILE FORMAT COMPARISON");
        System.out.println("=".repeat(150));
        System.out.printf("%-10s %-15s %-15s %-15s %-10s %-10s %-15s %-15s\n",
                "Format", "File Size", "Read Time", "Write Time", "Processed", "Success", "Failure", "Efficiency");
        System.out.println("-".repeat(150));

        for (FileStats stats : statsList) {
            String efficiency = stats.readTime + stats.writeTime > 0 ?
                    String.format("%.2f rec/ms", (stats.successCount / (double)(stats.readTime + stats.writeTime))) :
                    "N/A";
            
            System.out.printf("%-10s %-15s %-15dms %-15dms %-10d %-10d %-15d %s\n",
                    stats.format,
                    formatBytes(stats.fileSize),
                    stats.readTime,
                    stats.writeTime,
                    stats.recordsProcessed,
                    stats.successCount,
                    stats.failureCount,
                    efficiency);
        }

        System.out.println("=".repeat(150) + "\n");
    }

    /**
     * List available files by format
     */
    public void listFilesByFormat(String format) {
        try {
            String dir = switch (format.toLowerCase()) {
                case "csv" -> CSV_DIR;
                case "json" -> JSON_DIR;
                case "binary", "bin" -> BINARY_DIR;
                default -> null;
            };

            if (dir == null) {
                System.out.println("Unknown format: " + format);
                return;
            }

            Path dirPath = Paths.get(dir);
            if (!Files.exists(dirPath)) {
                System.out.println("Directory not found: " + dir);
                return;
            }

            System.out.println("\n" + "=".repeat(100));
            System.out.println("FILES IN " + format.toUpperCase() + " FORMAT - " + dir);
            System.out.println("=".repeat(100));
            System.out.printf("%-40s %-20s %-30s\n", "File Name", "Size", "Last Modified");
            System.out.println("-".repeat(100));

            try (Stream<Path> paths = Files.list(dirPath)) {
                paths.filter(Files::isRegularFile)
                        .forEach(path -> {
                            try {
                                String fileName = path.getFileName().toString();
                                long size = Files.size(path);
                                LocalDateTime modified = LocalDateTime.ofInstant(
                                        Instant.ofEpochMilli(Files.getLastModifiedTime(path).toMillis()),
                                        ZoneId.systemDefault()
                                );
                                System.out.printf("%-40s %-20s %-30s\n",
                                        fileName,
                                        formatBytes(size),
                                        modified.format(TIME_FORMAT));
                            } catch (IOException e) {
                                System.out.println("Error reading file info: " + e.getMessage());
                            }
                        });
            }

            System.out.println("=".repeat(100) + "\n");

        } catch (IOException e) {
            System.out.println("❌ Error listing files: " + e.getMessage());
        }
    }

    /**
     * Watch directory for new import files
     */
    public void watchDirectoryForImports(String format, long durationSeconds) {
        try {
            String dir = switch (format.toLowerCase()) {
                case "csv" -> CSV_DIR;
                case "json" -> JSON_DIR;
                case "binary", "bin" -> BINARY_DIR;
                default -> null;
            };

            if (dir == null) {
                System.out.println("Unknown format: " + format);
                return;
            }

            Path dirPath = Paths.get(dir);
            
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                dirPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

                System.out.println("👁 Watching " + dir + " for new files (duration: " + durationSeconds + "s)...\n");

                long endTime = System.currentTimeMillis() + (durationSeconds * 1000);
                boolean timeout = false;

                while (!timeout) {
                    WatchKey key = watchService.poll(1, java.util.concurrent.TimeUnit.SECONDS);
                    
                    if (key != null) {
                        for (WatchEvent<?> event : key.pollEvents()) {
                            if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                                Path newFile = (Path) event.context();
                                System.out.println("✓ New file detected: " + newFile);
                                System.out.println("  Path: " + dirPath.resolve(newFile));
                                System.out.println("  Size: " + formatBytes(Files.size(dirPath.resolve(newFile))));
                                System.out.println("  Detected at: " + LocalDateTime.now().format(TIME_FORMAT));
                            }
                        }
                        key.reset();
                    }

                    if (System.currentTimeMillis() > endTime) {
                        timeout = true;
                    }
                }
            }
            System.out.println("\n✓ Watch service stopped\n");

        } catch (IOException e) {
            System.out.println("❌ Watch Service Error: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("❌ Watch Service interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Resolve file path supporting relative and absolute paths
     */
    private Path resolvePath(String filePath, String defaultDir) throws FileNotFoundException {
        Path path = Paths.get(filePath);
        
        // If absolute path, use it directly
        if (path.isAbsolute()) {
            return path;
        }
        
        // Try relative to default directory
        Path defaultPath = Paths.get(defaultDir, filePath);
        if (Files.exists(defaultPath)) {
            return defaultPath;
        }
        
        // Try current directory
        if (Files.exists(path)) {
            return path;
        }
        
        throw new FileNotFoundException("File not found in any expected location: " + filePath);
    }

    private void processCSVLine(String line) throws Exception {
        String[] parts = line.split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid column count (expected 4)");
        }

        int studentId = Integer.parseInt(parts[0].trim());
        String subjectName = parts[1].trim();
        String subjectType = parts[2].trim();
        double grade = Double.parseDouble(parts[3].trim());

        if (grade < 0 || grade > 100) {
            throw new IllegalArgumentException("Grade out of range: " + grade);
        }

        Student student = findStudentById(studentId);
        if (student == null) {
            throw new IllegalArgumentException("Student not found: " + studentId);
        }

        Subject subject = subjectType.equalsIgnoreCase("Core") ?
                new CoreSubject(subjectName, "C-" + subjectName.substring(0, Math.min(3, subjectName.length())).toUpperCase()) :
                new ElectiveSubject(subjectName, "E-" + subjectName.substring(0, Math.min(3, subjectName.length())).toUpperCase());

        Grade newGrade = new Grade(studentId, subject, grade);
        ApplicationContext.getInstance().getGradeManager().addGrade(newGrade);
    }

    private void processJSONGrade(Map<String, Object> gradeMap) throws Exception {
        int studentId = ((Number) gradeMap.get("studentId")).intValue();
        String subjectName = (String) gradeMap.get("subjectName");
        String subjectType = (String) gradeMap.get("subjectType");
        double grade = ((Number) gradeMap.get("grade")).doubleValue();

        if (grade < 0 || grade > 100) {
            throw new IllegalArgumentException("Grade out of range: " + grade);
        }

        Student student = findStudentById(studentId);
        if (student == null) {
            throw new IllegalArgumentException("Student not found: " + studentId);
        }

        Subject subject = subjectType.equalsIgnoreCase("Core") ?
                new CoreSubject(subjectName, "C-" + subjectName.substring(0, Math.min(3, subjectName.length())).toUpperCase()) :
                new ElectiveSubject(subjectName, "E-" + subjectName.substring(0, Math.min(3, subjectName.length())).toUpperCase());

        Grade newGrade = new Grade(studentId, subject, grade);
        ApplicationContext.getInstance().getGradeManager().addGrade(newGrade);
    }

    private void processBinaryGrade(GradeData gradeData) throws Exception {
        Student student = findStudentById(gradeData.studentId);
        if (student == null) {
            throw new IllegalArgumentException("Student not found: " + gradeData.studentId);
        }

        Subject subject = gradeData.subjectType.equalsIgnoreCase("Core") ?
                new CoreSubject(gradeData.subjectName, "C-" + gradeData.subjectName.substring(0, Math.min(3, gradeData.subjectName.length())).toUpperCase()) :
                new ElectiveSubject(gradeData.subjectName, "E-" + gradeData.subjectName.substring(0, Math.min(3, gradeData.subjectName.length())).toUpperCase());

        Grade newGrade = new Grade(gradeData.studentId, subject, gradeData.grade);
        ApplicationContext.getInstance().getGradeManager().addGrade(newGrade);
    }

    /**
     * Optimized student lookup.
     * Big-O: O(1) average using HashMap-backed index (ApplicationContext.getInstance().getStudentIndex()).
     */
    private Student findStudentById(int studentId) {
        return ApplicationContext.getInstance().getStudentIndex().get(String.valueOf(studentId));
    }

    private String formatBytes(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.2f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    /**
     * Serializable class for binary format
     */
    public static class GradeData implements Serializable {
        public int studentId;
        public String subjectName;
        public String subjectType;
        public double grade;

        public GradeData(int studentId, String subjectName, String subjectType, double grade) {
            this.studentId = studentId;
            this.subjectName = subjectName;
            this.subjectType = subjectType;
            this.grade = grade;
        }
    }
}

