package maps.convert.osm2gml;

import maps.convert.ConvertStep;
import maps.convert.osm2gml.debug.*;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;

import java.util.*;
import java.util.stream.Stream;

/**
 * This step processes the intersection graph and generates the
 * geometric polygon areas for each intersection
 */
public class GenerateIntersectionAreaStep extends ConvertStep {
    private final TemporaryMap map;
    private final double sizeOf1Meter;

    private static final double SETBACK_COEFFICIENT = 0.5;
    private static final double STRAIGHT_ANGLE_TOLERANCE_DEGREES = 5.0;
    private static final double MITER_DISTANCE_LIMIT_COEFFICIENT = 1.5;
    private static final double BOUNDARY_LENGTH_LIMIT_COEFFICIENT = 0.4;

    /**
     * Construct a GenerateIntersectionAreaStep.
     * @param map The map to use.
     */
    public GenerateIntersectionAreaStep(final TemporaryMap map) {
        this.map = map;
        this.sizeOf1Meter = ConvertTools.sizeOf1Metre(map.getOSMMap());
    }

    @Override
    public String getDescription() {
        return "Generating intersection areas";
    }

    @Override
    protected void step() {
        final Collection<OSMIntersectionInfo> intersections = map.getOSMIntersections();
        intersections.forEach(this::computeIntersectionGeometry);
        setStatus("Generated polygon areas for " + intersections.size() + " intersections");
        visualizeResults();
    }

    private void computeIntersectionGeometry(final OSMIntersectionInfo intersection) {
        intersection.setVertices(computeVertices(intersection.getRoads()));
        bumpProgress();
    }

    private List<Point2D> computeVertices(final Set<RoadAspect> roads) {
        return switch (roads.size()) {
            case 0 -> Collections.emptyList();
            case 1 -> processDeadEnd(roads.iterator().next());
            case 2 -> {
                final Iterator<RoadAspect> it = roads.iterator();
                yield processThroughRoad(it.next(), it.next());
            }
            default -> generateIntersectionPolygon(roads);
        };
    }

    private List<Point2D> processDeadEnd(final RoadAspect road) {
        road.setRightEnd(road.getRightBoundaryLine(sizeOf1Meter).getOrigin());
        road.setLeftEnd(road.getLeftBoundaryLine(sizeOf1Meter).getOrigin());
        return collectVertices(List.of(road));
    }

    private List<Point2D> processThroughRoad(final RoadAspect first, final RoadAspect second) {
        if (isStraight(first, second)) {
            final double firstRoadWidth = first.getWidth(sizeOf1Meter);
            first.setRightEnd(setbackPoint(first.getRightBoundaryLine(sizeOf1Meter), firstRoadWidth));
            first.setLeftEnd(setbackPoint(first.getLeftBoundaryLine(sizeOf1Meter), firstRoadWidth));

            final double secondRoadWidth = second.getWidth(sizeOf1Meter);
            second.setRightEnd(setbackPoint(second.getRightBoundaryLine(sizeOf1Meter), secondRoadWidth));
            second.setLeftEnd(setbackPoint(second.getLeftBoundaryLine(sizeOf1Meter), secondRoadWidth));

            return collectVertices(List.of(first, second));
        }

        final Point2D firstRightEnd = intersectOrThrow(
                first.getRightBoundaryLine(sizeOf1Meter), second.getLeftBoundaryLine(sizeOf1Meter));
        final Point2D firstLeftEnd = intersectOrThrow(
                first.getLeftBoundaryLine(sizeOf1Meter), second.getRightBoundaryLine(sizeOf1Meter));

        first.setRightEnd(firstRightEnd);
        first.setLeftEnd(firstLeftEnd);
        second.setRightEnd(firstLeftEnd);
        second.setLeftEnd(firstRightEnd);

        return List.of(firstRightEnd, firstLeftEnd);
    }

    private List<Point2D> generateIntersectionPolygon(final Set<RoadAspect> roads) {
        final List<RoadAspect> sortedRoads = sortRoadsCCW(roads);
        final int degree = sortedRoads.size();

        for (int i = 0; i < degree; i++) {
            final RoadAspect prev = sortedRoads.get((i - 1 + degree) % degree);
            final RoadAspect curr = sortedRoads.get(i);
            final RoadAspect next = sortedRoads.get((i + 1) % degree);

            curr.setRightEnd(computeCorner(curr, prev, false));
            curr.setLeftEnd(computeCorner(curr, next, true));
        }

        return collectVertices(sortedRoads);
    }

