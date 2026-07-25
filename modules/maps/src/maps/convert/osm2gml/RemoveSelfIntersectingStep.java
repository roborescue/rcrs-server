package maps.convert.osm2gml;

import maps.convert.ConvertStep;
import maps.convert.osm2gml.debug.DebugPalette;
import maps.convert.osm2gml.debug.PolygonLayer;
import maps.convert.osm2gml.debug.StepVisualizer;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;

import java.util.*;

public class RemoveSelfIntersectingStep extends ConvertStep {
    private final TemporaryMap map;

    public RemoveSelfIntersectingStep(TemporaryMap map) {
        this.map = map;
    }

    @Override
    public String getDescription() {
        return "Removing self-intersecting loops from shapes";
    }

    @Override
    protected void step() {
        Set<TemporaryObject> allObjects = new LinkedHashSet<>(map.getAllObjects());
        Set<TemporaryObject> removed = new LinkedHashSet<>();
        Set<TemporaryObject> created = new LinkedHashSet<>();

        setProgress(allObjects.size());

        for (TemporaryObject object : allObjects) {
            List<Node> rawNodes = object.getNodes();
            List<Node> cleanedNodes = removeSelfIntersectingEdges(rawNodes, map);
            int n = cleanedNodes.size();

            if (n < 3) {
                map.removeTemporaryObject(object);
            } else if (n != rawNodes.size()) {
                map.removeTemporaryObject(object);
                removed.add(object);
                TemporaryObject newObj = createUpdateObject(object, cleanedNodes);
                map.addTemporaryObject(newObj);
                created.add(newObj);
                bumpProgress();
            }
            bumpProgress();
        }

        visualizeResults(removed, created);
    }

    private List<Node> removeSelfIntersectingEdges(List<Node> nodes, TemporaryMap map) {
        List<Node> result = new ArrayList<>(nodes);

        while (true) {
            Intersection intersection = findSelfIntersection(result);
            if (intersection == null) {
                return result;
            }
            result = rebuildPolygon(result, intersection, map);
        }
    }

    private Intersection findSelfIntersection(List<Node> nodes) {
        int size = nodes.size();

        if (size < 4) {
            return null;
        }

        for (int first = 0; first < size; first++) {
            int firstNext = (first + 1) % size;
            Line2D edge1 = new Line2D(
                    nodes.get(first).getPoint(),
                    nodes.get(firstNext).getPoint());

            for (int second = first + 2; second < size; second++) {
                int secondNext = (second + 1) % size;

                if (first == 0 && secondNext == 0) {
                    continue;
                }

                Line2D edge2 = new Line2D(
                        nodes.get(second).getPoint(),
                        nodes.get(secondNext).getPoint());

                Point2D point = GeometryTools2D.getSegmentIntersectionPoint(edge1, edge2);
                if (point != null) {
                    return new Intersection(first, secondNext, point);
                }
            }
        }

        return null;
    }

    private List<Node> rebuildPolygon(
            List<Node> nodes,
            Intersection intersection,
            TemporaryMap map) {

        Node node = map.getNode(intersection.point());

        List<Node> result = new ArrayList<>(nodes.subList(0, intersection.firstIndex() + 1));
        result.add(node);

        if (intersection.secondNextIndex() != 0) {
            result.addAll(nodes.subList(intersection.secondNextIndex(), nodes.size()));
        }

        return result;
    }

    private record Intersection(
            int firstIndex,
            int secondNextIndex,
            Point2D point) {
    }

    private TemporaryObject createUpdateObject(TemporaryObject original, List<Node> nodes) {
        List<DirectedEdge> newEdges = new ArrayList<>();
        Node first = nodes.getFirst();
        Node previous = first;

        for (int i = 1; i < nodes.size(); i++) {
            Node n = nodes.get(i);
            if (!n.equals(previous)) {
                newEdges.add(map.getDirectedEdge(previous, n));
                previous = n;
            }
        }
        if (!previous.equals(first)) {
            newEdges.add(map.getDirectedEdge(previous, first));
        }

        return original.copyWithEdges(newEdges);
    }

    private void visualizeResults(Set<TemporaryObject> removed, Set<TemporaryObject> created) {
        StepVisualizer.create(debug)
                .title("Remove Self Interactions Results")
                .layer(PolygonLayer.of(removed)
                        .name("Removed Objects")
                        .outlineColor(DebugPalette.CORAL_STROKE)
                        .fillColor(DebugPalette.CORAL_FILL))
                .layer(PolygonLayer.of(created)
                        .name("Created Objects")
                        .outlineColor(DebugPalette.MOSS_STROKE)
                        .fillColor(DebugPalette.MOSS_FILL))
                .backgroundLayer(PolygonLayer.of(map.getAllObjects())
                        .outlineColor(DebugPalette.SLATE_STROKE)
                        .fillColor(DebugPalette.SLATE_FILL))
                .show();
    }
}
