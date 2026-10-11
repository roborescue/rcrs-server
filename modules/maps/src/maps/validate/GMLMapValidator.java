package maps.validate;

import java.util.List;

import maps.Map;
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

    // Exit statuses of the command line tool
    private static final int EXIT_OK = 0;
    private static final int EXIT_ERRORS_FOUND = 1;
    private static final int EXIT_FAILURE = 2;

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
        System.exit(run(args));
    }

    // Validates the map given in args and returns the exit status
    private static int run(String[] args) {
        if (args.length != 1) {
            LOGGER.error("Usage: GMLMapValidator <map-file>");
            return EXIT_FAILURE;
        }
        String mapPath = args[0];

        try {
            Map readMap = MapReader.readMap(mapPath);

            // Only GML maps can be validated
            if (!(readMap instanceof GMLMap map)) {
                LOGGER.error("Not a GMLMap: {}", mapPath);
                return EXIT_FAILURE;
            }

            List<ValidationError> errors = validate(map);
            logResult(mapPath, errors);
            return errors.isEmpty() ? EXIT_OK : EXIT_ERRORS_FOUND;
        } catch (MapException e) {
            LOGGER.error("Failed to read the map: {}", args[0], e);
            return EXIT_FAILURE;
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
