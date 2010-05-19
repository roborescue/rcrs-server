package maps.convert.legacy2gml;


import maps.legacy.LegacyMap;
import maps.legacy.LegacyRoad;
import maps.legacy.LegacyNode;
import maps.gml.GMLMap;
import maps.gml.GMLNode;
import maps.gml.GMLRoad;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Comparator;
import java.util.Collections;
import java.util.Iterator;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Vector2D;

/**
   Container for node information during conversion.
*/
public class NodeInfo {
    private LegacyNode node;
    private Point2D centre;
    private List<GMLNode> apexes;
    private GMLRoad road;

    /**
       Construct a NodeInfo object.
       @param node The LegacyNode to store info about.
    */
    public NodeInfo(LegacyNode node) {
        this.node = node;
        centre = new Point2D(node.getX(), node.getY());
    }

    /**
       Get the LegacyNode.
       @return The LegacyNode.
    */
    public LegacyNode getNode() {
        return node;
    }

    /**
       Get the node location.
       @return The node location.
    */
    public Point2D getLocation() {
        return centre;
    }

    /**
       Get the generated GMLRoad.
       @return The generated road or null if this node did not generate a road segment.
    */
    public GMLRoad getRoad() {
        return road;
    }

    /**
       Process the node and create GMLRoad objects if required.
       @param legacy The legacy map.
       @param gml The GML map.
       @param roadInfo A map from road ID to RoadInfo.
    */
    public void process(LegacyMap legacy, GMLMap gml, Map<Integer, RoadInfo> roadInfo) {
        apexes = new ArrayList<GMLNode>();
        List<RoadAspect> roads = new ArrayList<RoadAspect>();
        for (int id : node.getEdges()) {
            LegacyRoad lRoad = legacy.getRoad(id);
            if (lRoad == null) {
                continue;
            }
            roads.add(new RoadAspect(lRoad, node, legacy, roadInfo.get(id)));
        }
        if (roads.size() == 1) {
            RoadAspect aspect = roads.get(0);
            findRoadEdges(aspect, centre);
        }
        else {
            // Sort the roads
            CounterClockwiseSort sort = new CounterClockwiseSort(centre);
            Collections.sort(roads, sort);
            // Now build the apex list
            Iterator<RoadAspect> it = roads.iterator();
            RoadAspect first = it.next();
            RoadAspect prev = first;
            RoadAspect next;
            while (it.hasNext()) {
                next = it.next();
                Point2D apex = findIncomingRoadIntersection(prev, next, centre);
                apexes.add(gml.createNode(apex.getX(), apex.getY()));
                prev = next;
            }
            Point2D apex = findIncomingRoadIntersection(prev, first, centre);
            apexes.add(gml.createNode(apex.getX(), apex.getY()));
        }
        if (apexes.size() > 2) {
            road = gml.createRoadFromNodes(apexes);
        }
    }

    /**
       Process two incoming roads and find the intersection of the right edge of the first road and the left edge of the second road.
       @param first The road endpoint to check the right edge of.
       @param second The road endpoint to check the left edge of.
       @param centrePoint The centre of the intersection.
       @return The intersection of the two roads.
    */
    private Point2D findIncomingRoadIntersection(RoadAspect first, RoadAspect second, Point2D centrePoint) {
        LegacyNode firstNode = first.getFarNode();
        LegacyNode secondNode = second.getFarNode();
        Point2D firstPoint = new Point2D(firstNode.getX(), firstNode.getY());
        Point2D secondPoint = new Point2D(secondNode.getX(), secondNode.getY());
        // Find the intersection of the incoming road edges
        Vector2D firstVector = centrePoint.minus(firstPoint);
        Vector2D secondVector = centrePoint.minus(secondPoint);
        Vector2D firstNormal = firstVector.getNormal().normalised().scale(-first.getRoadWidth() / 2.0);
        Vector2D secondNormal = secondVector.getNormal().normalised().scale(second.getRoadWidth() / 2.0);
        Point2D start1Point = firstPoint.plus(firstNormal);
        Point2D start2Point = secondPoint.plus(secondNormal);
        Line2D line1 = new Line2D(start1Point, firstVector);
        Line2D line2 = new Line2D(start2Point, secondVector);
        Point2D intersection = GeometryTools2D.getIntersectionPoint(line1, line2);
        if (intersection == null) {
            // Lines are parallel
            // This means the normals are parallel, so we can just add a normal to the centre point to generate an intersection point
            intersection = centrePoint.plus(firstNormal);
        }
        first.setRightEnd(intersection);
        second.setLeftEnd(intersection);
        return intersection;
    }

