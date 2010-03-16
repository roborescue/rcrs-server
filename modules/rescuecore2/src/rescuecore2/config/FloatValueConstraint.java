package rescuecore2.config;

/**
   A floating-point number constraint on a config value.
*/
public class FloatValueConstraint extends AbstractValueConstraint {
    private double min;
    private double max;

    /**
       Construct a FloatConstrainedConfigValue that has no minimum or maximum.
       @param key The key this constraint applies to.
    */
    public FloatValueConstraint(String key) {
        this(key, Double.NaN, Double.NaN);
    }

    /**
       Construct a FloatConstrainedConfigValue that has a particular range.
       @param key The key this constraint applies to.
       @param min The minimum value of the config entry.
       @param max The maximum value of the config entry.
    */
    public FloatValueConstraint(String key, double min, double max) {
        super(key);
        this.min = min;
        this.max = max;
    }

    @Override
    public String getDescription() {
        boolean minSpecified = !Double.isNaN(min) && !Double.isInfinite(min);
        boolean maxSpecified = !Double.isNaN(max) && !Double.isInfinite(max);
        if (!minSpecified && !maxSpecified) {
            return "Must be a number";
        }
        if (!minSpecified) {
            return "Must be a number less than or equal to " + max;
        }
        if (!maxSpecified) {
            return "Must be a number greater than or equal to " + min;
        }
        return "Must be a number between " + min + " and " + max + " inclusive";
    }

    @Override
    public boolean isValid(String value, Config config) {
        try {
            double d = Double.parseDouble(value);
            boolean minSpecified = !Double.isNaN(min) && !Double.isInfinite(min);
            boolean maxSpecified = !Double.isNaN(max) && !Double.isInfinite(max);
            return !((minSpecified && d < min) || (maxSpecified && d > max));
        }
        catch (NumberFormatException e) {
            return false;
        }
    }
}