    private List<RoadAspect> sortRoadsCCW(final Collection<RoadAspect> roads) {
        return roads.stream().sorted(Comparator.comparingDouble(road -> {
            final Point2D farPoint = road.getFarPoint();
            final Vector2D roadVector = farPoint.minus(road.getCenterPoint());
            return Math.atan2(roadVector.getY(), roadVector.getX());
        })).toList();
    }

    private Point2D computeCorner(final RoadAspect own, final RoadAspect other, final boolean isLeft) {
        final Line2D ownBoundary = own.getBoundaryLine(sizeOf1Meter, isLeft);

        if (isStraight(own, other)) {
            return setbackPoint(ownBoundary, own.getWidth(sizeOf1Meter));
        }

        final Line2D otherBoundary = other.getBoundaryLine(sizeOf1Meter, !isLeft);
        final Point2D miterCorner = intersectOrThrow(ownBoundary, otherBoundary);
        final double miterDistanceLimit = computeMiterDistanceLimit(
                ownBoundary, otherBoundary, own.getWidth(sizeOf1Meter), other.getWidth(sizeOf1Meter));
        final Vector2D cornerOffset = miterCorner.minus(own.getCenterPoint());

        if (miterDistanceLimit < cornerOffset.getLength()) {
            return own.getCenterPoint().plus(cornerOffset.normalised().scale(miterDistanceLimit));
        }
        return miterCorner;
    }

    private boolean isStraight(final RoadAspect first, final RoadAspect second) {
        final double angleBetweenRoads = GeometryTools2D.getAngleBetweenVectors(first.getVector(), second.getVector());
        return Math.abs(angleBetweenRoads - 180) < STRAIGHT_ANGLE_TOLERANCE_DEGREES;
    }

    private Point2D setbackPoint(final Line2D boundaryLine, final double roadWidth) {
        final double defaultLength = roadWidth * SETBACK_COEFFICIENT;
        final double lengthLimit = boundaryLine.getLength() * BOUNDARY_LENGTH_LIMIT_COEFFICIENT;
        final double setbackLength = Math.min(lengthLimit, defaultLength);
        return boundaryLine.getOrigin().plus(boundaryLine.getDirection().normalised().scale(setbackLength));
    }

    private Point2D intersectOrThrow(final Line2D first, final Line2D second) {
        return Objects.requireNonNull(GeometryTools2D.getIntersectionPoint(first, second),
                "Expect boundary lines to intersect but they were parallel: " + first + ", " + second);
    }

    private double computeMiterDistanceLimit(final Line2D ownBoundary, final Line2D otherBoundary,
                                             final double ownWidth, final double otherWidth) {
        final double widthBasedLimit = Math.max(ownWidth, otherWidth) * MITER_DISTANCE_LIMIT_COEFFICIENT;
        final double boundaryLengthBasedLimit =
                Math.min(ownBoundary.getLength(), otherBoundary.getLength()) * BOUNDARY_LENGTH_LIMIT_COEFFICIENT;
        return Math.min(widthBasedLimit, boundaryLengthBasedLimit);
    }

    private List<Point2D> collectVertices(final Collection<RoadAspect> roads) {
        return roads.stream().flatMap(road -> {
            final Line2D boundary = road.getMouthBoundary();
            return boundary == null ? Stream.empty() : Stream.of(boundary.getOrigin(), boundary.getEndPoint());
        }).toList();
    }

    private void visualizeResults() {
        StepVisualizer.create(debug)
                .title("Generate Intersection Areas")
                .layer(LineLayer.of(map.getOSMRoads())
                        .name("OSM Roads")
                        .color(DebugPalette.CONTEXT_STROKE))
                .layer(PointLayer.of(map.getOSMIntersections())
                        .name("OSM Intersections")
                        .color(DebugPalette.SLATE_STROKE))
                .layer(PolygonLayer.of(map.getOSMIntersections())
                        .name("Generated Intersection Polygons")
                        .outlineColor(DebugPalette.MOSS_STROKE)
                        .fillColor(DebugPalette.MOSS_FILL))
                .show();
    }
}
