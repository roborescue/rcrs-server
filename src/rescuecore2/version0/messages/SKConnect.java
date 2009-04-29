package rescuecore2.version0.messages;

import rescuecore2.messages.AbstractMessage;

/**
   A message for connecting a simulator to the kernel.
 */
public class SKConnect extends AbstractMessage {
    private IntComponent version;

    /**
       An SKConnect with version 0.
     */
    public SKConnect() {
        super("SK_CONNECT", MessageConstants.SK_CONNECT);
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