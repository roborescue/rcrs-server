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
        final Collection<OSMIntersectionInfo> intersections = map.getOSMIntersectionInfo();
        intersections.forEach(this::computeIntersectionGeometry);
        setStatus("Generated polygon areas for " + intersections.size() + " intersections");
        visualizeResults();
    }

    private void computeIntersectionGeometry(final OSMIntersectionInfo intersection) {
        final Set<RoadAspect> roads = intersection.getRoads();
        final int degree = roads.size();
        List<Point2D> vertices = null;
        if (degree == 0) {
            // do nothing.
        } else if (degree == 1) {
            vertices = processDeadEnd(roads.iterator().next());
        } else if (degree == 2) {
            final Iterator<RoadAspect> it = roads.iterator();
            vertices = processThroughRoad(it.next(), it.next());
        } else {
            vertices = generateIntersectionPolygon(roads);
        }
        if (vertices != null) intersection.setVertices(vertices);
        bumpProgress();
    }

    private List<Point2D> processDeadEnd(final RoadAspect road) {
        road.setRightEnd(road.getRightBoundaryLine(sizeOf1Meter).getOrigin());
        road.setLeftEnd(road.getLeftBoundaryLine(sizeOf1Meter).getOrigin());
        return collectVertices(List.of(road));
    }

    private List<Point2D> processThroughRoad(final RoadAspect first, final RoadAspect second) {
        final Point2D firstRightEnd = GeometryTools2D.getIntersectionPoint(
                first.getRightBoundaryLine(sizeOf1Meter), second.getLeftBoundaryLine(sizeOf1Meter));
        final Point2D firstLeftEnd = GeometryTools2D.getIntersectionPoint(
                first.getLeftBoundaryLine(sizeOf1Meter), second.getRightBoundaryLine(sizeOf1Meter));
        if (firstRightEnd == null || firstLeftEnd == null) {
            throw new IllegalStateException("Parallel boundary lines at a through-road node");
        }

        first.setLeftEnd(firstLeftEnd);
        first.setRightEnd(firstRightEnd);
        second.setLeftEnd(firstRightEnd);
        second.setRightEnd(firstLeftEnd);

        return List.of(firstRightEnd, firstLeftEnd);
    }

    private List<Point2D> generateIntersectionPolygon(final Set<RoadAspect> roads) {
        final List<RoadAspect> sortedRoads = sortRoadsCCW(roads);
        final int degree = sortedRoads.size();
        final double sizeOf1Meter = ConvertTools.sizeOf1Metre(map.getOSMMap());

        for (int i = 0; i < degree; i++) {
            final RoadAspect prev = sortedRoads.get((i - 1 + degree) % degree);
            final RoadAspect curr = sortedRoads.get(i);
            final RoadAspect next = sortedRoads.get((i + 1) % degree);

            final Line2D prevLeftBoundaryLine = prev.getLeftBoundaryLine(sizeOf1Meter);
            final Line2D currRightBoundaryLine = curr.getRightBoundaryLine(sizeOf1Meter);
            curr.setRightEnd(GeometryTools2D.getIntersectionPoint(prevLeftBoundaryLine, currRightBoundaryLine));

            final Line2D currLeftBoundaryLine = curr.getLeftBoundaryLine(sizeOf1Meter);
            final Line2D nextRightBoundaryLine = next.getRightBoundaryLine(sizeOf1Meter);
            curr.setLeftEnd(GeometryTools2D.getIntersectionPoint(currLeftBoundaryLine, nextRightBoundaryLine));
        }

        return collectVertices(sortedRoads);
    }

    private List<Point2D> collectVertices(final Collection<RoadAspect> roads) {
        return roads.stream().flatMap(road -> {
            final Line2D boundary = road.getMouthBoundary();
            return boundary == null ? Stream.empty() : Stream.of(boundary.getOrigin(), boundary.getEndPoint());
        }).toList();
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
