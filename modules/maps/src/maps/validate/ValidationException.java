package maps.validate;

/**
 * Exception that is raised when an error in a map is detected during
 * validation.
 *
 */
public class ValidationException extends Exception {
    private ValidationError error;

    /**
     * Creates a new exception for an error of the given kind.
     *
     * @param id the id of the map object containing the error
     * @param kind the kind of the error
     * @param message the error message
     */
    public ValidationException(int id, ValidationErrorKind kind, String message) {
        super(message);
        error = new ValidationError(id, kind, message);
    }

    /**
     * Get the underlying ValidationError for this exception.
     * @return The ValidationError causing this exception.
     */
    public ValidationError getError() {
        return error;
    }

}
