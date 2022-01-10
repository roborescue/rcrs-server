package rescuecore2.registry;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import rescuecore2.messages.Message;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;

/**
 * A message factory that filters urns that do not match a given set.
 */
public class FilterMessageFactory extends AbstractFilterFactory<MessageFactory>
		implements MessageFactory {

	/**
	 * Construct a FilterMessageFactory.
	 * 
	 * @param downstream The downstream message factory.
	 * @param urns       The set of URNs.
	 * @param inclusive  True if the set of URNs are allowed, false if they are
	 *                   forbidden.
	 */
	public FilterMessageFactory(MessageFactory downstream, Set<Integer> urns,
			boolean inclusive) {
		super(downstream, urns, inclusive);
	}

	@Override
	public Message makeMessage(int urn, InputStream data) throws IOException {
		if (!isValidUrn(urn))
			return null;

		return downstream.makeMessage(urn, data);
	}

	@Override
	public Message makeMessage(int urn, MessageProto data) {
		if (!isValidUrn(urn))
			return null;

		return downstream.makeMessage(urn, data);
	}

}
