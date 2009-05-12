package rescuecore2.version0.messages;

import java.io.InputStream;
import java.io.IOException;

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
    public Message createMessage(int id, InputStream data) throws IOException {
        Message result = null;
        switch (id) {
        case MessageConstants.KG_CONNECT:
            result = new KGConnect();
            break;
        case MessageConstants.KG_ACKNOWLEDGE:
            result = new KGAcknowledge();
            break;
        case MessageConstants.GK_CONNECT_OK:
            result = new GKConnectOK();
            break;
        case MessageConstants.GK_CONNECT_ERROR:
            result = new GKConnectError();
            break;
        case MessageConstants.SK_CONNECT:
            result = new SKConnect();
            break;
        case MessageConstants.SK_ACKNOWLEDGE:
            result = new SKAcknowledge();
            break;
        case MessageConstants.SK_UPDATE:
            result = new SKUpdate();
            break;
        case MessageConstants.KS_CONNECT_OK:
            result = new KSConnectOK();
            break;
        case MessageConstants.KS_CONNECT_ERROR:
            result = new KSConnectError();
            break;
        case MessageConstants.VK_CONNECT:
            result = new VKConnect();
            break;
        case MessageConstants.VK_ACKNOWLEDGE:
            result = new VKAcknowledge();
            break;
        case MessageConstants.KV_CONNECT_OK:
            result = new KVConnectOK();
            break;
        case MessageConstants.KV_CONNECT_ERROR:
            result = new KVConnectError();
            break;
        case MessageConstants.AK_CONNECT:
            result = new AKConnect();
            break;
        case MessageConstants.AK_ACKNOWLEDGE:
            result = new AKAcknowledge();
            break;
        case MessageConstants.KA_CONNECT_OK:
            result = new KAConnectOK();
            break;
        case MessageConstants.KA_CONNECT_ERROR:
            result = new KAConnectError();
            break;
        case MessageConstants.AK_MOVE:
            result = new AKMove();
            break;
        case MessageConstants.AK_LOAD:
            result = new AKLoad();
            break;
        case MessageConstants.AK_UNLOAD:
            result = new AKUnload();
            break;
        case MessageConstants.AK_SAY:
            result = new AKSay();
            break;
        case MessageConstants.AK_TELL:
            result = new AKTell();
            break;
        case MessageConstants.AK_EXTINGUISH:
            result = new AKExtinguish();
            break;
        case MessageConstants.AK_RESCUE:
            result = new AKRescue();
            break;
        case MessageConstants.AK_CLEAR:
            result = new AKClear();
            break;
        default:
            System.out.println("Unrecognised message ID: " + id);
            return null;
        }
        result.read(data);
        return result;
    }
}