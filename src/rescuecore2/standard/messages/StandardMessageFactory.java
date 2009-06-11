package rescuecore2.standard.messages;

import java.io.InputStream;
import java.io.IOException;

import rescuecore2.messages.Message;
import rescuecore2.messages.MessageFactory;

/**
   A factory for standard messages.
 */
public final class StandardMessageFactory implements MessageFactory {
    /** Singleton instance. */
    public static final StandardMessageFactory INSTANCE = new StandardMessageFactory();

    /**
       Singleton class: private constructor.
    */
    private StandardMessageFactory() {}

    @Override
    public int[] getKnownMessageTypeIDs() {
        return new int[] {MessageConstants.KA_HEAR_SAY,
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