package rescuecore2.config;

import java.util.Set;

/**
   Interface for specifying constraints on a Config.
*/
public interface ConfigConstraint {
    /**
       Check if this constraint has been violated.
       @param config The Config object.
       @return True iff the constraint has been violated.
    */
    boolean isViolated(Config config);

    /**
       Get a description of this constraint suitable for use in tooltips.
       @return A description of the constraint.
    */
    String getDescription();

    /**
       Get the set of keys this constriant applies to.
       @return The set of relevant keys.
     */
    Set<String> getKeys();
}
