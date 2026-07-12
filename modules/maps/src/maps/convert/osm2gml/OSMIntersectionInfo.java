package maps.convert.osm2gml;

import java.awt.geom.Rectangle2D;
import java.util.*;

import java.awt.geom.Area;
import java.awt.geom.Path2D;

import lombok.Getter;
import maps.convert.osm2gml.debug.Puntal;
import maps.osm.OSMRoadType;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;

//import rescuecore2.misc.gui.ShapeDebugFrame;
//import java.awt.Color;

import maps.osm.OSMNode;

/**
   Information about an OSM intersection.
*/
public class OSMIntersectionInfo implements OSMShape, Puntal {

    @Getter private final OSMNode center;
    private final List<RoadAspect> roads;
    private List<Point2D> vertices;
    private Area area;

    /**
       Create an IntersectionInfo.
       @param center The OSMNode at the centre of the intersection.
    */
    public OSMIntersectionInfo(OSMNode center) {
        this.center = center;
        roads = new ArrayList<>();
    }

    /**
       Add an incoming road.
       @param road The incoming road.
    */
    public void addRoadSegment(OSMRoadInfo road) {
        if (road.getFrom() == center && road.getTo() == center) {
            System.out.println("Degenerate road found");
        }
        else {
            roads.add(new RoadAspect(road, center));
        }
    }

    /**
     * Clear the list of connected road segments.
     * Used before rebuilding the intersection's connections after a merge.
     */
    public void clearRoadSegments() {
        roads.clear();
    }

    /**
       Process this intersection and determine the vertices and area it covers.
       @param sizeOf1m The size of 1m in latitude/longitude.
    */
    public void process(double sizeOf1m) {
        vertices = new ArrayList<>();

        if (roads.isEmpty()) {
            area = null;
        } else if (roads.size() == 1) {
            processSingleRoad(sizeOf1m);
            area = null;
        } else {
            processRoads(sizeOf1m);
        }
    }

    /**
     * Get the underlying OSMNode that represents the key point of this intersection.
     * @return The underlying OSMNode.
     */
    public OSMNode getUnderlyingNode() {
        return center;
    }

    /**
     * Get the representative geometric location of this intersection.
     * If the intersection polygon has been processed, it returns the centroid of that polygon.
     * Otherwise, it returns the location of the central OSMNode.
     * @return The location as a Point2D.
     */
    public Point2D getLocation() {
        if (area != null && !area.isEmpty()) {
            Rectangle2D bounds = area.getBounds2D();
            return new Point2D(bounds.getCenterX(), bounds.getCenterY());
        }
        // As a fallback, use the location of the central node.
        return new Point2D(center.getLongitude(), center.getLatitude());
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
        result.append(center);
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
        // Sort incoming roads counterclockwise about the centre.
        Point2D center = new Point2D(this.center.getLongitude(), this.center.getLatitude());
        CounterClockwiseSort sort = new CounterClockwiseSort(center);
        roads.sort(sort);

        Map<RoadAspect, Point2D[]> roadMouths = new HashMap<>();
        final double maxRoadWidth = roads.stream().map(RoadAspect::getWidth).max(Double::compareTo).orElseThrow();
        final double mouthDistance = maxRoadWidth * sizeOf1m;
        roads.forEach(road ->
                roadMouths.put(road, calculateRoadMouth(road, center, mouthDistance, sizeOf1m)));

        // Go through each pair of adjacent incoming roads and connect their mouths.
        Iterator<RoadAspect> it = roads.iterator();
        RoadAspect first = it.next();
        RoadAspect previous = first;
        while (it.hasNext()) {
            RoadAspect next = it.next();
            // Add the right corner of the previous road's mouth
            vertices.add(roadMouths.get(previous)[1]);
            // Add the left corner of the next road's mouth
            vertices.add(roadMouths.get(next)[0]);

            // Connect the two corners
            previous.setRightEnd(roadMouths.get(previous)[1]);
            next.setLeftEnd(roadMouths.get(next)[0]);

            previous = next;
        }
        // Connect the last road back to the first one
        vertices.add(roadMouths.get(previous)[1]);
        vertices.add(roadMouths.get(first)[0]);
        previous.setRightEnd(roadMouths.get(previous)[1]);
        first.setLeftEnd(roadMouths.get(first)[0]);

        // If there are multiple vertices then compute the area
        if (vertices.size() > 2) {
            Path2D.Double path = new Path2D.Double();
            Iterator<Point2D> ix = vertices.iterator();
            Point2D p = ix.next();
            path.moveTo(p.getX(), p.getY());
            while (ix.hasNext()) {
                p = ix.next();
                path.lineTo(p.getX(), p.getY());
            }
            path.closePath();
            area = new Area(path.createTransformedShape(null));
        }
        else {
            area = null;
        }
    }

