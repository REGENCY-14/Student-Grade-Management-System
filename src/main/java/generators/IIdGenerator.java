package generators;

/**
 * Single Responsibility Principle (SRP): Dedicated ID generation
 * Encapsulates all ID generation logic in one place
 * Open/Closed Principle (OCP): Can be extended for different ID strategies
 */
public interface IIdGenerator {
    /**
     * Generates the next ID
     * @return generated ID
     */
    int generateId();

    /**
     * Sets the starting point for ID generation
     * @param startValue starting ID value
     */
    void setStartingValue(int startValue);

    /**
     * Gets the current counter value
     * @return current counter
     */
    int getCurrentValue();
}
