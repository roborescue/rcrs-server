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

    public GenerateIntersectionAreaStep(final TemporaryMap map) {
        this.map = map;
    }

    @Override
    public String getDescription() {
        return "Generating intersection areas";
    }

    @Override
    protected void step() {
        final Collection<OSMIntersectionInfo> intersections = map.getOSMIntersectionInfo();
        intersections.forEach(this::computeIntersectionGeometry);
        setStatus("Generated polygon areas for " + intersections.size() + " intersections");
        visualizeResults();
    }

    private void computeIntersectionGeometry(final OSMIntersectionInfo intersection) {
        final Set<RoadAspect> roads = intersection.getRoads();
        final Iterator<RoadAspect> it = roads.iterator();
        final int degree = roads.size();

        if (degree == 0) {
            bumpProgress();
            return;
        }
        if (degree == 1) {
            processDeadEnd(it.next());
            bumpProgress();
            return;
        }

        final List<Point2D> vertices = generateIntersectionPolygon(roads);
        intersection.setVertices(vertices);
        bumpProgress();
    }

    private void processDeadEnd(final RoadAspect road) {
        final Point2D centerPoint = road.getCenterPoint();
        final Point2D farPoint = road.getFarPoint();
        final Vector2D roadVector = farPoint.minus(centerPoint);
        final Vector2D normalVector = roadVector.getNormal().normalised();
        final double sizeOf1Meter = ConvertTools.sizeOf1Metre(map.getOSMMap());
        final double halfWidth = road.getRoadWidth() * sizeOf1Meter / 2;

        final Point2D rightEnd = centerPoint.plus(normalVector.scale(-halfWidth));
        final Point2D leftEnd = centerPoint.plus(normalVector.scale(halfWidth));
        road.setRightEnd(rightEnd);
        road.setLeftEnd(leftEnd);
    }

    private List<Point2D> generateIntersectionPolygon(final Set<RoadAspect> roads) {
        final List<RoadAspect> sortedRoads = sortRoadsCCW(roads);
        final double mouthDistance = calculateMouthDistance(roads);
        final int degree = sortedRoads.size();

        for (int i = 0; i < degree; i++) {
            final RoadAspect prev = sortedRoads.get((i - 1 + degree) % degree);
            final RoadAspect curr = sortedRoads.get(i);
            final RoadAspect next = sortedRoads.get((i + 1) % degree);
            final double sizeOf1Meter = ConvertTools.sizeOf1Metre(map.getOSMMap());

            final Point2D centerPoint = curr.getCenterPoint();
            final Point2D farPoint = curr.getFarPoint();
            final Vector2D roadVector = farPoint.minus(centerPoint).normalised();
            final Vector2D normalVector = roadVector.getNormal().normalised();
            final double halfWidth = curr.getRoadWidth() * sizeOf1Meter / 2;

            final Point2D mouseCenter = centerPoint.plus(roadVector.scale(mouthDistance));
            final Point2D rightEnd = mouseCenter.plus(normalVector.scale(-halfWidth));
            final Point2D leftEnd = mouseCenter.plus(normalVector.scale(halfWidth));
            curr.setRightEnd(rightEnd);
            curr.setLeftEnd(leftEnd);
            if (i < 1) continue;

            final Line2D prevBoundary = prev.getMouseBoundary();
            final Line2D currBoundary = curr.getMouseBoundary();
            final Point2D rightIntersection = GeometryTools2D.getSegmentIntersectionPoint(currBoundary, prevBoundary);
            if (rightIntersection != null) {
                curr.setRightEnd(rightIntersection);
                prev.setLeftEnd(rightIntersection);
            }
            if (i < degree - 1) continue;

            final Line2D nextBoundary = next.getMouseBoundary();
            final Point2D leftIntersection = GeometryTools2D.getSegmentIntersectionPoint(currBoundary, nextBoundary);
            if (leftIntersection != null) {
                curr.setLeftEnd(leftIntersection);
                next.setRightEnd(leftIntersection);
            }
        }

        return sortedRoads.stream().flatMap(road -> {
            final Line2D boundary = road.getMouseBoundary();
            return boundary == null ? Stream.empty() : Stream.of(boundary.getOrigin(), boundary.getEndPoint());
        }).toList();
    }

    private double calculateMouthDistance(final Set<RoadAspect> roads) {
        final double maxRoadWidth = roads.stream().mapToDouble(RoadAspect::getRoadWidth).max().orElseThrow();
        return ConvertTools.sizeOfMeters(map.getOSMMap(), maxRoadWidth) * 0.75;
    }

    private List<RoadAspect> sortRoadsCCW(final Collection<RoadAspect> roads) {
        return roads.stream().sorted(Comparator.comparingDouble(road -> {
            final Point2D farPoint = road.getFarPoint();
            final Vector2D roadVector = farPoint.minus(road.getCenterPoint());
            return Math.atan2(roadVector.getY(), roadVector.getX());
        })).toList();
    }

    private void visualizeResults() {
        StepVisualizer.create(debug)
                .title("Generate Intersection Areas")
                .layer(LineLayer.of(map.getOSMRoadInfo())
                        .name("OSM Roads")
                        .color(DebugPalette.MAIN_STROKE))
                .layer(PointLayer.of(map.getOSMIntersectionInfo())
                        .name("OSM Intersections")
                        .color(DebugPalette.MAIN_STROKE))
                .layer(PolygonLayer.of(map.getOSMIntersectionInfo())
                        .name("Generated Intersection Polygons")
                        .outlineColor(DebugPalette.CREATED_STROKE)
                        .fillColor(DebugPalette.CREATED_FILL))
                .show();
    }
}
