package rescuecore2.messages.control;

import rescuecore2.messages.Control;
import rescuecore2.messages.AbstractMessage;

/**
   A message for acknowleding a connection to the kernel.
 */
public class VKAcknowledge extends AbstractMessage implements Control {
    /**
       VKAcknowledge message with an undefined ID component.
     */
    public VKAcknowledge() {
        super("VK_ACKNOWLEDGE", ControlMessageConstants.VK_ACKNOWLEDGE);
    }
}