    private void findRoadEdges(RoadAspect aspect, Point2D centrePoint) {
        LegacyNode farNode = aspect.getFarNode();
        Point2D roadPoint = new Point2D(farNode.getX(), farNode.getY());
        Vector2D vector = centrePoint.minus(roadPoint);
        Vector2D leftNormal = vector.getNormal().normalised().scale(aspect.getRoadWidth() / 2.0);
        Vector2D rightNormal = leftNormal.scale(-1);
        Point2D left = roadPoint.plus(leftNormal);
        Point2D right = roadPoint.plus(rightNormal);
        aspect.setLeftEnd(left);
        aspect.setRightEnd(right);
    }

    private static class RoadAspect {
        private boolean forward;
        private LegacyNode farNode;
        private RoadInfo info;
        private int width;

        RoadAspect(LegacyRoad road, LegacyNode intersection, LegacyMap map, RoadInfo info) {
            forward = intersection.getID() == road.getTail();
            farNode = map.getNode(forward ? road.getHead() : road.getTail());
            width = road.getWidth();
            this.info = info;
        }

        int getRoadWidth() {
            return width;
        }

        LegacyNode getFarNode() {
            return farNode;
        }

        void setLeftEnd(Point2D p) {
            if (forward) {
                info.setHeadLeft(p);
            }
            else {
                info.setTailRight(p);
            }
        }

        void setRightEnd(Point2D p) {
            if (forward) {
                info.setHeadRight(p);
            }
            else {
                info.setTailLeft(p);
            }
        }
    }

    private static class CounterClockwiseSort implements Comparator<RoadAspect> {
        private Point2D centre;

        /**
           Construct a CounterClockwiseSort with a reference point.
           @param centre The reference point.
        */
        public CounterClockwiseSort(Point2D centre) {
            this.centre = centre;
        }

        @Override
        public int compare(RoadAspect first, RoadAspect second) {
            double d1 = score(first);
            double d2 = score(second);
            if (d1 < d2) {
                return 1;
            }
            else if (d1 > d2) {
                return -1;
            }
            else {
                return 0;
            }
        }

        /**
           Compute the score for a RoadAspect - the amount of clockwiseness from 12 o'clock.
           @param aspect The RoadAspect.
           @return The amount of clockwiseness. This will be in the range [0..4) with 0 representing 12 o'clock, 1 representing 3 o'clock and so on.
        */
        public double score(RoadAspect aspect) {
            LegacyNode node = aspect.getFarNode();
            Point2D point = new Point2D(node.getX(), node.getY());
            Vector2D v = point.minus(centre);
            double sin = v.getX() / v.getLength();
            double cos = v.getY() / v.getLength();
            if (Double.isNaN(sin) || Double.isNaN(cos)) {
                System.out.println(v);
                System.out.println(v.getLength());
            }
            return convert(sin, cos);
        }

        // CHECKSTYLE:OFF:MagicNumber
        private double convert(double sin, double cos) {
            if (sin >= 0 && cos >= 0) {
                return sin;
            }
            if (sin >= 0 && cos < 0) {
                return 2 - sin;
            }
            if (sin < 0 && cos < 0) {
                return 2 - sin;
            }
            if (sin < 0 && cos >= 0) {
                return 4 + sin;
            }
            throw new IllegalArgumentException("This should be impossible! What's going on? sin=" + sin + ", cos=" + cos);
        }
        // CHECKSTYLE:ON:MagicNumber
    }
}

