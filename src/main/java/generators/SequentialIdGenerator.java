package generators;

/**
 * Single Responsibility Principle (SRP): Dedicated sequential ID generation
 * Thread-safe implementation for generating unique sequential IDs
 * Open/Closed Principle (OCP): Can be extended for UUID or other strategies
 */
public class SequentialIdGenerator implements IIdGenerator {
    private int counter;
    private final Object lock = new Object();

    public SequentialIdGenerator(int startingValue) {
        this.counter = startingValue;
    }

    @Override
    public int generateId() {
        synchronized (lock) {
            return counter++;
        }
    }

    @Override
    public void setStartingValue(int startValue) {
        synchronized (lock) {
            this.counter = startValue;
        }
    }

    @Override
    public int getCurrentValue() {
        synchronized (lock) {
            return counter;
        }
    }
}
