package traffic.object;

import rescuecore.RescueConstants;

public class FireStation extends Building {
  public FireStation(int id) { super(id); }
  public int type() { return RescueConstants.TYPE_FIRE_STATION; }
}
