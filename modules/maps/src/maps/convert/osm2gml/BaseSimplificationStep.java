package maps.convert.osm2gml;

import maps.convert.ConvertStep;
import maps.gml.debug.CustomStrokeLineInfo;
import maps.gml.debug.OSMNodeShapeInfo;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.gui.ShapeDebugFrame;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * An abstract base class for simplification steps that provides common visualization logic.
 */
public abstract class BaseSimplificationStep extends ConvertStep {
    protected final TemporaryMap map;

    public BaseSimplificationStep(TemporaryMap map) {
        super();
        this.map = map;
    }

    protected void visualizeNetworkDifference(Collection<OSMIntersectionInfo> beforeIntersections, Collection<OSMRoadInfo> beforeRoads, Collection<OSMIntersectionInfo> afterIntersections, Collection<OSMRoadInfo> afterRoads, String title) {
        List<ShapeDebugFrame.ShapeInfo> shapes = new ArrayList<>();
        Set<OSMRoadInfo> beforeRoadSet = new HashSet<>(beforeRoads);
        Set<OSMRoadInfo> afterRoadSet = new HashSet<>(afterRoads);
        Set<OSMIntersectionInfo> beforeIntersectionSet = new HashSet<>(beforeIntersections);
        Set<OSMIntersectionInfo> afterIntersectionSet = new HashSet<>(afterIntersections);

        final Color colourForOld = Color.decode("#E57373");
        final Color colourForNew = Color.decode("#1976d2");

        // Kept items (draw in black)
        Set<OSMRoadInfo> keptRoads = new HashSet<>(beforeRoads);
        Set<OSMIntersectionInfo> keptIntersections = new HashSet<>(beforeIntersections);
        keptRoads.retainAll(afterRoadSet);
        keptIntersections.retainAll(afterIntersectionSet);
        drawRoads(shapes, keptRoads, "Kept Road", Color.BLACK);
        drawIntersections(shapes, keptIntersections, "Kept Intersections", Color.BLACK, true);

        // Removed items (draw in red, dashed)
        Set<OSMRoadInfo> removedRoads = new HashSet<>(beforeRoadSet);
        Set<OSMIntersectionInfo> removedIntersections = new HashSet<>(beforeIntersectionSet);
        removedRoads.removeAll(afterRoadSet);
        removedIntersections.removeAll(afterIntersectionSet);
        drawRoads(shapes, removedRoads, "Removed Road", colourForOld, new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{5.0f}, 0.0f));
        drawIntersections(shapes, removedIntersections, "Removed Intersection", colourForOld, true);

        // Added items (draw in blue, thick)
        Set<OSMRoadInfo> addedRoads = new HashSet<>(afterRoads);
        Set<OSMIntersectionInfo> addedIntersections = new HashSet<>(afterIntersectionSet);
        addedRoads.removeAll(beforeRoadSet);
        addedIntersections.removeAll(beforeIntersections);
        drawRoads(shapes, addedRoads, "Added Road", colourForNew, new BasicStroke(2.0f));
        drawIntersections(shapes, addedIntersections, "Added Intersection", colourForNew, true);

        debug.show(title, shapes);
    }

    private void drawRoads(List<ShapeDebugFrame.ShapeInfo> shapes, Collection<OSMRoadInfo> roads, String name, Color colour, Stroke stroke) {
        for (OSMRoadInfo road : roads) {
            if (road.getFrom() == null || road.getTo() == null) continue;
            Line2D line = new Line2D(
                    new Point2D(road.getFrom().getLongitude(), road.getFrom().getLatitude()),
                    new Point2D(road.getTo().getLongitude(), road.getTo().getLatitude())
            );
            shapes.add(new CustomStrokeLineInfo(line, name, colour, stroke));
        }
    }

    private void drawRoads(List<ShapeDebugFrame.ShapeInfo> shapes, Collection<OSMRoadInfo> roads, String name, Color color) {
        drawRoads(shapes, roads, name, color, new BasicStroke(1.0f));
    }

    private void drawIntersections(List<ShapeDebugFrame.ShapeInfo> shapes, Collection<OSMIntersectionInfo> intersections, String name, Color colour, boolean square) {
        for (OSMIntersectionInfo intersection : intersections) {
            shapes.add(new OSMNodeShapeInfo(intersection.getNode(), name, colour, square));
        }
    }
}
