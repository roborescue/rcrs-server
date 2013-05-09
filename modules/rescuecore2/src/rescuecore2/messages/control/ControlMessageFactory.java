package rescuecore2.messages.control;

import java.io.InputStream;
import java.io.IOException;

import rescuecore2.messages.Message;
import rescuecore2.registry.AbstractMessageFactory;
import rescuecore2.log.Logger;

/**
 * A factory for control messages.
 */
public final class ControlMessageFactory extends
		AbstractMessageFactory<ControlMessageURN> {
	/** Singleton instance. */
	public static final ControlMessageFactory INSTANCE = new ControlMessageFactory();

	private ControlMessageFactory() {
		super(ControlMessageURN.class);
	}

	@Override
	public Message makeMessage(ControlMessageURN urn, InputStream data)
			throws IOException {
		switch (urn) {
		case KG_CONNECT:
			return new KGConnect(data);
		case KG_ACKNOWLEDGE:
			return new KGAcknowledge(data);
		case GK_CONNECT_OK:
			return new GKConnectOK(data);
		case GK_CONNECT_ERROR:
			return new GKConnectError(data);
		case SK_CONNECT:
			return new SKConnect(data);
		case SK_ACKNOWLEDGE:
			return new SKAcknowledge(data);
		case SK_UPDATE:
			return new SKUpdate(data);
		case KS_CONNECT_OK:
			return new KSConnectOK(data);
		case KS_CONNECT_ERROR:
			return new KSConnectError(data);
		case KS_AFTERSHOCKS_INFO:
			return new KSAfterShocksInfo(data);
		case KS_UPDATE:
			return new KSUpdate(data);
		case KS_COMMANDS:
			return new KSCommands(data);
		case VK_CONNECT:
			return new VKConnect(data);
		case VK_ACKNOWLEDGE:
			return new VKAcknowledge(data);
		case KV_CONNECT_OK:
			return new KVConnectOK(data);
		case KV_CONNECT_ERROR:
			return new KVConnectError(data);
		case KV_TIMESTEP:
			return new KVTimestep(data);
		case AK_CONNECT:
			return new AKConnect(data);
		case AK_ACKNOWLEDGE:
			return new AKAcknowledge(data);
		case KA_CONNECT_OK:
			return new KAConnectOK(data);
		case KA_CONNECT_ERROR:
			return new KAConnectError(data);
		case KA_SENSE:
			return new KASense(data);
		case SHUTDOWN:
			return new Shutdown(data);
		case ENTITY_ID_REQUEST:
			return new EntityIDRequest(data);
		case ENTITY_ID_RESPONSE:
			return new EntityIDResponse(data);
		default:
			Logger.warn("Unrecognised message urn: " + urn);
			return null;
		}
	}
}
