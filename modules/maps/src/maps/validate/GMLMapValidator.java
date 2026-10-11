package maps.validate;

import java.util.List;

import maps.MapException;
import maps.MapReader;
import maps.gml.GMLMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads a map and checks it for errors.
 */
public final class GMLMapValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(GMLMapValidator.class);

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
     * Validates the given map with the default validators.
     *
     * @param map the map to validate
     * @return the errors found, in the order the validators ran
     */
    public static List<ValidationError> validate(GMLMap map) {
        // Collect the errors of all validators in execution order
        return VALIDATORS.stream()
                .flatMap(validator -> validator.validate(map).stream())
                .toList();
    }

    /**
     * Validates the GML map given as the first argument and prints the errors.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            GMLMap map = (GMLMap)MapReader.readMap(args[0]);
            List<ValidationError> errors = validate(map);
            logResult(args[0], errors);
        }
        catch (MapException e) {
            LOGGER.error("Failed to read the map: {}", args[0], e);
        }
    }

    // Logs each error and a summary line in the TAG key=value format
    private static void logResult(String mapName, List<ValidationError> errors) {
        errors.forEach(error -> LOGGER.info(
                "VALIDATION_ERROR map={} kind={} id={} message={}",
                mapName, error.getKind(), error.getId(), error.getMessage()));
        LOGGER.info("VALIDATION_SUMMARY map={} errors{}", mapName, errors.size());
    }
}
