package rescuecore2.registry;

import java.util.Set;

public abstract class FilterFactory<T extends Factory> implements Factory {

	protected T downstream;
	private Set<Integer> urns;
	private boolean inclusive;

	public FilterFactory(T downstream, Set<Integer> urns, boolean inclusive) {
		this.downstream = downstream;
		this.urns = urns;
		this.inclusive = inclusive;
	}

	public boolean isValidUrn(int urn) {
		if (inclusive && !urns.contains(urn)) {
			return false;
		}
		if (!inclusive && urns.contains(urn)) {
			return false;
		}
		return true;
	}

	@Override
	public int[] getKnownURNs() {
		return downstream.getKnownURNs();
	}

	@Override
	public String getV1Equiv(int urnId) {
		return downstream.getV1Equiv(urnId);
	}

	@Override
	public String getPrettyName(int urn) {
		return downstream.getPrettyName(urn);
	}

}
