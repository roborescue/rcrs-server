package viewer.object;

import rescuecore.RescueConstants;

public class FireBrigade extends Humanoid {
  public FireBrigade(int id) { super(id); }
  public int type() { return RescueConstants.TYPE_FIRE_BRIGADE; }
}
