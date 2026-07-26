package maps.convert.osm2gml;

import lombok.Getter;
import maps.convert.osm2gml.debug.Lineal;
import maps.osm.OSMRoadType;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;

import maps.osm.OSMNode;

import java.awt.geom.Area;
import java.awt.geom.Path2D;

import java.util.List;
import java.util.Objects;

/**
 * Information about an OSM road.
 */
public class OSMRoadInfo implements OSMObjectInfo, Lineal {
    @Getter final private OSMNode start;
    @Getter final private OSMNode end;
    @Getter final private OSMRoadType type;
    @Getter final private int laneCount;
    @Getter final private Line2D line;
    @Getter private Point2D startLeft;
    @Getter private Point2D endLeft;
    @Getter private Point2D startRight;
    @Getter private Point2D endRight;
    private Area area;

    /**
     * Constructs road information for the specified endpoints, road type,
     * and lane count.
     *
     * @param start the start node
     * @param end the end node
     * @param type the road type
     * @param laneCount the lane count
     */
    public OSMRoadInfo(OSMNode start, OSMNode end, OSMRoadType type, int laneCount) {
        this.start = start;
        this.end = end;
        this.type = type;
        this.laneCount = laneCount;
        line = new Line2D(start.getPoint(), end.getPoint());
        area = null;
    }

    /**
     * Sets the point on the left side of the start node.
     *
     * @param p the point
     */
    public void setStartLeft(Point2D p) {
        startLeft = p;
        area = null;
    }

    /**
     * Sets the point on the right side of the start node.
     *
     * @param p the point
     */
    public void setStartRight(Point2D p) {
        startRight = p;
        area = null;
    }

    /**
     * Sets the point on the left side of the end node.
     *
     * @param p the point
     */
    public void setEndLeft(Point2D p) {
        endLeft = p;
        area = null;
    }

    /**
     * Sets the point at the right side of the end node.
     *
     * @param p the point
     */
    public void setEndRight(Point2D p) {
        endRight = p;
        area = null;
    }

    /**
     * Returns whether the lane count is known.
     *
     * @return {@code true} if the lane count is known; {@code false} otherwise
     */
    public boolean hasLaneCount() {
        return laneCount != -1;
    }

    /**
     * Returns whether this road contains the specified node.
     *
     * @param node the node
     * @return {@code true} if this road contains the node;
     *         {@code false} otherwise
     */
    public boolean contains(OSMNode node) {
        return node.equals(start) || node.equals(end);
    }

    /**
     * Returns the endpoint other than the specified node.
     *
     * @param node an endpoint of this road
     * @return the other endpoint
     * @throws IllegalArgumentException if the specified node is not an endpoint
     *         of this road
     */
    public OSMNode getOtherNode(OSMNode node) {
        if (start.equals(node)) return end;
        if (end.equals(node)) return start;
        throw new IllegalArgumentException("Node is not an endpoint of this road: " + node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Area getArea() {
        if (area != null) return area;
        if (startLeft == null || startRight == null || endLeft == null || endRight == null) return null;

        final Path2D.Double path = new Path2D.Double();
        path.moveTo(startLeft.getX(), startLeft.getY());
        path.lineTo(startRight.getX(), startRight.getY());
        path.lineTo(endRight.getX(), endRight.getY());
        path.lineTo(endLeft.getX(), endLeft.getY());
        path.closePath();
        area = new Area(path.createTransformedShape(null));
        return area;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemporaryObject createTemporaryObject(List<DirectedEdge> edges) {
        return new TemporaryRoad(edges);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Point2D> getVertices() {
        return List.of(startLeft, startRight, endRight, endLeft);
    }

    @Override
    public String toString() {
        return "RoadInfo (type=" + type + ", laneCount=" + laneCount + ", vertices=["
                + startLeft + ", " + startRight + ", " + endRight + ", " + endLeft + "])";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof OSMRoadInfo other)) return false;
        return start.getId() == other.start.getId() && end.getId() == other.end.getId() ||
               start.getId() == other.end.getId() && end.getId() == other.start.getId();
    }

    @Override
    public int hashCode() {
        long a = Math.min(start.getId(), end.getId());
        long b = Math.max(start.getId(), end.getId());
        return Objects.hash(a, b);
    }
}
