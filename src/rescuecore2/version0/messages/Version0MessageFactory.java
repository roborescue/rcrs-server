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
        case MessageConstants.SK_CONNECT:
            return new SKConnect();
        case MessageConstants.SK_ACKNOWLEDGE:
            return new SKAcknowledge();
        case MessageConstants.SK_UPDATE:
            return new SKUpdate();
        case MessageConstants.KS_CONNECT_OK:
            return new KSConnectOK();
        case MessageConstants.KS_CONNECT_ERROR:
            return new KSConnectError();
        case MessageConstants.VK_CONNECT:
            return new VKConnect();
        case MessageConstants.VK_ACKNOWLEDGE:
            return new VKAcknowledge();
        case MessageConstants.KV_CONNECT_OK:
            return new KVConnectOK();
        case MessageConstants.KV_CONNECT_ERROR:
            return new KVConnectError();
        case MessageConstants.AK_CONNECT:
            return new AKConnect();
        case MessageConstants.AK_ACKNOWLEDGE:
            return new AKAcknowledge();
        case MessageConstants.KA_CONNECT_OK:
            return new KAConnectOK();
        case MessageConstants.KA_CONNECT_ERROR:
            return new KAConnectError();
        case MessageConstants.AK_MOVE:
            return new AKMove();
        default:
            System.out.println("Unrecognised message ID: " + id);
            return null;
       }
    }
}