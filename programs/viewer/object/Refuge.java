package viewer.object;

import rescuecore.RescueConstants;

public class Refuge extends Building {
  public Refuge(int id) { super(id); }
  public int type() { return RescueConstants.TYPE_REFUGE; }
}