    private Point2D[] calculateRoadMouth(
            final RoadAspect road, final Point2D center, final double mouthDistance, final double sizeOf1m) {
        final OSMNode farNode = road.getFarNode();
        final Point2D farPoint = new Point2D(farNode.getLongitude(), farNode.getLatitude());

        // roadVector points FROM the far node TO the centre point.
        final Vector2D roadVector = center.minus(farPoint);

        // Calculated the mouth's center point using the *actual* safe distance.
        final Vector2D oppositeVector = roadVector.scale(-1);
        final double adjustedMouseDistance = Math.min(roadVector.getLength() * 0.45, mouthDistance);
        final Point2D mouthCenter = center.plus(oppositeVector.normalised().scale(adjustedMouseDistance));

        // Calculate the left and right corners of the mouth.
        final Vector2D roadNormal = roadVector.getNormal().normalised()
                .scale(road.getWidth() * sizeOf1m / 2.0);

        final Point2D leftCorner = mouthCenter.plus(roadNormal);
        final Point2D rightCorner = mouthCenter.plus(roadNormal.scale(-1));

        return new Point2D[]{leftCorner, rightCorner};
    }

    /**
       This "intersection" has a single incoming road. Set the incoming road's left and right edges.
    */
    private void processSingleRoad(double sizeOf1m) {
        Point2D centrePoint = new Point2D(center.getLongitude(), center.getLatitude());
        RoadAspect road = roads.getFirst();
        OSMNode node = road.getFarNode();
        Point2D nodePoint = new Point2D(node.getLongitude(), node.getLatitude());
        Vector2D nodeVector = centrePoint.minus(nodePoint);
        Vector2D nodeNormal = nodeVector.getNormal().normalised().scale(-road.getWidth() * sizeOf1m / 2);
        Vector2D nodeNormal2 = nodeNormal.scale(-1);
        Point2D start1Point = nodePoint.plus(nodeNormal);
        Point2D start2Point = nodePoint.plus(nodeNormal2);
        Line2D line1 = new Line2D(start1Point, nodeVector);
        Line2D line2 = new Line2D(start2Point, nodeVector);
        Point2D end1 = line1.getPoint(1);
        Point2D end2 = line2.getPoint(1);
        road.setRightEnd(end1);
        road.setLeftEnd(end2);
    }

    @Override
    public Point2D getPoint() {
        return center.getPoint();
    }

    private static class RoadAspect {
        private final boolean forward;
        private final OSMRoadInfo road;

        RoadAspect(final OSMRoadInfo road, final OSMNode center) {
            this.road = road;

            forward = center.equals(road.getTo());
        }

        OSMNode getFarNode() {
            return forward ? road.getFrom() : road.getTo();
        }

        void setLeftEnd(final Point2D p) {
            if (forward) {
                road.setToLeft(p);
            } else {
                road.setFromRight(p);
            }
        }

        void setRightEnd(final Point2D p) {
            if (forward) {
                road.setToRight(p);
            } else {
                road.setFromLeft(p);
            }
        }

        double getWidth() {
            final OSMRoadType roadType = road.getType();
            return road.hasLaneCount() ?
                road.getLaneCount() * roadType.getLaneWidth() + 2 * roadType.getShoulderWidth() :
                roadType.getDefaultWidth();
        }
    }

    private record CounterClockwiseSort(Point2D center) implements Comparator<RoadAspect> {
        /**
         * Construct a {@code CounterClockwiseSort} with a reference point.
         *
         * @param center The reference point.
         */
        private CounterClockwiseSort {
        }

        @Override
        public int compare(final RoadAspect first, final RoadAspect second) {
            double d1 = score(first);
            double d2 = score(second);
            return Double.compare(d2, d1);
        }

        /**
         * Compute the score for a RoadAspect - the amount of clockwiseness from 12 o'clock.
         *
         * @param aspect The RoadAspect.
         * @return The amount of clockwiseness. This will be in the range [0..4) with 0 representing 12 o'clock, 1 representing 3 o'clock and so on.
         */
        public double score(RoadAspect aspect) {
            OSMNode node = aspect.getFarNode();
            Point2D point = new Point2D(node.getLongitude(), node.getLatitude());
            Vector2D v = point.minus(center);
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
    }
}
