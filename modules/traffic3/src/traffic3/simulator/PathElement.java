package traffic3.simulator;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.misc.geometry.Point2D;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
   A container for information about a step along a path.
*/
public class PathElement {
    private EntityID areaID;
    private Point2D goalPoint;
    private List<Point2D> allPoints;

    /**
       Construct a PathElement.
       @param areaID The ID of the area this element refers to.
       @param goalPoint The goal of the path element.
       @param wayPoints Zero or more intermediate points that can be used if there is no line-of-sight to the goal.
    */
    public PathElement(EntityID areaID, Point2D goalPoint, Point2D... wayPoints) {
        this.areaID = areaID;
        this.goalPoint = goalPoint;
        allPoints = new ArrayList<Point2D>(Arrays.asList(wayPoints));
        allPoints.add(goalPoint);
        Collections.reverse(allPoints);
    }

    @Override
    public String toString() {
        return "Move to area " + areaID + ": " + allPoints;
    }

    /**
       Get the goal point.
       @return The goal point.
    */
    public Point2D getGoal() {
        return goalPoint;
    }

    /**
       Get the list of waypoints in preferred order.
       @return The waypoints.
    */
    public List<Point2D> getWaypoints() {
        return Collections.unmodifiableList(allPoints);
    }
}