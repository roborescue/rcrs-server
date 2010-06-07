package maps.validate;

/**
 * Exception that is raised when an error in a map is detected during
 * validation.
 *
 */
public class ValidationException extends Exception {
    private ValidationError error;

    /**
     * Create a new ValidationException.
     * @param id The id of the GMLObject containing the error.
     * @param message The error message.
     */
    public ValidationException(int id, String message) {
        super(message);
        error = new ValidationError(id, message);
    }

    /**
     * Get the underlying ValidationError for this exception.
     * @return The ValidationError causing this exception.
     */
    public ValidationError getError() {
        return error;
    }

}
