package maps.convert.osm2gml;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import maps.convert.osm2gml.debug.Puntal;
import rescuecore2.misc.geometry.Point2D;

import maps.osm.OSMNode;

/**
   Information about an OSM intersection.
*/
public class OSMIntersectionInfo implements OSMShape, Puntal {

    @Getter private final OSMNode center;
    @Getter private final Set<RoadAspect> roads;
    @Getter @Setter private List<Point2D> vertices;
    private Area area;

    /**
       Create an IntersectionInfo.
       @param center The OSMNode at the centre of the intersection.
    */
    public OSMIntersectionInfo(OSMNode center) {
        this.center = center;
        this.roads = new HashSet<>();
        this.vertices = new ArrayList<>();
        this.area = null;
    }

    /**
       Add an incoming road.
       @param road The incoming road.
    */
    public void addRoadSegment(OSMRoadInfo road) {
        if (road.getFrom().equals(center) && road.getTo().equals(center)) {
            System.err.println("Degenerate road found");
            return;
        }
        roads.add(new RoadAspect(road, center));
    }

    /**
     * Clear the list of connected road segments.
     * Used before rebuilding the intersection's connections after a merge.
     */
    public void clearRoadSegments() {
        roads.clear();
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

    public Area getArea() {
        if (roads.size() < 2) return null;
        if (area != null) return area;
        if (vertices.isEmpty()) return new Area();
        return (area = createArea());
    }

    private Area createArea() {
        final Path2D.Double path = new Path2D.Double();

        final Point2D first = vertices.getFirst();
        path.moveTo(first.getX(), first.getY());

        for (int i = 1; i < vertices.size(); i++) {
            final Point2D p = vertices.get(i);
            path.lineTo(p.getX(), p.getY());
        }

        path.closePath();

        return new Area(path);
    }

    @Override
    public Point2D getPoint() {
        return center.getPoint();
    }
}
