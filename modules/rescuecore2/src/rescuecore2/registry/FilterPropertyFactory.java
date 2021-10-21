package rescuecore2.registry;

import java.util.Map;
import java.util.Set;

import rescuecore2.worldmodel.Property;

/**
   A property factory that filters urns that do not match a given set.
 */
public class FilterPropertyFactory implements PropertyFactory {
    private PropertyFactory downstream;
    private Set<Integer> urns;
    private boolean inclusive;

    /**
       Construct a FilterPropertyFactory.
       @param downstream The downstream property factory.
       @param urns The set of URNs.
       @param inclusive True if the set of URNs are allowed, false if they are forbidden.
    */
    public FilterPropertyFactory(PropertyFactory downstream, Set<Integer> urns, boolean inclusive) {
        this.downstream = downstream;
        this.urns = urns;
        this.inclusive = inclusive;
    }

    @Override
    public int[] getKnownURNs() {
        return downstream.getKnownURNs();
    }

    @Override
    public Property makeProperty(int urn) {
        if (inclusive && !urns.contains(urn)) {
            return null;
        }
        if (!inclusive && urns.contains(urn)) {
            return null;
        }
        return downstream.makeProperty(urn);
    }

    @Override
    public String getV1Equiv(int urnId) {
    	return downstream.getV1Equiv(urnId);
    }
    
    @Override
    public String getPrettyName(int urn) {
    	return downstream.getPrettyName(urn);
    }
}
