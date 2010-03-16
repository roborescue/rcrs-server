package rescuecore2.config;

/**
   Interface for specifying constraints on a config value.
*/
public interface ValueConstraint extends ConfigConstraint {
    /**
       Get the key this restraint refers to.
       @return The config key.
    */
    String getKey();

    /**
       Check if a value is valid.
       @param value The value to check.
       @param config The Config object.
       @return True iff the value is valid.
    */
    boolean isValid(String value, Config config);

    /**
       Find out if an undefined value is counted as valid or invalid.
       @return True if an undefined value should be treated as valid, false if invalid.
    */
    boolean undefinedIsValid();
}
