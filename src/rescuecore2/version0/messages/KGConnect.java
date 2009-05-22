package rescuecore2.version0.messages;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.IntComponent;

/**
   A message for connecting to the GIS.
 */
public class KGConnect extends AbstractMessage {
    private IntComponent version;

    /**
       A KGConnect message. The version number will be zero.
     */
    public KGConnect() {
        super("KG_CONNECT", MessageConstants.KG_CONNECT);
        version = new IntComponent("Version", 0);
        addMessageComponent(version);
    }
}