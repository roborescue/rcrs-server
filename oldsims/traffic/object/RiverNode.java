package traffic.object;

import rescuecore.RescueConstants;

public class RiverNode extends Vertex {
  public RiverNode(int id) { super(id); }
  public int type() { return RescueConstants.TYPE_RIVER_NODE; }
}
