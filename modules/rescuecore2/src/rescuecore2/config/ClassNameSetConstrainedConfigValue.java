package rescuecore2.config;

/**
   A config value constraint that requires the value to be a list of valid class names.
*/
public class ClassNameSetConstrainedConfigValue extends AbstractConstrainedConfigValue {
    private Class<?> required;

    /**
       Construct a ClassNameSetConstrainedConfigValue that does not require the values to be a particular class.
       @param key The key this constraint applies to.
    */
    public ClassNameSetConstrainedConfigValue(String key) {
        this(key, null);
    }

    /**
       Construct a ClassNameSetConstrainedConfigValue that requires the values to be a particular class.
       @param key The key this constraint applies to.
       @param required The required class.
    */
    public ClassNameSetConstrainedConfigValue(String key, Class<?> required) {
        super(key);
        this.required = required;
    }

    @Override
    public String getDescription() {
        if (required == null) {
            return "Must be a list of valid class names";
        }
        return "Must be a list of valid class names that extend " + required.getName();
    }

    @Override
    public boolean isValid(String value, Config config) {
        try {
            for (String next : config.getArrayValue(key)) {
                Class<?> c = Class.forName(next);
                if (required != null && !required.isAssignableFrom(c)) {
                    return false;
                }
            }
        }
        catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }
}