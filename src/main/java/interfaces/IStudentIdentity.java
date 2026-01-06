package interfaces;

/**
 * Interface Segregation Principle (ISP): Segregated interface for basic student information
 * Clients needing only identification info depend only on this interface
 * No unnecessary methods exposed
 */
public interface IStudentIdentity {
    /**
     * Get student ID
     * @return unique student ID
     */
    int getId();

    /**
     * Get student name
     * @return student name
     */
    String getName();

    /**
     * Get student age
     * @return student age
     */
    int getAge();

    /**
     * Get student email
     * @return student email
     */
    String getEmail();

    /**
     * Get student phone
     * @return student phone number
     */
    String getPhone();
}
