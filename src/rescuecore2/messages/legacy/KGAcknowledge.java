package rescuecore2.messages.legacy;

/**
   A message the kernel sends to the GIS in order to acknowledge that the connection has been completed.
 */
public class KGAcknowledge extends LegacyMessage {
    /**
       Construct a KGAcknowledge message.
     */
    public KGAcknowledge() {
	super(MessageType.KG_ACKNOWLEDGE);
    }
}