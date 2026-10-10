package maps.convert.osm2gml;

import maps.convert.ConvertStep;
import maps.convert.osm2gml.debug.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Creates polygonal areas for intersections from the intersection graph.
 */
public class CreateIntersectionAreasStep extends ConvertStep {
    private final TemporaryMap map;
    private final double sizeOf1Meter;

    private static final double SETBACK_COEFFICIENT = 0.5;
    private static final double STRAIGHT_ANGLE_TOLERANCE_DEGREES = 5.0;
    private static final double MITER_DISTANCE_LIMIT_COEFFICIENT = 1.5;
    private static final double BOUNDARY_LENGTH_LIMIT_COEFFICIENT = 0.4;

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateIntersectionAreasStep.class);

    // How a corner position was derived.
    private enum CornerType { STRAIGHT, MITER, CLAMPED }

    // A computed corner with diagnostics. Widths are in map units.
    // miterOverrun is (unclamped miter distance / limit), or NaN when no limit was evaluated.
    private record Corner(Point2D point, CornerType type,
                          double ownWidth, double otherWidth, double miterOverrun) {

        // Returns the ratio of the wider road to the narrower one (always >= 1).
        double widthRatio() {
            return Math.max(ownWidth, otherWidth) / Math.min(ownWidth, otherWidth);
        }
    }

    // Geometry of one intersection: the mouth vertices and the per-corner diagnostics.
    private record IntersectionGeometry(int degree, List<Point2D> vertices, List<Corner> corners) {}

    /**
     * Constructs a new {@code CreateIntersectionAreasStep}.
     *
     * @param map the map
     */
    public CreateIntersectionAreasStep(TemporaryMap map) {
        this.map = map;
        sizeOf1Meter = ConvertTools.sizeOf1MetreLatitude(map.getOSMMap());
    }

    @Override
    public String getDescription() {
        return "Generating intersection areas";
    }

    @Override
    protected void step() {
        Collection<OSMIntersectionInfo> intersections = map.getOSMIntersections();
        setProgressLimit(intersections.size());
        List<IntersectionGeometry> results = intersections.stream()
                .map(this::computeIntersectionGeometry)
                .toList();
        setStatus("Generated polygon areas for " + intersections.size() + " intersections");
        logSummary(results);
        visualizeResults();
    }

    private IntersectionGeometry computeIntersectionGeometry(OSMIntersectionInfo intersection) {
        IntersectionGeometry geometry = computeGeometry(intersection.getRoads());
        intersection.setVertices(geometry.vertices());
        logIntersection(intersection, geometry);
        bumpProgress();
        return geometry;
    }

    private IntersectionGeometry computeGeometry(Set<RoadAspect> roads) {
        return switch (roads.size()) {
            case 0 -> new IntersectionGeometry(0, Collections.emptyList(), List.of());
            case 1 -> processDeadEnd(roads.iterator().next());
            case 2 -> {
                final Iterator<RoadAspect> it = roads.iterator();
                yield processThroughRoad(it.next(), it.next());
            }
            default -> generateIntersectionPolygon(roads);
        };
    }

    private IntersectionGeometry processDeadEnd(RoadAspect road) {
        road.setRightEnd(road.getRightBoundaryLine(sizeOf1Meter).getOrigin());
        road.setLeftEnd(road.getLeftBoundaryLine(sizeOf1Meter).getOrigin());
        return new IntersectionGeometry(1, collectVertices(List.of(road)), List.of());
    }

    private IntersectionGeometry processThroughRoad(RoadAspect first, RoadAspect second) {
        // Straight connections reuse computeCorner so that they are logged like any other corner.
        if (isStraight(first, second)) {
            Corner firstRight  = computeCorner(first, second, false);
            Corner firstLeft   = computeCorner(first, second, true);
            Corner secondRight = computeCorner(second, first, false);
            Corner secondLeft  = computeCorner(second, first, true);

            first.setRightEnd(firstRight.point());
            first.setLeftEnd(firstLeft.point());
            second.setRightEnd(secondRight.point());
            second.setLeftEnd(secondLeft.point());

            return new IntersectionGeometry(2, collectVertices(List.of(first, second)),
                    List.of(firstRight, firstLeft, secondRight, secondLeft));
        }

        Point2D firstRightEnd = intersectOrThrow(
                first.getRightBoundaryLine(sizeOf1Meter), second.getLeftBoundaryLine(sizeOf1Meter));
        Point2D firstLeftEnd = intersectOrThrow(
                first.getLeftBoundaryLine(sizeOf1Meter), second.getRightBoundaryLine(sizeOf1Meter));

        // The right boundary of one road meets the left boundary of the other at the same corner
        first.setRightEnd(firstRightEnd);
        first.setLeftEnd(firstLeftEnd);
        second.setRightEnd(firstLeftEnd);
        second.setLeftEnd(firstRightEnd);

        double firstWidth  = first.getWidth(sizeOf1Meter);
        double secondWidth = second.getWidth(sizeOf1Meter);
        // No miter limit is applied to a two-road bend, so the overrun is NaN.
        List<Corner> corners = List.of(
                new Corner(firstRightEnd, CornerType.MITER, firstWidth, secondWidth, Double.NaN),
                new Corner(firstLeftEnd, CornerType.MITER, firstWidth, secondWidth, Double.NaN));

        return new IntersectionGeometry(2, List.of(firstRightEnd, firstLeftEnd), corners);
    }

    private IntersectionGeometry generateIntersectionPolygon(Set<RoadAspect> roads) {
        List<RoadAspect> sortedRoads = sortRoadsCCW(roads);
        int degree = sortedRoads.size();
        List<Corner> corners = new ArrayList<>();

        for (int i = 0; i < degree; i++) {
            RoadAspect prev = sortedRoads.get((i - 1 + degree) % degree);
            RoadAspect curr = sortedRoads.get(i);
            RoadAspect next = sortedRoads.get((i + 1) % degree);

            Corner right = computeCorner(curr, prev, false);
            Corner left  = computeCorner(curr, next, true);
            curr.setRightEnd(right.point());
            curr.setLeftEnd(left.point());
            corners.add(right);
            corners.add(left);
        }

        return new IntersectionGeometry(degree, collectVertices(sortedRoads), corners);
    }

    private List<RoadAspect> sortRoadsCCW(Collection<RoadAspect> roads) {
        return roads.stream().sorted(Comparator.comparingDouble(road -> {
            Point2D farPoint = road.getFarPoint();
            Vector2D roadVector = farPoint.minus(road.getCenterPoint());
            return Math.atan2(roadVector.getY(), roadVector.getX());
        })).toList();
    }

    private Corner computeCorner(RoadAspect own, RoadAspect other, boolean isLeft) {
        double ownWidth   = own.getWidth(sizeOf1Meter);
        double otherWidth = other.getWidth(sizeOf1Meter);
        Line2D ownBoundary = own.getBoundaryLine(sizeOf1Meter, isLeft);

        if (isStraight(own, other)) {
            return new Corner(setbackPoint(ownBoundary, ownWidth),
                    CornerType.STRAIGHT, ownWidth, otherWidth, Double.NaN);
        }

        Line2D otherBoundary = other.getBoundaryLine(sizeOf1Meter, !isLeft);
        Point2D miterCorner = intersectOrThrow(ownBoundary, otherBoundary);
        double miterDistanceLimit = computeMiterDistanceLimit(ownBoundary, otherBoundary, ownWidth, otherWidth);
        Vector2D cornerOffset = miterCorner.minus(own.getCenterPoint());
        double overrun = cornerOffset.getLength() / miterDistanceLimit;

        if (miterDistanceLimit < cornerOffset.getLength()) {
            Point2D clamped = own.getCenterPoint().plus(cornerOffset.normalised().scale(miterDistanceLimit));
            return new Corner(clamped, CornerType.CLAMPED, ownWidth, otherWidth, overrun);
        }
        return new Corner(miterCorner, CornerType.MITER, ownWidth, otherWidth, overrun);
    }

    private boolean isStraight(RoadAspect first, RoadAspect second) {
        double angleBetweenRoads = GeometryTools2D.getAngleBetweenVectors(first.getVector(), second.getVector());
        return Math.abs(angleBetweenRoads - 180) < STRAIGHT_ANGLE_TOLERANCE_DEGREES;
    }

    private Point2D setbackPoint(Line2D boundaryLine, double roadWidth) {
        double defaultLength = roadWidth * SETBACK_COEFFICIENT;
        double lengthLimit = boundaryLine.getLength() * BOUNDARY_LENGTH_LIMIT_COEFFICIENT;
        double setbackLength = Math.min(lengthLimit, defaultLength);
        return boundaryLine.getOrigin().plus(boundaryLine.getDirection().normalised().scale(setbackLength));
    }

    private Point2D intersectOrThrow(Line2D first, Line2D second) {
        return Objects.requireNonNull(GeometryTools2D.getIntersectionPoint(first, second),
                "Expect boundary lines to intersect but they were parallel: " + first + ", " + second);
    }

    private double computeMiterDistanceLimit(Line2D ownBoundary, Line2D otherBoundary,
                                             double ownWidth, double otherWidth) {
        double widthBasedLimit = Math.max(ownWidth, otherWidth) * MITER_DISTANCE_LIMIT_COEFFICIENT;
        double boundaryLengthBasedLimit =
                Math.min(ownBoundary.getLength(), otherBoundary.getLength()) * BOUNDARY_LENGTH_LIMIT_COEFFICIENT;
        return Math.min(widthBasedLimit, boundaryLengthBasedLimit);
    }

    private List<Point2D> collectVertices(Collection<RoadAspect> roads) {
        return roads.stream().flatMap(road -> {
            Line2D boundary = road.getMouthBoundary();
            return boundary == null ? Stream.empty() : Stream.of(boundary.getOrigin(), boundary.getEndPoint());
        }).toList();
    }

    private void visualizeResults() {
        StepVisualizer.create(debug)
                .title("Generate Intersection Areas")
                .layer(LineLayer.of(map.getOSMRoads())
                        .name("OSM Roads")
                        .color(DebugPalette.SLATE_STROKE))
                .layer(PointLayer.of(map.getOSMIntersections())
                        .name("OSM Intersections")
                        .color(DebugPalette.SLATE_STROKE))
                .layer(PolygonLayer.of(map.getOSMIntersections())
                        .name("Generated Intersection Polygons")
                        .outlineColor(DebugPalette.MOSS_STROKE)
                        .fillColor(DebugPalette.MOSS_FILL))
                .show();
    }

    // Logs one line per intersection and one line per corner in a machine-readable format.
    // Every line carries the OSM node ID so that corner lines can be joined to their intersection.
    private void logIntersection(OSMIntersectionInfo intersection, IntersectionGeometry geometry) {
        long nodeId = intersection.getNode().getId();
        Point2D center = intersection.getPoint();
        double maxWidthRatio = geometry.corners().stream()
                .mapToDouble(Corner::widthRatio)
                .max()
                .orElse(Double.NaN);
        LOGGER.info("INTERSECTION_RESULT osm_node_id={} x={} y={} degree={} vertex_count={} "
                + "corner_count={} max_width_ratio={}",
                nodeId, center.getX(), center.getY(), geometry.degree(),
                geometry.vertices().size(), geometry.corners().size(), maxWidthRatio);

        geometry.corners().forEach(corner -> LOGGER.info(
                "INTERSECTION_CORNER type={} own_width_m={} other_width_m={} "
                + "width_ratio={} miter_overrun={}",
                corner.type(), corner.ownWidth() / sizeOf1Meter, corner.otherWidth() / sizeOf1Meter,
                corner.widthRatio(), corner.miterOverrun()));
    }

    // Logs aggregated corner statistics for the whole step.
    private void logSummary(List<IntersectionGeometry> results) {
        List<Corner> corners = results.stream().flatMap(r -> r.corners().stream()).toList();
        Map<CornerType, Long> typeCounts = corners.stream().collect(Collectors.groupingBy(
                Corner::type, () -> new EnumMap<>(CornerType.class), Collectors.counting()));

        LOGGER.info("INTERSECTION_SUMMARY intersections={} corners={} straight={} miter={} clamped={}",
                results.size(), corners.size(),
                typeCounts.getOrDefault(CornerType.STRAIGHT, 0L),
                typeCounts.getOrDefault(CornerType.MITER, 0L),
                typeCounts.getOrDefault(CornerType.CLAMPED, 0L));
    }
}
