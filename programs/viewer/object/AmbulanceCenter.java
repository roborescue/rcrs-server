package viewer.object;

import rescuecore.RescueConstants;

public class AmbulanceCenter extends Building {
  public AmbulanceCenter(int id) { super(id); }
  public int type() { return RescueConstants.TYPE_AMBULANCE_CENTER; }
}
