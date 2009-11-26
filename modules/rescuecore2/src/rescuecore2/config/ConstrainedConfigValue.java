package rescuecore2.config;

/**
   Interface for specifying constraints on a config value.
*/
public interface ConstrainedConfigValue {
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
       Get a description of this constraint suitable for use in tooltips.
       @return A description of the constraint.
    */
    String getDescription();
}