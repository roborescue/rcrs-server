package maps.convert.legacy2gml;

import rescuecore2.misc.geometry.Point2D;

import maps.gml.GMLMap;
import maps.gml.GMLNode;
import maps.gml.GMLRoad;

import java.util.List;
import java.util.ArrayList;

/**
   Container for road information during conversion.
*/
public class RoadInfo {
    private Point2D headLeft;
    private Point2D headRight;
    private Point2D tailLeft;
    private Point2D tailRight;
    private GMLRoad road;

    /**
       Construct a RoadInfo.
    */
    public RoadInfo() {
    }

    /**
       Set the left corner at the head end.
       @param newHeadLeft The new head-left corner.
    */
    public void setHeadLeft(Point2D newHeadLeft) {
        headLeft = newHeadLeft;
    }

    /**
       Set the right corner at the head end.
       @param newHeadRight The new head-right corner.
    */
    public void setHeadRight(Point2D newHeadRight) {
        headRight = newHeadRight;
    }

    /**
       Set the left corner at the tail end.
       @param newTailLeft The new tail-left corner.
    */
    public void setTailLeft(Point2D newTailLeft) {
        tailLeft = newTailLeft;
    }

    /**
       Set the right corner at the tail end.
       @param newTailRight The new tail-right corner.
    */
    public void setTailRight(Point2D newTailRight) {
        tailRight = newTailRight;
    }

    /**
       Get the generated GMLRoad.
       @return The generated road.
    */
    public GMLRoad getRoad() {
        return road;
    }

    /**
       Process this RoadInfo and generate a GMLRoad object.
       @param gml The GML map.
     */
    public void process(GMLMap gml) {
        List<GMLNode> apexes = new ArrayList<GMLNode>();
        apexes.add(gml.createNode(headLeft.getX(), headLeft.getY()));
        apexes.add(gml.createNode(tailLeft.getX(), tailLeft.getY()));
        apexes.add(gml.createNode(tailRight.getX(), tailRight.getY()));
        apexes.add(gml.createNode(headRight.getX(), headRight.getY()));
        road = gml.createRoadFromNodes(apexes);
    }
}

