package maps.convert.osm2gml;

import lombok.Getter;
import maps.osm.OSMNode;
import maps.osm.OSMRoadType;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;

import java.util.Objects;

public class RoadAspect {
    private final OSMRoadInfo road;
    private final boolean isIncoming;
    private Point2D rightEnd;
    private Point2D leftEnd;
    private Line2D leftBoundaryLine;
    private Line2D rightBoundaryLine;

    @Getter private final OSMNode center;
    @Getter private final OSMNode farNode;

    public RoadAspect(final OSMRoadInfo road, final OSMNode center) {
        this.road = road;
        this.isIncoming = road.getEnd().equals(center);

        this.center = center;
        this.farNode = isIncoming ? road.getStart() : road.getEnd();
    }

    public void setRightEnd(final Point2D rightEnd) {
        this.rightEnd = rightEnd;
        if (isIncoming) {
            road.setStartLeft(rightEnd);
        } else {
            road.setEndRight(rightEnd);
        }
    }

    public void setLeftEnd(final Point2D leftEnd) {
        this.leftEnd = leftEnd;
        if (isIncoming) {
            road.setStartRight(leftEnd);
        } else {
            road.setEndLeft(leftEnd);
        }
    }

    public Point2D getCenterPoint() {
        return center.getPoint();
    }

    public Point2D getFarPoint() {
        return farNode.getPoint();
    }

    public Vector2D getVector() {
        return getFarPoint().minus(getCenterPoint());
    }

    public double getWidth(final double sizeOf1Meter) {
        final OSMRoadType roadType = road.getType();
        final double widthMeter = road.hasLaneCount() ?
                road.getLaneCount() * roadType.getLaneWidth() + 2 * roadType.getShoulderWidth() :
                roadType.getDefaultWidth();
        return widthMeter * sizeOf1Meter;
    }

    public Line2D getMouthBoundary() {
        return rightEnd == null || leftEnd == null ? null : new Line2D(rightEnd, leftEnd);
    }

    public Line2D getLeftBoundaryLine(final double sizeOf1Meter) {
        if (leftBoundaryLine == null) leftBoundaryLine = getBoundaryLine(sizeOf1Meter, true);
        return leftBoundaryLine;
    }

    public Line2D getRightBoundaryLine(final double sizeOf1Meter) {
        if (rightBoundaryLine == null) rightBoundaryLine = getBoundaryLine(sizeOf1Meter, false);
        return rightBoundaryLine;
    }

    public Line2D getBoundaryLine(final double sizeOf1Meter, final boolean isLeft) {
        final double halfWidth = getWidth(sizeOf1Meter) / 2;
        final Vector2D vector = getVector();
        final Vector2D normalVector = vector.getNormal().normalised();
        final Point2D origin = getCenterPoint().plus(normalVector.scale((isLeft ? 1 : -1) * halfWidth));
        return new Line2D(origin, vector);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof RoadAspect other)) return false;
        return center.getId() == other.center.getId() && farNode.getId() == other.farNode.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(center.getId(), farNode.getId());
    }
}
