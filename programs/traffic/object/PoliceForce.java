package traffic.object;

import rescuecore.RescueConstants;

public class PoliceForce extends Humanoid {
  public PoliceForce(int id) { super(id); }
  public int type() { return RescueConstants.TYPE_POLICE_FORCE; }
}
