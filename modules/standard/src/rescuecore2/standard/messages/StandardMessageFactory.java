package rescuecore2.standard.messages;

import java.io.InputStream;
import java.io.IOException;

import rescuecore2.messages.Message;
import rescuecore2.messages.AbstractMessageFactory;

/**
   A factory for standard messages.
 */
public final class StandardMessageFactory extends AbstractMessageFactory<StandardMessageURN> {
    /** Singleton instance. */
    public static final StandardMessageFactory INSTANCE = new StandardMessageFactory();

    /**
       Singleton class: private constructor.
    */
    private StandardMessageFactory() {
        super(StandardMessageURN.class);
    }

    @Override
    public Message createMessage(StandardMessageURN urn, InputStream data) throws IOException {
        switch (urn) {
        case KA_HEAR_SAY:
            return new KAHearSay(data);
        case KA_HEAR_TELL:
            return new KAHearTell(data);
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
        case AK_SUBSCRIBE:
            return new AKSubscribe(data);
        case AK_SPEAK:
            return new AKSpeak(data);
        case KA_HEAR_CHANNEL:
            return new KAHearChannel(data);
        default:
            System.out.println("Unrecognised message urn: " + urn);
            return null;
        }
    }
}