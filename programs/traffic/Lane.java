package traffic;

import java.util.*;
import traffic.object.*;

public class Lane implements Constants {
  public final Road road;
  public final PointObject forward;
  public final PointObject back;
  public final int nth;
  public final int priority;
  private final HashSet m_mvObjOnSet = new HashSet();
  private final Set m_unmodifiableMvObjOnSet = Collections.unmodifiableSet(m_mvObjOnSet);

  public final int dx;
  public final int dy;

  /** constructs a Lane which is a nth th lane toward the specified forward on the specified road.
   *  @param nth is a position from outside.
   */
  public Lane(Road road, PointObject forward, int nth) {
    this.road = road;
    this.forward = forward;
    this.back = (forward == road.head()) ? road.tail() : road.head();
    this.nth = nth;
    this.priority = (forward == road.head()) ? road.linesToHead() : road.linesToTail();

    int numLines = road.linesToHead() + road.linesToTail();
    double n = - numLines / 2d + 0.5
      + ((forward == road.head()) ? nth : (numLines - 1 - nth));
    if (!DRIVING_DIRECTION_IS_LEFT)
      n = - n;
    dx = (int) (road.laneDx() * n);
    dy = (int) (road.laneDy() * n);
  }

  public boolean isBlocked() { return nth < road.blockedLines(); }

  public void addMvObjOn(MovingObject mv)    { m_mvObjOnSet.add(mv); }
  public void removeMvObjOn(MovingObject mv) { m_mvObjOnSet.remove(mv); }
  public Set mvObjOnSet() { return m_unmodifiableMvObjOnSet; }

  private Lane m_adjacentLaneOfSamePriorityRoadViaHead;
  private Lane m_adjacentLaneOfSamePriorityRoadViaTail;

  public void setAdjacentLaneOfSamePriorityRoad(Lane laneViaHead, Lane laneViaTail) {
    m_adjacentLaneOfSamePriorityRoadViaHead = laneViaHead;
    m_adjacentLaneOfSamePriorityRoadViaTail = laneViaTail;
  }

  public Lane adjacentLaneOfSamePriorityRoadVia(PointObject via) {
    return (via == road.head())
      ? m_adjacentLaneOfSamePriorityRoadViaHead
      : m_adjacentLaneOfSamePriorityRoadViaTail;
  }

  public String toString() { return "(b" + back.id + ")r" + road.id + "(f" + forward.id + ")[" + road.length() + "]@" + nth; }
}
