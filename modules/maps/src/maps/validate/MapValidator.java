package maps.validate;

import java.util.Collection;

import maps.Map;

/**
 * Interface for classes that can validate maps.
 *
 * @param <T>
 */
public interface MapValidator<T extends Map> {

    /**
     * Check if the given map is valid. If not, return a collection of errors.
     * @param map Map that should be checked.
     * @return Collection of errors.
     */
    Collection<ValidationError> validate(T map);
}
