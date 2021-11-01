package rescuecore2.registry;

import java.util.Set;

public abstract class AbstractFilterFactory<T extends Factory> implements Factory {

  protected T downstream;
  private Set<Integer> urns;
  private boolean inclusive;

  public AbstractFilterFactory(T downstream, Set<Integer> urns, boolean inclusive) {
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
  public String getURNStr(int urnId) {
    return downstream.getURNStr(urnId);
  }

  @Override
  public String getPrettyName(int urn) {
    return downstream.getPrettyName(urn);
  }
}