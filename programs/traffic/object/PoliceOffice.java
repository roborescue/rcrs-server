package traffic.object;

import rescuecore.RescueConstants;

public class PoliceOffice extends Building {
  public PoliceOffice(int id) { super(id); }
  public int type() { return RescueConstants.TYPE_POLICE_OFFICE; }
}
