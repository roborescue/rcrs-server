package rescuecore2.config;

/**
   Abstract base class for constrained config values.
*/
public abstract class AbstractConstrainedConfigValue implements ConstrainedConfigValue {
    /** The key this constraint refers to. */
    protected final String key;

    /**
       Construct an AbstractConstrainedConfigValue.
       @param key The key this constraint applies to.
    */
    protected AbstractConstrainedConfigValue(String key) {
        this.key = key;
    }

    @Override
    public final String getKey() {
        return key;
    }
}