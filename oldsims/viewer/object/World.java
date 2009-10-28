package viewer.object;

import rescuecore.RescueConstants;

public class World extends VirtualObject {
  public World(int id) { super(id); }
  public int type() { return RescueConstants.TYPE_WORLD; }
}
