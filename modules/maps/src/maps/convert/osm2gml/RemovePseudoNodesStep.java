package maps.convert.osm2gml;

import maps.convert.osm2gml.debug.DebugPalette;
import maps.convert.osm2gml.debug.LineLayer;
import maps.convert.osm2gml.debug.PointLayer;
import maps.convert.osm2gml.debug.StepVisualizer;
import maps.osm.OSMNode;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;

import java.util.*;
import java.util.List;

/**
 * This step simplifies the road network by removing pseudo-nodes.
 */
public class RemovePseudoNodesStep extends BaseSimplificationStep {
    // Angle threshold in degrees. If the angle is less than this, consider it a straight line.
    private static final double STRAIGHT_LINE_ANGLE_THRESHOLD = 10.0;

    public RemovePseudoNodesStep(final TemporaryMap map) {
        super(map);
    }

    @Override
    public String getDescription() {
        return "Removing pseudo-nodes from straight roads";
    }

    @Override
    protected void step() {
        int pass = 0;
        final Set<OSMIntersectionInfo> removedIntersections = new HashSet<>();
        final Set<OSMRoadInfo> initialRoads = new HashSet<>(map.getOSMRoadInfo());

        while (pass <= 20) {
            final Set<OSMIntersectionInfo> result = processPseudoNodes();
            if (result.isEmpty()) {
                // No nodes were removed in a full pass, so the process has converged
                break;
            }

            pass++;
            removedIntersections.addAll(result);
        }

        final Set<OSMRoadInfo> removedRoads = new HashSet<>(initialRoads);
        removedRoads.removeAll(map.getOSMRoadInfo());

        final Set<OSMRoadInfo> createdRoads = new HashSet<>(map.getOSMRoadInfo());
        createdRoads.removeAll(initialRoads);

        // Safety break to prevent potential infinite loops in complex scenarios
        if (20 < pass) {
            System.err.println("Exceeded 20 passes in RemovePseudoNodesStep. Aborting.");
        }

        setStatus("Removed " + removedIntersections.size() + " pseudo-nodes.");
        visualizeResults(removedIntersections, removedRoads, createdRoads);
    }

    private Set<OSMIntersectionInfo> processPseudoNodes() {
        // We must work with copies as we will be modifying the map's lists
        final List<OSMIntersectionInfo> intersections = new ArrayList<>(map.getOSMIntersectionInfo());
        final List<OSMRoadInfo> roads = new ArrayList<>(map.getOSMRoadInfo());

        // Find all pseudo-nodes in the current graph
        final List<OSMIntersectionInfo> pseudoNodes = intersections.stream()
                .filter(intersection -> getConnectedRoads(intersection, roads).size() == 2)
                .toList();

        if (pseudoNodes.isEmpty()) return Collections.emptySet();

        final Set<OSMIntersectionInfo> removedIntersections = new HashSet<>();

        for (final OSMIntersectionInfo pseudoNode : pseudoNodes) {
            // The intersection might have been removed already as part of another merge
            if (!intersections.contains(pseudoNode)) continue;

            final List<OSMRoadInfo> connectedRoads = getConnectedRoads(pseudoNode, roads);
            if (connectedRoads.size() != 2) continue; // State changed, skip

            final OSMRoadInfo road1 = connectedRoads.get(0);
            final OSMRoadInfo road2 = connectedRoads.get(1);

            if (!isStraight(pseudoNode, road1, road2)) continue;

            final OSMNode nodeToRemove = pseudoNode.getUnderlyingNode();
            final OSMNode node1 = road1.getFrom().equals(nodeToRemove) ? road1.getTo() : road1.getFrom();
            final OSMNode node2 = road2.getFrom().equals(nodeToRemove) ? road2.getTo() : road2.getFrom();
            final OSMRoadInfo roadToCreate = new OSMRoadInfo(node1, node2, road1.getType(), road1.getLaneCount());

            roads.remove(road1);
            roads.remove(road2);
            roads.add(roadToCreate);
            intersections.remove(pseudoNode);

            removedIntersections.add(pseudoNode);
        }

        if (!removedIntersections.isEmpty()) map.setOSMInfo(intersections, roads, map.getOSMBuildingInfo());

        return removedIntersections;
    }

    private static List<OSMRoadInfo> getConnectedRoads(
            final OSMIntersectionInfo intersection, final List<OSMRoadInfo> allRoads) {
        return allRoads.stream()
                .filter(road -> {
                    final OSMNode center = intersection.getCenter();
                    return road.getFrom().equals(center) || road.getTo().equals(center);
                })
                .toList();
    }

    private boolean isStraight(OSMIntersectionInfo intersection, OSMRoadInfo road1, OSMRoadInfo road2) {
        OSMNode centre = intersection.getUnderlyingNode();
        OSMNode other1 = road1.getFrom().equals(centre) ? road1.getTo() : road1.getFrom();
        OSMNode other2 = road2.getFrom().equals(centre) ? road2.getTo() : road2.getFrom();

        Point2D pCentre = new Point2D(centre.getLongitude(), centre.getLatitude());
        Point2D p1 = new Point2D(other1.getLongitude(), other1.getLatitude());
        Point2D p2 = new Point2D(other2.getLongitude(), other2.getLatitude());

        Vector2D v1 = p1.minus(pCentre);
        Vector2D v2 = p2.minus(pCentre);

        double angle = GeometryTools2D.getAngleBetweenVectors(v1, v2);

        return Math.abs(180 - angle) < STRAIGHT_LINE_ANGLE_THRESHOLD;
    }

    private void visualizeResults(final Set<OSMIntersectionInfo> removedIntersections,
                                  final Set<OSMRoadInfo> removedRoads,
                                  final Set<OSMRoadInfo> createdRoads) {
        StepVisualizer.create(debug)
                .title("Pseudo-Nodes Removal Results")
                .layer(LineLayer.of(removedRoads)
                        .name("Removed Roads")
                        .color(DebugPalette.REMOVED_STROKE))
                .layer(PointLayer.of(removedIntersections)
                        .name("Removed Intersecions")
                        .color(DebugPalette.REMOVED_STROKE))
                .layer(LineLayer.of(createdRoads)
                        .name("Created Roads")
                        .color(DebugPalette.CREATED_STROKE))
                .backgroundLayer(LineLayer.of(map.getOSMRoadInfo())
                        .name("Roads")
                        .color(DebugPalette.MAIN_STROKE))
                .show();
    }
}
