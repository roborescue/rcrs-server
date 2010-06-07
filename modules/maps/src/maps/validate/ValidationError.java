package maps.validate;

/**
 * This class encapsulates a validation error. It contains the object id and an
 * error message.
 */
public class ValidationError {
    private int id;
    private String message;

    /**
     * Create a new ValidationError object.
     * @param id The id of the GMLObject containing the error.
     * @param message The error message.
     */
    public ValidationError(int id, String message) {
        this.id = id;
        this.message = message;
    }

    /**
     * Get the id of the object this error refers to.
     * @return The id of the GMLObject containing the error.
     */
    public int getId() {
        return id;
    }

    /**
     * Get the error message of this ValidationError.
     * @return The error message.
     */
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Error in object " + id + ": " + message;
    }
}
