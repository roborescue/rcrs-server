package maps.convert.osm2gml;

import maps.osm.OSMNode;
import rescuecore2.log.Logger;
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

    public RemovePseudoNodesStep(TemporaryMap map) {
        super(map);
    }

    @Override
    public String getDescription() {
        return "Removing pseudo-nodes from straight roads";
    }

    @Override
    protected void step() {
        int totalRemoved = 0;
        int pass = 0;

        // Store the initial state for the final visualization
        List<OSMIntersectionInfo> initialIntersections = new ArrayList<>(map.getOSMIntersectionInfo());
        List<OSMRoadInfo> initialRoads = new ArrayList<>(map.getOSMRoadInfo());

        while (true) {
            pass++;
            int removedInThisPass = processPseudoNodes();
            if (removedInThisPass == 0) {
                // No nodes were removed in a full pass, so the process has converged
                break;
            }
            totalRemoved += removedInThisPass;
            Logger.info("Pass " + pass + ": Removed " + removedInThisPass + " pseudo-nodes. Total removed " + totalRemoved);

            // Safety break to prevent potential infinite loops in complex scenarios
            if (20 < pass) {
                Logger.error("Exceeded 20 passes in RemovePseudoNodesStep. Aborting.");
                break;
            }
        }

        // Visualize the difference between the initial and final state
        visualizeNetworkDifference(initialIntersections, initialRoads, map.getOSMIntersectionInfo(), map.getOSMRoadInfo(), "Pseudo-Nodes Removal Results");

        setStatus("Removed " + totalRemoved + " pseudo-nodes.");
    }

    private int processPseudoNodes() {
        // We must work with copies as we will be modifying the map's lists
        List<OSMIntersectionInfo> intersections = new ArrayList<>(map.getOSMIntersectionInfo());
        List<OSMRoadInfo> roads = new ArrayList<>(map.getOSMRoadInfo());
        int removedCount = 0;

        // Find all pseudo-nodes in the current graph
        List<OSMIntersectionInfo> pseudoNodes = new ArrayList<>();
        for (OSMIntersectionInfo intersection : intersections) {
            if (getConnectedRoads(intersection, roads).size() == 2) {
                pseudoNodes.add(intersection);
            }
        }

        if (pseudoNodes.isEmpty()) {
            return 0;
        }

        for (OSMIntersectionInfo intersection : pseudoNodes) {
            // The intersection might have been removed already as part of another merge
            if (!intersections.contains(intersection)) {
                continue;
            }

            List<OSMRoadInfo> connectedRoads = getConnectedRoads(intersection, roads);
            if (connectedRoads.size() != 2) continue; // State changed, skip

            OSMRoadInfo road1 = connectedRoads.get(0);
            OSMRoadInfo road2 = connectedRoads.get(1);

            if (isStraight(intersection, road1, road2)) {
                OSMNode nodeToRemove = intersection.getUnderlyingNode();
                OSMNode node1 = road1.getFrom().equals(nodeToRemove) ? road1.getTo() : road1.getFrom();
                OSMNode node2 = road2.getFrom().equals(nodeToRemove) ? road2.getTo() : road2.getFrom();

                // Create a new road connecting the outer nodes
                OSMRoadInfo newRoad = new OSMRoadInfo(node1, node2, road1.getType(), road1.getLaneCount());

                // Remove old entities and add the new one
                roads.remove(road1);
                roads.remove(road2);
                roads.add(newRoad);
                intersections.remove(intersection);

                removedCount++;
            }
        }

        if (0 < removedCount) {
            map.setOSMInfo(intersections, roads, map.getOSMBuildingInfo());
        }

        return removedCount;
    }

    private List<OSMRoadInfo> getConnectedRoads(OSMIntersectionInfo intersection, List<OSMRoadInfo> allRoads) {
        List<OSMRoadInfo> result = new ArrayList<>();
        OSMNode intersectionNode = intersection.getUnderlyingNode();
        for (OSMRoadInfo road : allRoads) {
            if (road.getFrom().equals(intersectionNode) || road.getTo().equals(intersectionNode)) {
                result.add(road);
            }
        }
        return result;
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
}
