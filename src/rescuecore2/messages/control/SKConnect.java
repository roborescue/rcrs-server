package rescuecore2.messages.control;

import rescuecore2.messages.Control;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.IntComponent;

/**
   A message for connecting a simulator to the kernel.
 */
public class SKConnect extends AbstractMessage implements Control {
    private IntComponent version;

    /**
       An SKConnect with version 0.
     */
    public SKConnect() {
        super("SK_CONNECT", ControlMessageConstants.SK_CONNECT);
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