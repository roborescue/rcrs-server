package maps.convert.osm2gml;

import maps.osm.OSMNode;
import rescuecore2.log.Logger;
import rescuecore2.misc.geometry.Point2D;

import java.util.*;
import java.util.List;

public class MergeIntersectionStep extends BaseSimplificationStep {
    private final double mergeDistance;

    public MergeIntersectionStep(TemporaryMap map) {
        super(map);

        // Merge intersection within 10 meters of each other.
        this.mergeDistance = ConvertTools.sizeOf1Metre(map.getOSMMap()) * 10.0;
    }

    @Override
    public String getDescription() {
        return "Merging nearby intersections";
    }

    @Override
    protected void step() {
        List<OSMIntersectionInfo> initialIntersections = new ArrayList<>(map.getOSMIntersectionInfo());
        Collection<OSMRoadInfo> initialRoads = new ArrayList<>(map.getOSMRoadInfo());

        if (initialIntersections.size() < 2) {
            setStatus("Not enough intersections to merge.");
            return;
        }

        // Group nearby intersections.
        List<Set<OSMIntersectionInfo>> groupedIntersections = groupIntersections(initialIntersections);

        // Create new centroid intersections and create a map from old to new.
        List<OSMIntersectionInfo> newIntersections = new ArrayList<>();
        Map<OSMIntersectionInfo, OSMIntersectionInfo> oldToNewMap = new HashMap<>();

        for (Set<OSMIntersectionInfo> group : groupedIntersections) {
            OSMIntersectionInfo newCentroid = createCentroidIntersection(group);
            newIntersections.add(newCentroid);
            for (OSMIntersectionInfo old : group) {
                oldToNewMap.put(old, newCentroid);
            }
        }

        // Clear the internal state of all new intersections before rebuilding.
        for (OSMIntersectionInfo intersection : newIntersections) {
            intersection.clearRoadSegments();
        }

        // Re-link all roads to the new centroid intersections.
        Set<OSMRoadInfo> finalRoads = new HashSet<>();
        for (OSMRoadInfo road : initialRoads) {
            OSMIntersectionInfo oldStart = map.getRoadStartIntersection(road);
            OSMIntersectionInfo oldEnd = map.getRoadEndIntersection(road);

            OSMIntersectionInfo newStart = oldToNewMap.get(oldStart);
            OSMIntersectionInfo newEnd = oldToNewMap.get(oldEnd);

            // Discard invalid roads or roads that were internal to a merged group.
            if (newStart == null || newEnd == null || newStart.getUnderlyingNode().equals(newEnd.getUnderlyingNode())) {
                continue;
            }

            final OSMRoadInfo createdRoad = new OSMRoadInfo(newStart.getUnderlyingNode(), newEnd.getUnderlyingNode(),
                    road.getType(), road.getLaneCount());
            newStart.addRoadSegment(createdRoad);
            newEnd.addRoadSegment(createdRoad);
            finalRoads.add(createdRoad);
        }

        // Update the map with the simplified graph.
        map.setOSMInfo(newIntersections, new ArrayList<>(finalRoads), map.getOSMBuildingInfo());

        visualizeNetworkDifference(initialIntersections, initialRoads, map.getOSMIntersectionInfo(), map.getOSMRoadInfo(), "Intersection Merging Results");

        String status = "Merged " + initialIntersections.size() + " intersections into " + newIntersections.size();
        setStatus(status);
        Logger.info(status);
    }

    private List<Set<OSMIntersectionInfo>> groupIntersections(List<OSMIntersectionInfo> intersections) {
        List<Set<OSMIntersectionInfo>> groups = new ArrayList<>();
        Set<OSMIntersectionInfo> visited = new HashSet<>();
        for (OSMIntersectionInfo startNode : intersections) {
            if (visited.contains(startNode)) continue;

            Set<OSMIntersectionInfo> currentGroup = new HashSet<>();
            Queue<OSMIntersectionInfo> queue = new ArrayDeque<>();
            queue.add(startNode);
            visited.add(startNode);

            while (!queue.isEmpty()) {
                OSMIntersectionInfo current = queue.poll();
                currentGroup.add(current);
                for (OSMIntersectionInfo neighbour : intersections) {
                    if (!visited.contains(neighbour) && isNear(current.getLocation(), neighbour.getLocation())) {
                        visited.add(neighbour);
                        queue.add(neighbour);
                    }
                }
            }
            groups.add(currentGroup);
        }
        return groups;
    }

    private OSMIntersectionInfo createCentroidIntersection(Set<OSMIntersectionInfo> group) {
        if (group.isEmpty()) return null;
        if (group.size() == 1) return group.iterator().next();

        double totalLon = 0, totalLat = 0;
        long representativeId = -1;

        for (OSMIntersectionInfo i : group) {
            Point2D loc = i.getLocation();
            totalLon += loc.getX();
            totalLat += loc.getY();
            if (representativeId == -1) representativeId = i.getUnderlyingNode().getID();
        }

        double centroidLon = totalLon / group.size();
        double centroidLat = totalLat / group.size();

        OSMNode centroidNode = new OSMNode(-Math.abs(representativeId), centroidLat, centroidLon);
        return new OSMIntersectionInfo(centroidNode);
    }

    private boolean isNear(Point2D p1, Point2D p2) {
        if (p1 == null || p2 == null) return false;
        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();
        double distSq = dx * dx + dy * dy;
        return distSq <= mergeDistance * mergeDistance;
    }
}
