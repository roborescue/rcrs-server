package viewer.object;

import rescuecore.RescueConstants;

public class AmbulanceTeam extends Humanoid {
  public AmbulanceTeam(int id) { super(id); }
  public int type() { return RescueConstants.TYPE_AMBULANCE_TEAM; }
}
