package rescuecore2.version0.messages;

import rescuecore2.messages.AbstractMessage;

/**
   A message for acknowleding a connection to the GIS.
 */
public class KGAcknowledge extends AbstractMessage {
    public KGAcknowledge() {
        super("KG_ACKNOWLEDGE", MessageConstants.KG_ACKNOWLEDGE);
    }
}