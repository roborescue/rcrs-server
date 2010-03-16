package maps.convert.osm2gml;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import java.util.Comparator;

import java.awt.geom.Area;
import java.awt.geom.Path2D;

import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.misc.geometry.GeometryTools2D;

//import rescuecore2.misc.gui.ShapeDebugFrame;
//import java.awt.Color;

import maps.osm.OSMNode;

/**
   Information about an OSM intersection.
*/
public class OSMIntersectionInfo implements OSMShape {
    //    private static ShapeDebugFrame debug = new ShapeDebugFrame();

    private OSMNode centre;
    private List<RoadAspect> roads;
    private List<Point2D> vertices;
    private Area area;

    /**
       Create an IntersectionInfo.
       @param centre The OSMNode at the centre of the intersection.
    */
    public OSMIntersectionInfo(OSMNode centre) {
        this.centre = centre;
        roads = new ArrayList<RoadAspect>();
    }

    /**
       Add an incoming road.
       @param road The incoming road.
    */
    public void addRoadSegment(OSMRoadInfo road) {
        if (road.getFrom() == centre && road.getTo() == centre) {
            System.out.println("Degenerate road found");
        }
        else {
            roads.add(new RoadAspect(road, centre));
        }
    }

    /**
       Process this intersection and determine the vertices and area it covers.
       @param sizeOf1m The size of 1m in latitude/longitude.
    */
    public void process(double sizeOf1m) {
        vertices = new ArrayList<Point2D>();
        if (roads.size() > 1) {
            processRoads(sizeOf1m);
        }
        else {
            processSingleRoad(sizeOf1m);
            area = null;
        }
    }

    /**
       Get the OSMNode at the centre of this intersection.
       @return The OSMNode at the centre.
    */
    public OSMNode getCentre() {
        return centre;
    }

    @Override
    public Area getArea() {
        return area;
    }

    @Override
    public List<Point2D> getVertices() {
        return vertices;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("IntersectionInfo (centre ");
        result.append(centre);
        result.append(") [");
        for (Iterator<Point2D> it = vertices.iterator(); it.hasNext();) {
            result.append(it.next().toString());
            if (it.hasNext()) {
                result.append(", ");
            }
        }
        result.append("]");
        if (area == null) {
            result.append(" (degenerate)");
        }
        return result.toString();
    }

    private void processRoads(double sizeOf1m) {
        // Sort incoming roads counterclockwise about the centre
        Point2D centrePoint = new Point2D(centre.getLongitude(), centre.getLatitude());
        CounterClockwiseSort sort = new CounterClockwiseSort(centrePoint);
        Collections.sort(roads, sort);
        // Go through each pair of adjacent incoming roads and compute the two intersection points
        Iterator<RoadAspect> it = roads.iterator();
        RoadAspect first = it.next();
        RoadAspect previous = first;
        while (it.hasNext()) {
            RoadAspect next = it.next();
            Point2D p = findIncomingRoadIntersection(previous, next, centrePoint, sizeOf1m);
            vertices.add(p);
            previous = next;
        }
        Point2D p = findIncomingRoadIntersection(previous, first, centrePoint, sizeOf1m);
        vertices.add(p);
        // If there are multiple vertices then compute the area
        if (vertices.size() > 2) {
            Iterator<Point2D> ix = vertices.iterator();
            Point2D point = ix.next();
            Path2D.Double path = new Path2D.Double();
            path.moveTo(point.getX(), point.getY());
            while (ix.hasNext()) {
                point = ix.next();
                path.lineTo(point.getX(), point.getY());
            }
            path.closePath();
            area = new Area(path.createTransformedShape(null));
        }
        else {
            area = null;
        }
    }

