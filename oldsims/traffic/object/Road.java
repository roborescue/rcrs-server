package traffic.object;

import traffic.*;
import java.util.*;

public class Road extends Edge {
  public Road(int id) { super(id); }

  private int m_width;
  private int m_block;
  private int m_linesToHead;
  private int m_linesToTail;

  public int width() { return m_width; }
  public int block() { return m_block; }
  public int linesToHead() { return m_linesToHead; }
  public int linesToTail() { return m_linesToTail; }

  public int aliveLinesToHead() {
    int result = linesToHead() - blockedLines();
    return ((result >= 0) ? result : 0);
  }
  public int aliveLinesToTail() {
    int result = linesToTail() - blockedLines();
    return ((result >= 0) ? result : 0);
  }

  public int blockedLines() {
    double blockWidth = ((double) block()) / 2d;
    double linesBlockedRate = blockWidth / lineWidth();
    return (int) Math.floor(linesBlockedRate + 0.5d);
  }

  public double lineWidth() { return ((double) width()) / ((double) linesToHead() + linesToTail()); }

  public int aliveLinesTo(PointObject to)
    { return (to == head()) ? aliveLinesToHead() : aliveLinesToTail(); }
  public int linesTo(PointObject to)
    { return (to == head()) ? linesToHead() : linesToTail(); }

  public void setWidth(int value) { m_width = value; }
  public void setBlock(int value) { m_block = value; }
  public void setLinesToHead(int value) { m_linesToHead = value; }
  public void setLinesToTail(int value) { m_linesToTail = value; }

  // ---------------------------------------------------------------- Simulator
  private List m_lanesToHead;
  private List m_lanesToTail;

  public  List lanesTo(PointObject to) {
    //if(ASSERT)Util.myassert(to == head()  ||  to == tail(), "wrong to", to.id);
    if(ASSERT)Util.myassert(to == head()  ||  to == tail(), "wrong to: " + to.id + " (h:" + head().id + ", t:" + tail().id + ") of Road:" + id);
    return (to == head()) ? lanesToHead() : lanesToTail();
  }
  public  List lanesFrom(PointObject from) {
    //if(ASSERT)Util.myassert(from == head()  ||  from == tail(), "wrong from", from.id);
    if(ASSERT)Util.myassert(from == head()  ||  from == tail(), "wrong from: " + from.id + " (h:" + head().id + ", t:" + tail().id + ") of Road:" + id);
    return (from == tail()) ? lanesToHead() : lanesToTail();
  }

  public List lanesToHead() {
    if (m_lanesToHead == null)
      m_lanesToHead = lanesTo(head(), m_linesToHead);
    return m_lanesToHead;
  }
  public List lanesToTail() {
    if (m_lanesToTail == null)
      m_lanesToTail = lanesTo(tail(), m_linesToTail);
    return m_lanesToTail;
  }

  private List lanesTo(PointObject forward, int lines) {
    ArrayList lanes = new ArrayList(lines);
    for (int i = 0;  i < lines;  i ++)
      lanes.add(new Lane(this, forward, i));
    return Collections.unmodifiableList(lanes);
  }

  public void setAdjacentLanesOfSamePriorityRoad() {
    setAdjacentLanesOfSamePriorityRoad(head());
    setAdjacentLanesOfSamePriorityRoad(tail());
  }

  public void setAdjacentLanesOfSamePriorityRoad(PointObject forward) {
    List lanesViaHead = adjacentLanesOfSamePriorityRoadVia(forward, head());
    List lanesViaTail = adjacentLanesOfSamePriorityRoadVia(forward, tail());
    Iterator it = lanesTo(forward).iterator();
    while (it.hasNext()) {
      Lane ln = (Lane) it.next();
      ln.setAdjacentLaneOfSamePriorityRoad((Lane) lanesViaHead.get(ln.nth), (Lane) lanesViaTail.get(ln.nth));
    }
  }

  private List adjacentLanesOfSamePriorityRoadVia(PointObject forward, PointObject via) {
    Road rd = samePriorityRoadVia(forward, via);
    if (rd == null)
      return DUMMY_LANES;
    return (via == forward  ^  via == rd.head())
      ? rd.lanesToHead()
      : rd.lanesToTail();
  }

  private Road samePriorityRoadVia(PointObject forward, PointObject via) {
    int priority = (forward == head()) ? linesToHead() : linesToTail();
    Road candidate = null;
    Iterator it = via.links().iterator();
    while (it.hasNext()) {
      Object obj = it.next();
      if (!(obj instanceof Road)  ||  obj == this)
	continue;
      Road rd = (Road) obj;
      int p = (via == forward  ^  via == rd.head())
	? rd.linesToHead()
	: rd.linesToTail();
      if (p == priority)
	if (candidate == null) candidate = rd;
	else                   return null;
    }
    return (candidate == null)
      ? null
      : candidate;
  }

  private static final List DUMMY_LANES = new NullList();
  private static class NullList extends AbstractList {
    public Object get(int index) { return null; }
    public int size() { return -1; }
  }

  // --------------------------------------------------------------- for Viewer
  private double m_laneDx = Double.POSITIVE_INFINITY;
  private double m_laneDy = Double.POSITIVE_INFINITY;
  public double laneDx() {
    if (m_laneDx == Double.POSITIVE_INFINITY)
      setLaneD();
    return m_laneDx;
  }
  public double laneDy() {
    if (m_laneDy == Double.POSITIVE_INFINITY)
      setLaneD();
    return m_laneDy;
  }
  private void setLaneD() {
    PointObject h = head(), t = tail();
    double a = Math.atan2(h.y() - t.y(), h.x() - t.x()) - Math.PI / 2;
    double linewidth = lineWidth();
    m_laneDx = linewidth * Math.cos(a);
    m_laneDy = linewidth * Math.sin(a);
  }
}
