package rescuecore2.config;

import java.util.Set;
import java.util.Collections;

/**
   Abstract base class for value constraints.
*/
public abstract class AbstractValueConstraint implements ValueConstraint {
    /** The key this constraint refers to. */
    protected final String key;

    /**
       Construct an AbstractConstrainedConfigValue.
       @param key The key this constraint applies to.
    */
    protected AbstractValueConstraint(String key) {
        this.key = key;
    }

    @Override
    public final boolean isViolated(Config config) {
        String value = config.getValue(key, null);
        if (value == null) {
            return !undefinedIsValid();
        }
        return !isValid(value, config);
    }

    @Override
    public final String getKey() {
        return key;
    }

    @Override
    public final Set<String> getKeys() {
        return Collections.singleton(key);
    }

    @Override
    public boolean undefinedIsValid() {
        return false;
    }
}
