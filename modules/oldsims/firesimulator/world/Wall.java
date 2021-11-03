package firesimulator.world;

import java.awt.Point;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * @author tn
 *
 */
public class Wall {
  private static final Logger LOG = Logger.getLogger(Wall.class);

  public static int MAX_SAMPLE_DISTANCE = 50000;
  public int x1;
  public int y1;
  public int x2;
  public int y2;
  public Building owner;
  public int rays;
  public int hits;
  public int selfHits;
  public int strange;
  public static float RAY_RATE = 0.01f;
  public double length;
  Point a;
  Point b;

  public Wall(int x1, int y1, int x2, int y2, Building owner) {
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
    a = new Point(x1, y1);
    b = new Point(x2, y2);
    length = a.distance(b);
    rays = (int) Math.ceil(length * RAY_RATE);
    hits = 0;
    this.owner = owner;
  }

  public boolean validate() {
    return !(a.x == b.x && a.y == b.y);
  }

  public void findHits(World world) {
    selfHits = 0;
    strange = 0;
    for (int emitted = 0; emitted < rays; emitted++) {
      // creating ray
      Point start = firesimulator.util.Geometry.getRndPoint(a, b);
      if (start == null) {
        strange++;
        LOG.debug("strange -> " + a.x + "," + a.y + "/" + b.x + "," + b.y);
        continue;
      }
      Point end = firesimulator.util.Geometry.getRndPoint(start, MAX_SAMPLE_DISTANCE);
      // intersect
      Wall closest = null;
      double minDist = Double.MAX_VALUE;
      for (Iterator it = world.allWalls.iterator(); it.hasNext();) {
        Wall other = (Wall) it.next();
        if (other == this)
          continue;
        Point cross = firesimulator.util.Geometry.intersect(start, end, other.a, other.b);
        if (cross != null) {
          if (cross.distance(start) < minDist) {
            minDist = cross.distance(start);
            closest = other;
          }
        }
      }
      if (closest == null) {
        // Nothing was hit
        continue;
      }
      if (closest.owner == this.owner) {
        // The source building was hit
        selfHits++;
      }
      if (closest != this && closest != null && closest.owner != owner) {
        hits++;
        Integer value = (Integer) owner.connectedBuildings.get(closest.owner);
        int temp = 0;
        if (value != null) {
          temp = value.intValue();
        }
        temp++;
        owner.connectedBuildings.put(closest.owner, Integer.valueOf(temp));
      }
    }
  }

  public String toString() {
    return "wall (" + a.x + "," + a.y + ")-(" + b.x + "," + b.y + "), length=" + length + "mm, rays=" + rays;
  }
}