package rescuecore2.version0.messages;

import rescuecore2.messages.AbstractMessage;

/**
   A message for acknowleding a connection to the kernel.
 */
public class VKAcknowledge extends AbstractMessage {
    /**
       VKAcknowledge message with an undefined ID component.
     */
    public VKAcknowledge() {
        super("VK_ACKNOWLEDGE", MessageConstants.VK_ACKNOWLEDGE);
    }
}