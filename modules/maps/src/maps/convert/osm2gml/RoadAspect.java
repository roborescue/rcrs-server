package maps.convert.osm2gml;

import lombok.Getter;
import maps.osm.OSMNode;
import maps.osm.OSMRoadType;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;

public class RoadAspect {
    private final OSMRoadInfo road;
    private final boolean isIncoming;
    private Point2D rightEnd;
    private Point2D leftEnd;
    private Line2D leftBoundaryLine;
    private Line2D rightBoundaryLine;

    @Getter private final Point2D centerPoint;
    @Getter private final Point2D farPoint;
    @Getter private final Vector2D vector;

    public RoadAspect(final OSMRoadInfo road, final OSMNode center) {
        this.road = road;
        this.isIncoming = road.getTo().equals(center);

        this.centerPoint = center.getPoint();
        this.farPoint = (isIncoming ? road.getFrom() : road.getTo()).getPoint();
        this.vector = farPoint.minus(centerPoint);
    }

    public void setRightEnd(final Point2D rightEnd) {
        this.rightEnd = rightEnd;
        if (isIncoming) {
            road.setFromLeft(rightEnd);
        } else {
            road.setToRight(rightEnd);
        }
    }

    public void setLeftEnd(final Point2D leftEnd) {
        this.leftEnd = leftEnd;
        if (isIncoming) {
            road.setFromRight(leftEnd);
        } else {
            road.setToLeft(leftEnd);
        }
    }

    public Line2D getMouthBoundary() {
        return rightEnd == null || leftEnd == null ? null : new Line2D(rightEnd, leftEnd);
    }

    public Line2D getLeftBoundaryLine(final double sizeOf1Meter) {
        if (leftBoundaryLine == null) leftBoundaryLine = createBoundaryLine(true, sizeOf1Meter);
        return leftBoundaryLine;
    }

    public Line2D getRightBoundaryLine(final double sizeOf1Meter) {
        if (rightBoundaryLine == null) rightBoundaryLine = createBoundaryLine(false, sizeOf1Meter);
        return rightBoundaryLine;
    }

    private Line2D createBoundaryLine(final boolean isLeft, final double sizeOf1Meter) {
        final double halfWidth = calculateWidth(sizeOf1Meter) / 2;
        final Vector2D normalVector = vector.getNormal().normalised();
        final Point2D origin = centerPoint.plus(normalVector.scale((isLeft ? 1 : -1) * halfWidth));
        return new Line2D(origin, vector);
    }

    private double calculateWidth(final double sizeOf1Meter) {
        final OSMRoadType roadType = road.getType();
        final double widthMeter = road.hasLaneCount() ?
                road.getLaneCount() * roadType.getLaneWidth() + 2 * roadType.getShoulderWidth() :
                roadType.getDefaultWidth();
        return widthMeter * sizeOf1Meter;
    }
}
