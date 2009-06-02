package rescuecore2.messages.control;

import rescuecore2.messages.Control;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.IntComponent;

/**
   A message for connecting a viewer to the kernel.
 */
public class VKConnect extends AbstractMessage implements Control {
    private IntComponent version;

    /**
       An VKConnect with version 0.
     */
    public VKConnect() {
        super("VK_CONNECT", ControlMessageConstants.VK_CONNECT);
        version = new IntComponent("Version", 0);
        addMessageComponent(version);
    }

    /**
       Get the version number of this request.
       @return The version number.
     */
    public int getVersion() {
        return version.getValue();
    }
}