package validators;

/**
 * Interface Segregation Principle (ISP): Segregated validation interfaces
 * This ensures only necessary validation methods are exposed
 */
public interface IValidator<T> {
    /**
     * Validates the given object
     * @param obj object to validate
     * @return true if valid, false otherwise
     */
    boolean validate(T obj);
    
    /**
     * Gets the error message from last validation
     * @return error message
     */
    String getErrorMessage();
}
