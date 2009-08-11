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
                          MessageConstants.AK_EXTINGUISH,
                          MessageConstants.AK_SUBSCRIBE,
                          MessageConstants.AK_SPEAK,
                          MessageConstants.KA_HEAR_CHANNEL
        };
    }

    @Override
    public Message createMessage(int id, InputStream data) throws IOException {
        switch (id) {
        case MessageConstants.KA_HEAR_SAY:
            return new KAHearSay(data);
        case MessageConstants.KA_HEAR_TELL:
            return new KAHearTell(data);
        case MessageConstants.AK_MOVE:
            return new AKMove(data);
        case MessageConstants.AK_LOAD:
            return new AKLoad(data);
        case MessageConstants.AK_UNLOAD:
            return new AKUnload(data);
        case MessageConstants.AK_SAY:
            return new AKSay(data);
        case MessageConstants.AK_TELL:
            return new AKTell(data);
        case MessageConstants.AK_EXTINGUISH:
            return new AKExtinguish(data);
        case MessageConstants.AK_RESCUE:
            return new AKRescue(data);
        case MessageConstants.AK_CLEAR:
            return new AKClear(data);
        case MessageConstants.AK_SUBSCRIBE:
            return new AKSubscribe(data);
        case MessageConstants.AK_SPEAK:
            return new AKSpeak(data);
        case MessageConstants.KA_HEAR_CHANNEL:
            return new KAHearChannel(data);
        default:
            System.out.println("Unrecognised message ID: " + id);
            return null;
        }
    }
}