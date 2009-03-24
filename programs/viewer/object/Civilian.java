package viewer.object;

import rescuecore.RescueConstants;

public class Civilian extends Humanoid {
  public Civilian(int id) { super(id); }
  public int type() { return RescueConstants.TYPE_CIVILIAN; }
}
