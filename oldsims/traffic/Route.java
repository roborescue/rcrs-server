package traffic;

import java.util.*;
import traffic.object.*;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

public class Route {
    private static final Log LOG = LogFactory.getLog(Route.class);
  private ArrayList m_route;
  private int m_indexOfDestination;

  public Route(int initialCapacity) { m_route = new ArrayList(initialCapacity); }

  public void add(MotionlessObject ml) { m_route.add(ml); }
  public void add(int index, MotionlessObject ml) { m_route.add(index, ml); }
  public MotionlessObject get(int index) { return (MotionlessObject) m_route.get(index); }
  public int size() { return m_route.size(); }
  public boolean isEmpty() { return m_route.isEmpty(); }
  public Iterator iterator() { return m_route.iterator(); }
  public ListIterator listIterator() { return m_route.listIterator(); }
  public ListIterator listIterator(int index) { return m_route.listIterator(index); }
  public int indexOf(Object obj) { return m_route.indexOf(obj); }
  public boolean contains(Object obj) { return m_route.contains(obj); }
  public void remove() { m_route.remove(size() - 1); }
  public List subList(int fromIndex, int toIndex) { return m_route.subList(fromIndex, toIndex); }

  public int indexOf(MotionlessObject ml, int from) {
    for (int i = from;  i < size();  i ++)
      if (m_route.get(i) == ml)
	return i;
    return -1;
  }

  public int indexOf(Lane lane, int from) { return indexOf(lane, from, size()); }
  public int indexOf(Lane lane, int from, int to) {
    int result = -1;
    int r = lane.road.id;
    int h = lane.road.head().id;
    int t = lane.road.tail().id;
    for (int i = from;  i < to;  i ++) {
      int m = get(i).id;
      if (m == r  ||  m == h  ||  m == t)
        result = (i <= indexOfDestination()) ? i : indexOfDestination();
      else
	if (result != -1)
	  return result;
    }
    return result;
  }

  public void setIndexOfDestination() { m_indexOfDestination = size() - 1; }
  public int indexOfDestination() { return m_indexOfDestination; }
  public MotionlessObject destination() { return get(m_indexOfDestination); }

  public String toString() {
    String result = "Route[numObj=" + size() + "]:";
    Iterator it = iterator();
    while (it.hasNext())
      result += " " + ((MotionlessObject) it.next()).id;
    return result;
  }

  public static Route singleObjRoute(MotionlessObject origin) {
    Route route = new Route(1);
    route.add(origin);
    return route;
  }

  public boolean checkValidity(MovingObject agent) {
    if (isEmpty())
      return printError(agent, "The submited route consists of no object.");
    if (!(get(0) instanceof MotionlessObject))
      return printError(agent, "Each object of a route must be a MotionlessObject.");
    if (!complementRouteSpecOfAtsumiSimulator(agent))
      return printError(agent, "The origin of submited route is not adjacent with the current position (ID:" + agent.motionlessPosition().id +
			").  Probably this agent delayed submiting a route.");
    MotionlessObject previous = (MotionlessObject) get(0);
    ListIterator lit = listIterator(1);
    while (lit.hasNext()) {
      Object obj = lit.next();
      if (!(obj instanceof MotionlessObject))
	return printError(agent, "Each object of a route must be a MotionlessObject.");
      if (!previous.isAdjacentTo((MotionlessObject) obj))
	return printError(agent, "A route must be connected.");
      if (obj instanceof Building  &&  lit.hasNext())
	return printError(agent, "A Building must be only origin or destination.");
      previous = (MotionlessObject) obj;
    }
    return true;
  }

  private boolean printError(MovingObject mv, String reason) {
    if (Constants.PRINT_REASON_WHY_AGENT_COMMAND_WAS_NOT_EXECUTED)
      LOG.debug("[Wrong AK_MOVE Route] time:" + Constants.WORLD.time() + ", agentID:" + mv.id + "\n  " + reason);
    return false;
  }

  public boolean complementRouteSpecOfAtsumiSimulator(MovingObject agent) {
    MotionlessObject mlpos = agent.motionlessPosition();
    MotionlessObject origin = (MotionlessObject) get(0);
    if (origin == mlpos)                        {                                       return true; }
    if (mlpos.isAdjacentTo(origin))             { add(0, mlpos);                        return true; }
    if (mlpos instanceof Building  &&  origin instanceof Road) {
      HashSet entrances = ((Building) mlpos).entrances();
      Road road = (Road) origin;
      if (entrances.contains(road.head()))      { add(0, mlpos);  add(1, road.head());  return true; }
      if (entrances.contains(road.tail()))      { add(0, mlpos);  add(1, road.tail());  return true; }
    } else if (mlpos instanceof Road) {
      Road road = (Road) mlpos;
      if (road.head().links().contains(origin)) { add(0, mlpos);  add(1, road.head());  return true; }
      if (road.tail().links().contains(origin)) { add(0, mlpos);  add(1, road.tail());  return true; }
    }
    return false;
  }
}
