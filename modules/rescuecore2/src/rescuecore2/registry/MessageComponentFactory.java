package rescuecore2.registry;

/**
   Factory class for creating messages.
 */
public interface MessageComponentFactory {
    /**
       Get all message urns understood by this factory.
       @return All message urns.
    */
    int[] getKnownMessageURNs();
    String getV1Equiv(int urnId);
	String getPrettyName(int urn);

}
