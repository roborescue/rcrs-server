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
    public int[] getKnownMessageTypeIDs() {
        return new int[] {MessageConstants.AK_CONNECT,
                          MessageConstants.AK_ACKNOWLEDGE,
                          MessageConstants.KA_CONNECT_OK,
                          MessageConstants.KA_CONNECT_ERROR,
                          MessageConstants.KA_SENSE,
                          MessageConstants.KA_HEAR_SAY,
                          MessageConstants.KA_HEAR_TELL,
                          MessageConstants.AK_MOVE,
                          MessageConstants.AK_LOAD,
                          MessageConstants.AK_UNLOAD,
                          MessageConstants.AK_SAY,
                          MessageConstants.AK_TELL,
                          MessageConstants.AK_RESCUE,
                          MessageConstants.AK_CLEAR,
                          MessageConstants.AK_EXTINGUISH
        };
    }

    @Override
    public Message createMessage(int id, InputStream data) throws IOException {
        Message result = null;
        switch (id) {
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
        case MessageConstants.KA_SENSE:
            result = new KASense();
            break;
        case MessageConstants.KA_HEAR_SAY:
            result = new KAHearSay();
            break;
        case MessageConstants.KA_HEAR_TELL:
            result = new KAHearTell();
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