package rescuecore2.config;

/**
   An integer constraint on a config value.
*/
public class IntegerValueConstraint extends AbstractValueConstraint {
    private int min;
    private int max;

    /**
       Construct an IntegerValueConstraint that has no minimum or maximum.
       @param key The key this constraint applies to.
    */
    public IntegerValueConstraint(String key) {
        this(key, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    /**
       Construct an IntegerValueConstraint that has a particular range.
       @param key The key this constraint applies to.
       @param min The minimum value of the config entry.
       @param max The maximum value of the config entry.
    */
    public IntegerValueConstraint(String key, int min, int max) {
        super(key);
        this.min = min;
        this.max = max;
    }

    @Override
    public String getDescription() {
        if (min == Integer.MIN_VALUE && max == Integer.MAX_VALUE) {
            return "Must be an integer";
        }
        if (min == Integer.MIN_VALUE) {
            return "Must be an integer less than or equal to " + max;
        }
        if (max == Integer.MAX_VALUE) {
            return "Must be an integer greater than or equal to " + min;
        }
        return "Must be an integer between " + min + " and " + max + " inclusive";
    }

    @Override
    public boolean isValid(String value, Config config) {
        try {
            int i = Integer.parseInt(value);
            return (i >= min && i <= max);
        }
        catch (NumberFormatException e) {
            return false;
        }
    }
}
