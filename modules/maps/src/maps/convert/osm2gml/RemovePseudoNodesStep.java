package maps.convert.osm2gml;

import maps.convert.ConvertStep;
import maps.convert.osm2gml.debug.DebugPalette;
import maps.convert.osm2gml.debug.LineLayer;
import maps.convert.osm2gml.debug.PointLayer;
import maps.convert.osm2gml.debug.StepVisualizer;
import maps.osm.OSMNode;
import rescuecore2.misc.geometry.GeometryTools2D;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This step simplifies the road network by removing pseudo-nodes.
 */
public class RemovePseudoNodesStep extends ConvertStep {
    private final TemporaryMap map;

    // Angle threshold in degrees. If the angle is less than this, consider it a straight line.
    private static final double STRAIGHT_LINE_ANGLE_THRESHOLD = 10.0;

    public RemovePseudoNodesStep(final TemporaryMap map) {
        this.map = map;
    }

    @Override
    public String getDescription() {
        return "Removing pseudo-nodes from straight roads";
    }

    @Override
    protected void step() {
        final Deque<OSMIntersectionInfo> queue = map.getOSMIntersections().stream()
                .filter(intersection -> map.getOSMIntersectionDegree(intersection) == 2)
                .collect(Collectors.toCollection(ArrayDeque::new));

        setProgressLimit(queue.size());

        final Set<OSMRoadInfo> beforeRoads = new HashSet<>(map.getOSMRoads());
        final Set<OSMIntersectionInfo> removedIntersections = new HashSet<>();

        while (!queue.isEmpty()) {
            final OSMIntersectionInfo pseudoNode = queue.poll();

            final Set<OSMRoadInfo> connectedRoads = map.getConnectedOSMRoads(pseudoNode);
            final Iterator<OSMRoadInfo> it = connectedRoads.iterator();
            final OSMRoadInfo firstRoad = it.next();
            final OSMRoadInfo secondRoad = it.next();

            if (!isStraight(firstRoad, secondRoad)) continue;
            if (firstRoad.getType() != secondRoad.getType()) continue;
            if (firstRoad.getLaneCount() != secondRoad.getLaneCount()) continue;

            final OSMNode nodeToRemove = pseudoNode.getNode();
            final OSMNode firstNode = firstRoad.getOtherNode(nodeToRemove);
            final OSMNode secondNode = secondRoad.getOtherNode(nodeToRemove);
            final OSMRoadInfo mergedRoad = new OSMRoadInfo(
                    firstNode, secondNode, firstRoad.getType(), firstRoad.getLaneCount());

            map.removeOSMRoad(firstRoad);
            map.removeOSMRoad(secondRoad);
            map.addOSMRoad(mergedRoad);
            map.removeOSMIntersection(pseudoNode);

            removedIntersections.add(pseudoNode);

            final OSMIntersectionInfo firstIntersection = map.getOSMIntersection(firstNode);
            if (map.getOSMIntersectionDegree(firstIntersection) == 2 && !queue.contains(firstIntersection)) {
                queue.offer(firstIntersection);
            }

            final OSMIntersectionInfo secondIntersection = map.getOSMIntersection(secondNode);
            if (map.getOSMIntersectionDegree(secondIntersection) == 2 && !queue.contains(secondIntersection))
                queue.offer(secondIntersection);

            setProgress(getProgressLimit() - queue.size());
        }
        setProgress(getProgressLimit());

        final Set<OSMRoadInfo> afterRoads = new HashSet<>(map.getOSMRoads());
        final Set<OSMRoadInfo> removedRoads = new HashSet<>(beforeRoads);
        removedRoads.removeAll(afterRoads);
        final Set<OSMRoadInfo> createdRoads = new HashSet<>(afterRoads);
        createdRoads.removeAll(beforeRoads);
        setStatus("Removed " + removedIntersections.size() + " pseudo-nodes.");
        visualizeResults(removedIntersections, removedRoads, createdRoads);
    }

    private boolean isStraight(final OSMRoadInfo first, final OSMRoadInfo second) {
        double angle = GeometryTools2D.getLeftAngleBetweenLines(first.getLine(), second.getLine());
        return angle < STRAIGHT_LINE_ANGLE_THRESHOLD || 360 - STRAIGHT_LINE_ANGLE_THRESHOLD < angle;
    }

    private void visualizeResults(final Set<OSMIntersectionInfo> removedIntersections,
                                  final Set<OSMRoadInfo> removedRoads,
                                  final Set<OSMRoadInfo> createdRoads) {
        StepVisualizer.create(debug)
                .title("Pseudo-Nodes Removal Results")
                .layer(LineLayer.of(removedRoads)
                        .name("Removed Roads")
                        .color(DebugPalette.CORAL_STROKE))
                .layer(PointLayer.of(removedIntersections)
                        .name("Removed Intersecions")
                        .color(DebugPalette.CORAL_STROKE))
                .layer(LineLayer.of(createdRoads)
                        .name("Created Roads")
                        .color(DebugPalette.MOSS_STROKE))
                .backgroundLayer(LineLayer.of(map.getOSMRoads())
                        .name("Roads")
                        .color(DebugPalette.SLATE_STROKE))
                .show();
    }
}
