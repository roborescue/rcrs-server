package rescuecore2.registry;

public interface Factory {
	/**
	 * Get all message urns understood by this factory.
	 * 
	 * @return All message urns.
	 */
	int[] getKnownURNs();

	String getV1Equiv(int urnId);

	String getPrettyName(int urnId);
}
