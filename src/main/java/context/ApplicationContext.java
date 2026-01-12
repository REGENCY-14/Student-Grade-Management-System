package context;

import core.Student;
import core.Grade;
import manager.GradeManager;
import models.StudentService;
import scheduler.TaskScheduler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Application Context - Holds all shared application state and components
 * This class encapsulates all the static variables that were previously in Menu
 * Follows Single Responsibility Principle: stores state, nothing more
 * 
 * Uses Service Locator pattern with static instance for backward compatibility
 * while transitioning other classes to dependency injection
 */
public class ApplicationContext {
    // Service Locator - static instance for global access
    private static ApplicationContext instance;
    
    // Data storage
    private ArrayList<Student> students;
    private HashMap<String, Student> studentIndex;
    private ArrayList<Grade> grades;
    
    // Managers and services
    private GradeManager gradeManager;
    private StudentService studentService;
    private TaskScheduler taskScheduler;
    private Scanner scanner;
    
    // Counters
    private int studentIdCounter;
    private static final int INITIAL_STUDENT_ID = 1000;
    
    /**
     * Initialize the application context with default values
     * Also sets the static instance for Service Locator pattern
     */
    public ApplicationContext() {
        this.students = new ArrayList<>();
        this.studentIndex = new HashMap<>();
        this.grades = new ArrayList<>();
        this.gradeManager = new GradeManager();
        this.studentService = new StudentService(students, INITIAL_STUDENT_ID);
        this.studentIdCounter = INITIAL_STUDENT_ID;
        this.scanner = new Scanner(System.in);
        
        // Set static instance for Service Locator pattern
        ApplicationContext.instance = this;
    }
    
    /**
     * Get the global ApplicationContext instance (Service Locator)
     * Used by classes that don't have direct dependency injection
     */
    public static ApplicationContext getInstance() {
        if (instance == null) {
            instance = new ApplicationContext();
        }
        return instance;
    }
    
    // ========== Getters and Setters ==========
    
    public ArrayList<Student> getStudents() {
        return students;
    }
    
    public HashMap<String, Student> getStudentIndex() {
        return studentIndex;
    }
    
    public ArrayList<Grade> getGrades() {
        return grades;
    }
    
    public GradeManager getGradeManager() {
        return gradeManager;
    }
    
    public StudentService getStudentService() {
        return studentService;
    }
    
    public TaskScheduler getTaskScheduler() {
        return taskScheduler;
    }
    
    public void setTaskScheduler(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }
    
    public Scanner getScanner() {
        return scanner;
    }
    
    public int getStudentIdCounter() {
        return studentIdCounter;
    }
    
    public void setStudentIdCounter(int counter) {
        this.studentIdCounter = counter;
    }
    
    /**
     * Generate next student ID
     */
    public int generateStudentId() {
        return studentIdCounter++;
    }
    
    /**
     * Get a student by ID from the index (O(1) lookup)
     */
    public Student getStudentById(int id) {
        Student fromIndex = studentIndex.get(String.valueOf(id));
        if (fromIndex != null) {
            return fromIndex;
        }
        // Fallback: search in students list
        return students.stream()
                .filter(s -> s.getId() == id)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Add a student to the context
     */
    public void addStudent(Student student) {
        students.add(student);
        studentIndex.put(String.valueOf(student.getId()), student);
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        if (scanner != null) {
            scanner.close();
        }
        if (taskScheduler != null) {
            taskScheduler.shutdown();
        }
    }
}
