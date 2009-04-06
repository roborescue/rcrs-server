package traffic.object;

import rescuecore.RescueConstants;

public class River extends Edge {
  public River(int id) { super(id); }
  public int type() { return RescueConstants.TYPE_RIVER; }
}
