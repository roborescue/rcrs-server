package rescuecore2.standard.messages;

import java.io.IOException;
import java.io.InputStream;

import rescuecore2.log.Logger;
import rescuecore2.messages.Message;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;
import rescuecore2.registry.AbstractMessageFactory;

/**
 * A factory for standard messages.
 */
public final class StandardMessageFactory
		extends AbstractMessageFactory<StandardMessageURN> {

	/** Singleton instance. */
	public static final StandardMessageFactory INSTANCE = new StandardMessageFactory();

	/**
	 * Singleton class: private constructor.
	 */
	private StandardMessageFactory() {
		super(StandardMessageURN.class);
	}

	@Override
	public Message makeMessage(StandardMessageURN urn, InputStream data)
			throws IOException {
		switch (urn) {
		case AK_REST:
			return new AKRest(data);
		case AK_MOVE:
			return new AKMove(data);
		case AK_LOAD:
			return new AKLoad(data);
		case AK_UNLOAD:
			return new AKUnload(data);
		case AK_SAY:
			return new AKSay(data);
		case AK_TELL:
			return new AKTell(data);
		case AK_EXTINGUISH:
			return new AKExtinguish(data);
		case AK_RESCUE:
			return new AKRescue(data);
		case AK_CLEAR:
			return new AKClear(data);
		case AK_CLEAR_AREA:
			return new AKClearArea(data);
		case AK_SUBSCRIBE:
			return new AKSubscribe(data);
		case AK_SPEAK:
			return new AKSpeak(data);
		default:
			Logger.warn("Unrecognised message urn: " + urn);
			return null;
		}
	}

	@Override
	public Message makeMessage(StandardMessageURN urn, MessageProto proto) {
		switch (urn) {
		case AK_REST:
			return new AKRest(proto);
		case AK_MOVE:
			return new AKMove(proto);
		case AK_LOAD:
			return new AKLoad(proto);
		case AK_UNLOAD:
			return new AKUnload(proto);
		case AK_SAY:
			return new AKSay(proto);
		case AK_TELL:
			return new AKTell(proto);
		case AK_EXTINGUISH:
			return new AKExtinguish(proto);
		case AK_RESCUE:
			return new AKRescue(proto);
		case AK_CLEAR:
			return new AKClear(proto);
		case AK_CLEAR_AREA:
			return new AKClearArea(proto);
		case AK_SUBSCRIBE:
			return new AKSubscribe(proto);
		case AK_SPEAK:
			return new AKSpeak(proto);
		default:
			Logger.warn("Unrecognised message urn: " + urn);
			return null;
		}
	}
}
