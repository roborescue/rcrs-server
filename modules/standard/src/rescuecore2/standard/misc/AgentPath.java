package rescuecore2.standard.misc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardWorldModel;

/**
 * A class for encapsulating the actual movement path an agent took during a
 * timestep.
 */
public abstract class AgentPath {

  /**
   * Compute the path an agent took. This will read the positionHistory property
   * and generate a path.
   *
   * @param human
   *   The agent.
   * @param world
   *   The world model.
   *
   * @return The computed Path, or null if the agent didn't move.
   */
  public static AgentPath computePath(Human human, StandardWorldModel world) {
    if (human == null) {
      throw new IllegalArgumentException("Agent must not be null");
    }
    if (!human.isPositionDefined()) {
      throw new IllegalArgumentException("Agent has an undefined position");
    }
    if (!human.isPositionHistoryDefined()) {
      // Agent didn't move.
      return null;
    }
    int[] history = human.getPositionHistory();
    if (history.length > 2) {
      return new CoordinatePath(history);
    }
    // Agent didn't move far enough: only one piece of history.
    return null;
  }


  /**
   * Get the coordinates of a point along this path.
   *
   * @param d
   *   The distance along the path in the range [0..1].
   *
   * @return The coordinates of the point along the path.
   */
  public abstract Pair<Integer, Integer> getPointOnPath(double d);

  /**
   * Get the length of this path.
   *
   * @return The length of the path.
   */
  public abstract double getLength();

  private static class CompositePath extends AgentPath {

    private List<AgentPath> parts;

    CompositePath() {
      parts = new ArrayList<AgentPath>();
    }


    void addPath(AgentPath p) {
      parts.add(p);
    }


    @Override
    public double getLength() {
      double d = 0;
      for (AgentPath next : parts) {
        d += next.getLength();
      }
      return d;
    }


    @Override
    public Pair<Integer, Integer> getPointOnPath(double d) {
      double length = getLength();
      double point = d * length;
      // Find the right part
      AgentPath result = null;
      for (AgentPath next : parts) {
        double nextLength = next.getLength();
        if (nextLength > point) {
          result = next;
          break;
        }
        point -= nextLength;
      }
      if (result == null) {
        // Fell off the end, probably because of numerical issues
        return parts.get(parts.size() - 1).getPointOnPath(1.0);
      }
      double p = point / result.getLength();
      return result.getPointOnPath(p);
    }


    @Override
    public String toString() {
      StringBuilder result = new StringBuilder();
      for (Iterator<AgentPath> it = parts.iterator(); it.hasNext();) {
        result.append(it.next());
        if (it.hasNext()) {
          result.append(", ");
        }
      }
      return result.toString();
    }
  }

  private abstract static class AbstractPath extends AgentPath {

    private Pair<Integer, Integer> start;
    private Pair<Integer, Integer> end;
    private double length;
    private String description;

    protected void setStart(int sX, int sY) {
      start = new Pair<Integer, Integer>(sX, sY);
    }


    protected void setEnd(int eX, int eY) {
      end = new Pair<Integer, Integer>(eX, eY);
    }


    protected void setStart(Pair<Integer, Integer> s) {
      start = s;
    }


    protected void setEnd(Pair<Integer, Integer> e) {
      end = e;
    }


    protected void setLength(double l) {
      length = l;
    }


    protected void computeLength() {
      int dx = end.first() - start.first();
      int dy = end.second() - start.second();
      length = Math.hypot(dx, dy);
    }


    protected void setDescription(String d) {
      description = d;
    }


    @Override
    public double getLength() {
      return length;
    }


    @Override
    public Pair<Integer, Integer> getPointOnPath(double d) {
      double dx = end.first() - start.first();
      double dy = end.second() - start.second();
      int x = start.first() + (int) (d * dx);
      int y = start.second() + (int) (d * dy);
      return new Pair<Integer, Integer>(x, y);
    }


    @Override
    public String toString() {
      return description;
    }
  }

  private static class CoordinatePath extends CompositePath {

    CoordinatePath(int[] history) {
      int fromX = history[0];
      int fromY = history[1];
      for (int i = 2; i < history.length; i += 2) {
        int toX = history[i];
        int toY = history[i + 1];
        addPath(new CoordinatePathSegment(fromX, fromY, toX, toY));
        fromX = toX;
        fromY = toY;
      }
    }
  }

  private static class CoordinatePathSegment extends AbstractPath {

    CoordinatePathSegment(int fromX, int fromY, int toX, int toY) {
      setStart(fromX, fromY);
      setEnd(toX, toY);
      setDescription(
          "From " + fromX + ", " + fromY + " to " + toX + ", " + toY);
      computeLength();
    }
  }
}
