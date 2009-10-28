package viewer.object;

import java.util.*;
import rescuecore.RescueConstants;

public class Node extends Vertex {
  public Node(int id) { super(id); }
  public int type() { return RescueConstants.TYPE_NODE; }

  public Road roadBetweenThisAnd(Node another) {
    Road result = null;
    Iterator it = edges().iterator();
    while (it.hasNext()) {
      MotionlessObject e = (MotionlessObject) it.next();
      if (e instanceof Road) {
	Road rd = (Road) e;
	if (   (rd.head() == this  &&  rd.tail() == another)
	    || (rd.tail() == this  &&  rd.head() == another))
	  return rd;
      }
    }
    return null;
  }
}
