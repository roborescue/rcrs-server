package rescuecore2.version0.messages;

import rescuecore2.messages.Message;
import rescuecore2.messages.MessageFactory;

/**
   A factory for version 0 messages.
 */
public final class Version0MessageFactory implements MessageFactory {
    /** Singleton instance. */
    public static final Version0MessageFactory INSTANCE = new Version0MessageFactory();

    /**
       Singleton class: private constructor.
     */
    private Version0MessageFactory() {}

    @Override
    public Message createMessage(int id) {
        switch (id) {
        case MessageConstants.KG_CONNECT:
            return new KGConnect();
        case MessageConstants.KG_ACKNOWLEDGE:
            return new KGAcknowledge();
        case MessageConstants.GK_CONNECT_OK:
            return new GKConnectOK();
        case MessageConstants.GK_CONNECT_ERROR:
            return new GKConnectError();
        case MessageConstants.AK_CONNECT:
            return new AKConnect();
        case MessageConstants.AK_ACKNOWLEDGE:
            return new AKAcknowledge();
        case MessageConstants.KA_CONNECT_OK:
            return new KAConnectOK();
        case MessageConstants.KA_CONNECT_ERROR:
            return new KAConnectError();
        default:
            throw new IllegalArgumentException("Unrecognised message ID: " + id);
        }
    }
}