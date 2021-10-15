package rescuecore2.registry;

import java.util.Map;
import java.util.Set;
import java.io.InputStream;
import java.io.IOException;

import rescuecore2.messages.Message;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;

/**
   A message factory that filters urns that do not match a given set.
 */
public class FilterMessageFactory implements MessageFactory {
    private MessageFactory downstream;
    private Set<Integer> urns;
    private boolean inclusive;

    /**
       Construct a FilterMessageFactory.
       @param downstream The downstream message factory.
       @param urns The set of URNs.
       @param inclusive True if the set of URNs are allowed, false if they are forbidden.
    */
    public FilterMessageFactory(MessageFactory downstream, Set<Integer> urns, boolean inclusive) {
        this.downstream = downstream;
        this.urns = urns;
        this.inclusive = inclusive;
    }

    @Override
    public int[] getKnownMessageURNs() {
        return downstream.getKnownMessageURNs();
    }

    @Override
    public Message makeMessage(int urn, InputStream data) throws IOException {
        if (inclusive && !urns.contains(urn)) {
            return null;
        }
        if (!inclusive && urns.contains(urn)) {
            return null;
        }
        return downstream.makeMessage(urn, data);
    }
    
    @Override
    public Message makeMessage(int urn, MessageProto data) {
        if (inclusive && !urns.contains(urn)) {
            return null;
        }
        if (!inclusive && urns.contains(urn)) {
            return null;
        }
        return downstream.makeMessage(urn, data);
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
