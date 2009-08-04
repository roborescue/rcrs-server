package rescuecore2.messages.control;

import rescuecore2.messages.Control;
import rescuecore2.messages.AbstractMessage;

/**
   A message for acknowleding a connection to the GIS.
 */
public class KGAcknowledge extends AbstractMessage implements Control {
    /**
       A KGAcknowldge message.
     */
    public KGAcknowledge() {
        super("KG_ACKNOWLEDGE", ControlMessageConstants.KG_ACKNOWLEDGE);
    }
}