package maps.validate;

/**
 * This class encapsulates a validation error. It contains the object id and an
 * error message.
 */
public class ValidationError {
    private int id;
    private ValidationErrorKind kind;
    private String message;

    /**
     * Creates a new error of the given kind.
     *
     * @param id the id of the map object containing the error
     * @param kind the kind of the error
     * @param message the error message
     */
    public ValidationError(int id, ValidationErrorKind kind, String message) {
        this.id = id;
        this.kind = kind;
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
     * Returns the kind of this error.
     *
     * @return the kind of the error
     */
    public ValidationErrorKind getKind() {
        return kind;
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
