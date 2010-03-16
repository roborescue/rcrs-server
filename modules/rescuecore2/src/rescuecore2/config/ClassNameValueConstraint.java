package rescuecore2.config;

/**
   A config value constraint that requires the value to be a valid class name.
*/
public class ClassNameValueConstraint extends AbstractValueConstraint {
    private Class<?> required;

    /**
       Construct a ClassNameConstrainedConfigValue that does not require the value to be a particular class.
       @param key The key this constraint applies to.
    */
    public ClassNameValueConstraint(String key) {
        this(key, null);
    }

    /**
       Construct a ClassNameConstrainedConfigValue that requires the value to be a particular class.
       @param key The key this constraint applies to.
       @param required The required class.
    */
    public ClassNameValueConstraint(String key, Class<?> required) {
        super(key);
        this.required = required;
    }

    @Override
    public String getDescription() {
        if (required == null) {
            return "Must be a valid class name";
        }
        return "Must be a valid class name that extends " + required.getName();
    }

    @Override
    public boolean isValid(String value, Config config) {
        try {
            Class<?> c = Class.forName(value);
            if (required != null) {
                return required.isAssignableFrom(c);
            }
            return true;
        }
        catch (ClassNotFoundException e) {
            return false;
        }
    }
}