    /**
       Process two incoming roads and find the intersection of the right edge of the first road and the left edge of the second road.
       @param first The road to check the right edge of.
       @param second The road the check the left edge of.
       @param centrePoint The centre of the intersection.
       @return The intersection of the two roads.
    */
    private Point2D findIncomingRoadIntersection(RoadAspect first, RoadAspect second, Point2D centrePoint, double sizeOf1m) {
        OSMNode firstNode = first.getFarNode();
        OSMNode secondNode = second.getFarNode();
        // Find the intersection of the incoming road edges
        Point2D firstPoint = new Point2D(firstNode.getLongitude(), firstNode.getLatitude());
        Point2D secondPoint = new Point2D(secondNode.getLongitude(), secondNode.getLatitude());
        Vector2D firstVector = centrePoint.minus(firstPoint);
        Vector2D secondVector = centrePoint.minus(secondPoint);
        Vector2D firstNormal = firstVector.getNormal().normalised().scale(-Constants.ROAD_WIDTH * sizeOf1m / 2);
        Vector2D secondNormal = secondVector.getNormal().normalised().scale(Constants.ROAD_WIDTH * sizeOf1m / 2);
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

        /*
          List<ShapeDebugFrame.ShapeInfo> shapes = new ArrayList<ShapeDebugFrame.ShapeInfo>();
          shapes.add(new ShapeDebugFrame.Line2DShapeInfo(new Line2D(firstPoint, centrePoint), "First road", Color.BLUE, false, false));
          shapes.add(new ShapeDebugFrame.Line2DShapeInfo(new Line2D(firstPoint, firstNormal), "First road offset", Color.YELLOW, false, false));
          shapes.add(new ShapeDebugFrame.Point2DShapeInfo(start1Point, "Left start", Color.BLUE, true));
          shapes.add(new ShapeDebugFrame.Line2DShapeInfo(line1, "Left edge", Color.BLUE, true, false));
          shapes.add(new ShapeDebugFrame.Line2DShapeInfo(new Line2D(secondPoint, centrePoint), "Second road", Color.WHITE, false, false));
          shapes.add(new ShapeDebugFrame.Line2DShapeInfo(new Line2D(secondPoint, secondNormal), "Second road offset", Color.CYAN, false, false));
          shapes.add(new ShapeDebugFrame.Point2DShapeInfo(start2Point, "Right start", Color.WHITE, true));
          shapes.add(new ShapeDebugFrame.Line2DShapeInfo(line2, "Right edge", Color.WHITE, true, false));
          shapes.add(new ShapeDebugFrame.Point2DShapeInfo(intersection, "Intersection", Color.ORANGE, true));
          debug.show("Intersection", shapes);
        */

        return intersection;
    }

    /**
       This "intersection" has a single incoming road. Set the incoming road's left and right edges.
    */
    private void processSingleRoad(double sizeOf1m) {
        Point2D centrePoint = new Point2D(centre.getLongitude(), centre.getLatitude());
        RoadAspect road = roads.iterator().next();
        OSMNode node = road.getFarNode();
        Point2D nodePoint = new Point2D(node.getLongitude(), node.getLatitude());
        Vector2D nodeVector = centrePoint.minus(nodePoint);
        Vector2D nodeNormal = nodeVector.getNormal().normalised().scale(-Constants.ROAD_WIDTH * sizeOf1m / 2);
        Vector2D nodeNormal2 = nodeNormal.scale(-1);
        Point2D start1Point = nodePoint.plus(nodeNormal);
        Point2D start2Point = nodePoint.plus(nodeNormal2);
        Line2D line1 = new Line2D(start1Point, nodeVector);
        Line2D line2 = new Line2D(start2Point, nodeVector);
        Point2D end1 = line1.getPoint(1);
        Point2D end2 = line2.getPoint(1);
        road.setRightEnd(end1);
        road.setLeftEnd(end2);

        /*
          List<ShapeDebugFrame.ShapeInfo> shapes = new ArrayList<ShapeDebugFrame.ShapeInfo>();
          shapes.add(new ShapeDebugFrame.Line2DShapeInfo(new Line2D(nodePoint, centrePoint), "Single road", Color.BLUE, false));
          shapes.add(new ShapeDebugFrame.Line2DShapeInfo(new Line2D(nodePoint, nodeNormal), "Offset 1", Color.YELLOW, false));
          shapes.add(new ShapeDebugFrame.Line2DShapeInfo(new Line2D(nodePoint, nodeNormal2), "Offset 2", Color.CYAN, false));
          shapes.add(new ShapeDebugFrame.Point2DShapeInfo(start1Point, "Left start", Color.BLUE, true));
          shapes.add(new ShapeDebugFrame.Line2DShapeInfo(line1, "Left edge", Color.BLUE, true));
          shapes.add(new ShapeDebugFrame.Point2DShapeInfo(start2Point, "Right start", Color.WHITE, true));
          shapes.add(new ShapeDebugFrame.Line2DShapeInfo(line2, "Right edge", Color.WHITE, true));
          shapes.add(new ShapeDebugFrame.Point2DShapeInfo(end1, "Endpoint 1", Color.ORANGE, true));
          shapes.add(new ShapeDebugFrame.Point2DShapeInfo(end2, "Endpoint 2", Color.PINK, true));
          debug.show(shapes);
        */
    }

    private static class RoadAspect {
        private boolean forward;
        private OSMRoadInfo road;

        RoadAspect(OSMRoadInfo road, OSMNode intersection) {
            this.road = road;
            forward = intersection == road.getTo();
        }

        OSMRoadInfo getRoad() {
            return road;
        }

        OSMNode getFarNode() {
            return forward ? road.getFrom() : road.getTo();
        }

        void setLeftEnd(Point2D p) {
            if (forward) {
                road.setToLeft(p);
            }
            else {
                road.setFromRight(p);
            }
        }

        void setRightEnd(Point2D p) {
            if (forward) {
                road.setToRight(p);
            }
            else {
                road.setFromLeft(p);
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
            OSMNode node = aspect.getFarNode();
            Point2D point = new Point2D(node.getLongitude(), node.getLatitude());
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
