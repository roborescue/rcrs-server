package rescuecore2.messages.legacy;

/**
   A message the kernel sends to the GIS in order to connect.
 */
public class KGConnect extends LegacyMessage {
    private int version;

    /**
       Construct a KGConnect message with a particular version number. Should probably be zero.
       @param version The version parameter for the message.
     */
    public KGConnect(int version) {
	super(MessageType.KG_CONNECT);
        this.version = version;
    }

    /**
       Get the version number of this request.
       @return The version number.
     */
    public int getVersion() {
        return version;
    }
}