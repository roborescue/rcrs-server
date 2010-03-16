package rescuecore2.config;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
   A config value constraint that requires the value to be one of a discrete set of values.
*/
public class DiscreteValueConstraint extends AbstractValueConstraint {
    private Set<String> allowed;

    /**
       Construct a DiscreteConstrainedConfigValue.
       @param key The key this constraint applies to.
       @param allowed The set of allowed values.
    */
    public DiscreteValueConstraint(String key, Set<String> allowed) {
        super(key);
        this.allowed = new HashSet<String>(allowed);
    }

    @Override
    public String getDescription() {
        StringBuilder result = new StringBuilder();
        result.append("Must be one of: ");
        for (Iterator<String> it = allowed.iterator(); it.hasNext();) {
            result.append(it.next());
            if (it.hasNext()) {
                result.append(", ");
            }
        }
        return result.toString();
    }

    @Override
    public boolean isValid(String value, Config config) {
        return allowed.contains(value);
    }
}
