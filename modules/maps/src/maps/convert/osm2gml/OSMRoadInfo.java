package maps.convert.osm2gml;

import lombok.Getter;
import lombok.Setter;
import maps.osm.OSMRoadType;
import rescuecore2.misc.geometry.Point2D;

import maps.osm.OSMNode;

import java.awt.geom.Area;
import java.awt.geom.Path2D;

import java.util.List;

/**
 * Information about an OSM road.
 */
public class OSMRoadInfo implements OSMShape {
    @Setter @Getter private OSMNode from;
    @Setter @Getter private OSMNode to;
    @Getter final private OSMRoadType type;
    @Getter final private int laneCount;
    @Getter private Point2D fromLeft;
    @Getter private Point2D toLeft;
    @Getter private Point2D fromRight;
    @Getter private Point2D toRight;
    private Area area;

    /**
     * Create an {@code OSMRoadInfo} between two nodes.
     * @param from      The first {@code OSMNode}.
     * @param to        The second {@code OSMNode}.
     * @param type      The type of the road.
     * @param laneCount The number of lanes of the road.
     */
    public OSMRoadInfo(final OSMNode from, final OSMNode to, final OSMRoadType type, final int laneCount) {
        this.from      = from;
        this.to        = to;
        this.type      = type;
        this.laneCount = laneCount;
        area = null;
    }

    /**
     * Set the point that is on the left side of this road at the "from" end.
     * @param p The from-left corner point.
     */
    public void setFromLeft(final Point2D p) {
        fromLeft = p;
        area = null;
    }

    /**
     * Set the point that is on the right side of this road at the "from" end.
     * @param p The from-right corner point.
     */
    public void setFromRight(final Point2D p) {
        fromRight = p;
        area = null;
    }

    /**
     * Set the point that is on the left side of this road at the "to" end.
     * @param p The to-left corner point.
     */
    public void setToLeft(final Point2D p) {
        toLeft = p;
        area = null;
    }

    /**
     * Set the point that is on the right side of this road at the "to" end.
     * @param p The to-right corner point.
     */
    public void setToRight(final Point2D p) {
        toRight = p;
        area = null;
    }

    /**
     * Check whether this road has lane count information.
     * @return {@code true} if the lane count is known, {@code false} if it is unset (represented as -1).
     */
    public boolean hasLaneCount() {
        return laneCount != -1;
    }

    @Override
    public Area getArea() {
        if (area != null) return area;
        if (fromLeft == null || fromRight == null || toLeft == null || toRight == null) return null;

        final Path2D.Double path = new Path2D.Double();
        path.moveTo(fromLeft.getX(), fromLeft.getY());
        path.lineTo(fromRight.getX(), fromRight.getY());
        path.lineTo(toRight.getX(), toRight.getY());
        path.lineTo(toLeft.getX(), toLeft.getY());
        path.closePath();
        area = new Area(path.createTransformedShape(null));
        return area;
    }

    @Override
    public List<Point2D> getVertices() {
        return List.of(fromLeft, fromRight, toRight, toLeft);
    }

    @Override
    public String toString() {
        return "RoadInfo [" + fromLeft + ", " + fromRight + ", " + toRight + ", " + toLeft + "]";
    }
}
