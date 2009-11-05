package viewer.object;

import java.util.*;
import rescuecore.RescueConstants;

public abstract class MovingObject extends RealObject {
  public MovingObject(int id) { super(id); }

  private int m_position;
  private int m_positionExtra;
  //private int m_direction;
  private int[] m_positionHistory = null;
  private int m_timeUpdatedPositionHistory;

  public int x() {
    if (position() instanceof Edge) {
      Edge e = (Edge) position();
      return e.head().x() + (e.tail().x() - e.head().x()) * m_positionExtra / e.length();
    }
    return position().x();
  }

  public int y() {
    if (position() instanceof Edge) {
      Edge e = (Edge) position();
      return e.head().y() + (e.tail().y() - e.head().y()) * m_positionExtra / e.length();
    }
    return position().y();
  }

  public MotionlessObject motionlessPosition() { return position().motionlessPosition(); }

  public RealObject position() { return (RealObject) WORLD.get(m_position); }
  public int positionExtra() { return m_positionExtra; }
  //public int direction() { return m_direction; }
  public int[] positionHistory() {
    if (m_timeUpdatedPositionHistory != WORLD.time())
      m_positionHistory = null;  // It is necessary to update a history by myself for AtsumiTrafficSimulator.
    return m_positionHistory;
  }

  public void setPosition(int value) { m_position = value; }
  public void setPositionExtra(int value) { m_positionExtra = value; }
  //public void setDirection(int value) { m_direction = value; }
  public void setPositionHistory(int[] value) {
    m_positionHistory = value;
    m_timeUpdatedPositionHistory = WORLD.time();
  }

  public void input(String property, int[] value) {
      if ("POSITION".equals(property)) {
          setPosition(value[0]);
      }
      if ("POSITION_EXTRA".equals(property)) {
          setPositionExtra(value[0]);
      }
      if ("POSITION_HISTORY".equals(property)) {
          setPositionHistory(value);
      }
      super.input(property, value);
  }

  // ------------------------------------------------------------------- Viewer
  private List m_route = new ArrayList();
  private double m_index;
  private int m_lastMovingTime;

  public void prepareForAnimation(int pos, int posEx, int[] route) {
    m_route.clear();
    m_index = 0;
    m_lastMovingTime = WORLD.time();
    setRoute(pos, route);
  }

  // NOTE: The positionHistory property be not complete perhaps,
  //       but this viewer does not view states of agents very strictly.
  private void setRoute(int pos, int[] route) {
    if (motionlessPosition() instanceof Road)
      m_route.add(motionlessPosition());

    for (int i = 0;  i < route.length;  i ++) {
      Node nd0 = (Node) WORLD.get(route[i]);
      if (i == route.length - 1) {
          if (nd0 != null) {
              m_route.add(nd0);
          }
          break;
      }
      Node nd1 = (Node) WORLD.get(route[i + 1]);
      Road rd = (nd0 == null || nd1 == null) ? null : nd0.roadBetweenThisAnd(nd1);
      if (rd == null)
	// Probably the agent turned, but it's impossible to dicide turn from/to any roads.
	continue;
      m_route.add(nd0);
      m_route.add(rd);
    }

    RealObject end = WORLD.get(pos);
    if ((end instanceof Road  ||  end instanceof Building)  // allow to add a building
	&& end.id != m_position)
      m_route.add(end);
  }

  public void move() {
    if (m_lastMovingTime != WORLD.time()
	|| m_route.isEmpty()
	|| Math.floor(m_index) > m_route.size() - 1)
      return;
    
    m_position = ((MotionlessObject) m_route.get((int) m_index)).id;
    if (position() instanceof Road) {
      m_positionExtra = ((Road) position()).length() / 2;
    }
    m_index += m_route.size() / (double) VIEWER.numOfExposuresEachCycle();
  }
}
