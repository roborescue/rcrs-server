package rescuecore2.registry;

import java.util.Set;

import rescuecore2.worldmodel.Property;

/**
   A property factory that filters urns that do not match a given set.
 */
public class FilterPropertyFactory implements PropertyFactory {
    private PropertyFactory downstream;
    private Set<String> urns;
    private boolean inclusive;

    /**
       Construct a FilterPropertyFactory.
       @param downstream The downstream property factory.
       @param urns The set of URNs.
       @param inclusive True if the set of URNs are allowed, false if they are forbidden.
    */
    public FilterPropertyFactory(PropertyFactory downstream, Set<String> urns, boolean inclusive) {
        this.downstream = downstream;
        this.urns = urns;
        this.inclusive = inclusive;
    }

    @Override
    public String[] getKnownPropertyURNs() {
        return downstream.getKnownPropertyURNs();
    }

    @Override
    public Property makeProperty(String urn) {
        if (inclusive && !urns.contains(urn)) {
            return null;
        }
        if (!inclusive && urns.contains(urn)) {
            return null;
        }
        return downstream.makeProperty(urn);
    }
}
