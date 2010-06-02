package rescuecore2.registry;

import java.util.Set;
import java.io.InputStream;
import java.io.IOException;

import rescuecore2.messages.Message;

/**
   A message factory that filters urns that do not match a given set.
 */
public class FilterMessageFactory implements MessageFactory {
    private MessageFactory downstream;
    private Set<String> urns;
    private boolean inclusive;

    /**
       Construct a FilterMessageFactory.
       @param downstream The downstream message factory.
       @param urns The set of URNs.
       @param inclusive True if the set of URNs are allowed, false if they are forbidden.
    */
    public FilterMessageFactory(MessageFactory downstream, Set<String> urns, boolean inclusive) {
        this.downstream = downstream;
        this.urns = urns;
        this.inclusive = inclusive;
    }

    @Override
    public String[] getKnownMessageURNs() {
        return downstream.getKnownMessageURNs();
    }

    @Override
    public Message makeMessage(String urn, InputStream data) throws IOException {
        if (inclusive && !urns.contains(urn)) {
            return null;
        }
        if (!inclusive && urns.contains(urn)) {
            return null;
        }
        return downstream.makeMessage(urn, data);
    }
}
