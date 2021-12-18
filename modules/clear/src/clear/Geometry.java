package clear;

import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.standard.entities.Human;

public class Geometry {

  public static Area getClearArea(Human agent, int targetX, int targetY,
      int clearLength, int clearRad) {
    Vector2D agentToTarget = new Vector2D(targetX - agent.getX(),
        targetY - agent.getY());

    if (agentToTarget.getLength() > clearLength)
      agentToTarget = agentToTarget.normalised().scale(clearLength);
    agentToTarget = agentToTarget.normalised()
        .scale(agentToTarget.getLength() + 510);

    Vector2D backAgent = (new Vector2D(agent.getX(), agent.getY()))
        .add(agentToTarget.normalised().scale(-510));
    Line2D line = new Line2D(backAgent.getX(), backAgent.getY(),
        agentToTarget.getX(), agentToTarget.getY());

    Vector2D dir = agentToTarget.normalised().scale(clearRad);
    Vector2D perpend1 = new Vector2D(-dir.getY(), dir.getX());
    Vector2D perpend2 = new Vector2D(dir.getY(), -dir.getX());

    rescuecore2.misc.geometry.Point2D points[] = new rescuecore2.misc.geometry.Point2D[] {
        line.getOrigin().plus(perpend1), line.getEndPoint().plus(perpend1),
        line.getEndPoint().plus(perpend2), line.getOrigin().plus(perpend2)};
    int[] xPoints = new int[points.length];
    int[] yPoints = new int[points.length];
    for (int i = 0; i < points.length; i++) {
      xPoints[i] = (int) points[i].getX();
      yPoints[i] = (int) points[i].getY();
    }
    return new Area(new Polygon(xPoints, yPoints, points.length));
  }


  public static double surface(Area area) {
    PathIterator iter = area.getPathIterator(null);

    double sum_all = 0;
    while (!iter.isDone()) {
      List<double[]> points = new ArrayList<double[]>();
      while (!iter.isDone()) {
        double point[] = new double[2];
        int type = iter.currentSegment(point);
        iter.next();
        if (type == PathIterator.SEG_CLOSE) {
          if (points.size() > 0)
            points.add(points.get(0));
          break;
        }
        points.add(point);
      }

      double sum = 0;
      for (int i = 0; i < points.size() - 1; i++)
        sum += points.get(i)[0] * points.get(i + 1)[1]
            - points.get(i)[1] * points.get(i + 1)[0];

      sum_all += Math.abs(sum) / 2;
    }

    return sum_all;
  }


  public static List<int[]> getAreas(Area area) {
    PathIterator iter = area.getPathIterator(null);
    List<int[]> areas = new ArrayList<int[]>();
    ArrayList<Integer> list = new ArrayList<Integer>();
    while (!iter.isDone()) {
      double point[] = new double[2]; // x, y
      int type = iter.currentSegment(point);
      if (type == PathIterator.SEG_CLOSE) {
        if (list.size() > 0) {
          int[] newArea = new int[list.size()];
          for (int i = 0; i < list.size(); i++)
            newArea[i] = list.get(i);
          areas.add(newArea);
          list = new ArrayList<Integer>();
        }
      } else {
        list.add((int) point[0]);
        list.add((int) point[1]);
      }
      iter.next();
    }
    return areas;
  }
}