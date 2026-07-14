package maps.convert.osm2gml;

import lombok.Getter;
import maps.osm.OSMNode;
import maps.osm.OSMRoadType;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;

public class RoadAspect {
    private final OSMRoadInfo road;
    private final boolean isIncoming;
    private Point2D rightEnd;
    private Point2D leftEnd;

    @Getter private final Point2D centerPoint;
    @Getter private final Point2D farPoint;
    @Getter private final double roadWidth;

    public RoadAspect(final OSMRoadInfo road, final OSMNode center) {
        this.road = road;
        this.isIncoming = road.getTo().equals(center);

        this.centerPoint = center.getPoint();
        this.farPoint = (isIncoming ? road.getFrom() : road.getTo()).getPoint();
        this.roadWidth = calculateRoadWidth(road);
    }

    private double calculateRoadWidth(final OSMRoadInfo road) {
        final OSMRoadType roadType = road.getType();
        return road.hasLaneCount() ?
                road.getLaneCount() * roadType.getLaneWidth() + 2 * roadType.getShoulderWidth() :
                roadType.getDefaultWidth();
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

    public Line2D getMouseBoundary() {
        return rightEnd == null || leftEnd == null ? null : new Line2D(rightEnd, leftEnd);
    }
}
