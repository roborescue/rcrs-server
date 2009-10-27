package traffic.object;

import java.util.*;
import rescuecore.RescueConstants;

public class Node extends Vertex {
  public Node(int id) { super(id); }
  public String type() { return "NODE"; }
}
