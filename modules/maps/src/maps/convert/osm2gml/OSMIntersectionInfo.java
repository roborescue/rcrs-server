package maps.convert.osm2gml;

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
 * Information about an OSM intersection.
 */
public class OSMIntersectionInfo implements OSMShape, Puntal {

    @Getter private final OSMNode node;
    @Getter private final Set<RoadAspect> roads;
    @Getter @Setter private List<Point2D> vertices;
    private Area area;

    /**
     * Create an IntersectionInfo.
     * @param node The OSMNode at the center of the intersection.
     */
    public OSMIntersectionInfo(final OSMNode node) {
        this.node = node;
        this.roads = new LinkedHashSet<>();
        this.vertices = new ArrayList<>();
        this.area = null;
    }

    /**
     * Add the road that connect to this intersection.
     * @param road The road that connect to this intersection.
     */
    public void addRoadSegment(final OSMRoadInfo road) {
        roads.add(new RoadAspect(road, node));
    }

    /**
     * Remove the road that connected to this intersection.
     * @param road The road that connected to this intersection.
     */
    public void removeRoadSegment(final OSMRoadInfo road) {
        roads.remove(new RoadAspect(road, node));
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("IntersectionInfo (center ");
        result.append(node);
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
        return node.getPoint();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof OSMIntersectionInfo other)) return false;
        return node.getId() == other.node.getId();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(node.getId());
    }
}
