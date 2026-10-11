package maps.validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import maps.MapException;
import maps.MapReader;
import maps.gml.GMLMap;

/**
 * Loads a map and checks it for errors.
 */
public final class GMLMapValidator {
    // Validators applied to every GML map, in the order they run
    private static final List<MapValidator<GMLMap>> VALIDATORS = List.of(
            new GMLConnectivityValidator(),
            new GMLShapeValidator(),
            new GMLTraversabilityValidator());

    private GMLMapValidator() {}

    /**
     * Returns the default validators for GML maps.
     *
     * @return an unmodifiable list of validators
     */
    public static List<MapValidator<GMLMap>> getDefaultValidators() {
        return VALIDATORS;
    }

    /**
     * Validates the GML map given as the first argument and prints the errors.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            GMLMap map = (GMLMap)MapReader.readMap(args[0]);

            // Collect the errors of all validators in execution order
            List<ValidationError> errors = VALIDATORS.stream()
                    .flatMap(validator -> validator.validate(map).stream())
                    .toList();

            // Report success and stop when nothing was found
            if (errors.isEmpty()) {
                System.out.println("No errors have been found.");
                return;
            }
            errors.forEach(System.out::println);
        }
        catch (MapException e) {
            e.printStackTrace();
        }
    }
}
