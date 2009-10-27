package traffic.object;

import java.util.*;
import traffic.*;
import rescuecore.RescueConstants;

public abstract class PointObject extends MotionlessObject {
  public PointObject(int id) { super(id); }

  private int m_x;
  private int m_y;
  
  public int x() { return m_x; }
  public int y() { return m_y; }

  public void setX(int value) { m_x = value; }
  public void setY(int value) { m_y = value; }

  public void input(String property, int[] value) {
      if ("X".equals(property)) {
          setX(value[0]);
      }
      else if ("Y".equals(property)) {
          setY(value[0]);
      }
      else {
          super.input(property, value);
      }
  }

  public abstract HashSet links();

  // ---------------------------------------------------------------- Simulator
  private List m_outLanes;
  public List outLanes() {
    if (m_outLanes == null) {
      m_outLanes = new ArrayList();
      Iterator it = links().iterator();
      while (it.hasNext()) {
	MotionlessObject obj = (MotionlessObject) it.next();
	if (obj instanceof Road)
	  m_outLanes.addAll(((Road) obj).lanesFrom(this));
      }
      ((ArrayList) m_outLanes).trimToSize();
      m_outLanes = Collections.unmodifiableList(m_outLanes);
    }
    return m_outLanes;
  }

  private List m_inLanes;
  public List inLanes() {
    if (m_inLanes == null) {
      m_inLanes = new ArrayList();
      Iterator it = links().iterator();
      while (it.hasNext()) {
	MotionlessObject obj = (MotionlessObject) it.next();
	if (obj instanceof Road)
	  m_inLanes.addAll(((Road) obj).lanesTo(this));
      }
      ((ArrayList) m_inLanes).trimToSize();
      m_inLanes = Collections.unmodifiableList(m_inLanes);
    }
    return m_inLanes;
  }

  public boolean shouldStop(Lane from, Road to) {
    Lane lane = from.adjacentLaneOfSamePriorityRoadVia(this);
    return (lane == null  ||  lane.road != to);
  }
}
