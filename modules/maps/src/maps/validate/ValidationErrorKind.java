package maps.validate;

/**
 * Categories of errors found by map validators.
 */
public enum ValidationErrorKind {
    // Connectivity
    REACHES_NON_TRAVERSABLE_SHAPE,
    NOT_FULLY_CONNECTED,
    NEIGHBOUR_NOT_FOUND,
    SELF_CONNECTED,
    NOT_REFLEXIVE,
    EDGE_NOT_SHARED,

    // Shape
    SHAPE_OVERLAP,
    INVALID_SHAPE,
    CENTROID_OUTSIDE,
    ZERO_LENGTH_EDGE,
    BROKEN_OUTLINE,
    DEGENERATE_SHAPE,

    // Traversability
    NO_LINE_OF_SIGHT,
    EDGE_TOO_NARROW,
    UNREACHABLE_EDGE,

    // Temporary value until every error is migrated
    UNCLASSIFIED
}
