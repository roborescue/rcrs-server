package maps.validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import maps.MapException;
import maps.MapReader;
import maps.gml.GMLMap;

/**
 * Load a map and check it for errors.
 *
 */
public final class GMLMapValidator {
    private static List<MapValidator<GMLMap>> validators;

    static {
        validators = new ArrayList<MapValidator<GMLMap>>();

        validators.add(new GMLConnectivityValidator());
        validators.add(new GMLShapeValidator());
        validators.add(new GMLTraversabilityValidator());
    }

    private GMLMapValidator() {}

    /**
     * Returns a list of default MapValidators to use for GML maps.
       @return List of default MapValidators.
     */
    public static List<MapValidator<GMLMap>> getDefaultValidators() {
        return new ArrayList<MapValidator<GMLMap>>(validators);
    }

    /**
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        try {
            GMLMap map = (GMLMap)MapReader.readMap(args[0]);
            boolean hasErrors = true;
            for (MapValidator<GMLMap> val : validators) {
                Collection<ValidationError> errors = val.validate(map);
                for (ValidationError e : errors) {
                    System.err.println(e);
                    hasErrors = true;
                }
            }
            if (!hasErrors) {
                System.out.println("No errors have been found.");
            }

        }
        catch (MapException e) {
            e.printStackTrace();
        }

    }

}
