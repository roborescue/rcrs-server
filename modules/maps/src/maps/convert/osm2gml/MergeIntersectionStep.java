package maps.convert.osm2gml;

import maps.osm.OSMNode;
import rescuecore2.log.Logger;
import rescuecore2.misc.geometry.Point2D;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class MergeIntersectionStep extends BaseSimplificationStep {
    private static final double MERGE_DISTANCE = 10;
    private final double mergeDistance;

    public MergeIntersectionStep(final TemporaryMap map) {
        super(map);
        this.mergeDistance = ConvertTools.sizeOfMeters(map.getOSMMap(), MERGE_DISTANCE);
    }

    @Override
    public String getDescription() {
        return "Merging nearby intersections";
    }

    @Override
    protected void step() {
        final Set<OSMIntersectionInfo> intersections = new HashSet<>(map.getOSMIntersectionInfo());

        if (intersections.size() < 2) {
            setStatus("Not enough intersections to merge.");
            return;
        }

        final Set<Set<OSMIntersectionInfo>> clusters = clusterIntersections(intersections);
        final Map<OSMIntersectionInfo, OSMIntersectionInfo> intersectionToCentroid =
                mapIntersectionsToCentroids(clusters);
        final List<OSMIntersectionInfo> centroids = intersectionToCentroid.values().stream()
                .distinct().toList();

        intersections.forEach(OSMIntersectionInfo::clearRoadSegments);

        final Set<OSMRoadInfo> roads = new HashSet<>(map.getOSMRoadInfo());
        final Set<OSMRoadInfo> mergedRoads = createMergedRoads(roads, intersectionToCentroid);//new HashSet<>();

        map.setOSMInfo(centroids, mergedRoads, map.getOSMBuildingInfo());
        visualizeNetworkDifference(intersections, roads, map.getOSMIntersectionInfo(), map.getOSMRoadInfo(), "Intersection Merging Results");
        String status = "Merged " + intersections.size() + " intersections into " + centroids.size();
        setStatus(status);
        Logger.info(status);
    }

    private Set<Set<OSMIntersectionInfo>> clusterIntersections(final Set<OSMIntersectionInfo> intersections) {
        final Set<Set<OSMIntersectionInfo>> clusters = new HashSet<>();
        final Set<OSMIntersectionInfo> visited = new HashSet<>();

        for (final OSMIntersectionInfo intersection : intersections) {
            if (visited.contains(intersection)) continue;

            final Set<OSMIntersectionInfo> cluster = new HashSet<>();
            final Queue<OSMIntersectionInfo> queue = new ArrayDeque<>();
            queue.offer(intersection);
            visited.add(intersection);

            while (!queue.isEmpty()) {
                final OSMIntersectionInfo current = queue.poll();
                cluster.add(current);

                for (final OSMIntersectionInfo neighbor : intersections) {
                    if (visited.contains(neighbor)) continue;
                    if (exceedMergeDistance(current, neighbor)) continue;
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
            clusters.add(cluster);
        }
        return clusters;
    }

    private Map<OSMIntersectionInfo, OSMIntersectionInfo> mapIntersectionsToCentroids(
            final Set<Set<OSMIntersectionInfo>> clusters) {
        return clusters.stream()
                .flatMap(cluster -> {
                    final OSMIntersectionInfo centroid = createCentroidIntersection(cluster);
                    return cluster.stream()
                            .map(intersection -> Map.entry(intersection, centroid));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Set<OSMRoadInfo> createMergedRoads(
            final Set<OSMRoadInfo> roads,
            final Map<OSMIntersectionInfo, OSMIntersectionInfo> intersectionToCentroid) {
        final Set<OSMRoadInfo> mergedRoads = new HashSet<>();
        final Map<OSMIntersectionInfo, OSMIntersectionInfo> connectedCentroids = new HashMap<>();

        for (final OSMRoadInfo road : roads) {
            final OSMIntersectionInfo startIntersection = map.getRoadStartIntersection(road);
            final OSMIntersectionInfo endIntersection = map.getRoadEndIntersection(road);
            final OSMIntersectionInfo startCentroid = intersectionToCentroid.get(startIntersection);
            final OSMIntersectionInfo endCentroid = intersectionToCentroid.get(endIntersection);

            final boolean isSelfLoop = startCentroid.equals(endCentroid);
            if (isSelfLoop || isAlreadyConnected(connectedCentroids, startCentroid, endCentroid)) continue;

            final OSMRoadInfo mergedRoad = new OSMRoadInfo(startCentroid.getCenter(), endCentroid.getCenter(),
                    road.getType(), road.getLaneCount());
            connectedCentroids.put(startCentroid, endCentroid);
            startCentroid.addRoadSegment(mergedRoad);
            endCentroid.addRoadSegment(mergedRoad);
            mergedRoads.add(mergedRoad);
        }
        return mergedRoads;
    }

    private boolean isAlreadyConnected(
            final Map<OSMIntersectionInfo, OSMIntersectionInfo> connectedCentroids,
            final OSMIntersectionInfo startCentroid,
            final OSMIntersectionInfo endCentroid) {
        final boolean forwardConnected = endCentroid.equals(connectedCentroids.get(startCentroid));
        final boolean backwardConnected = startCentroid.equals(connectedCentroids.get(endCentroid));
        return forwardConnected || backwardConnected;
    }

    private boolean exceedMergeDistance(final OSMIntersectionInfo first, final OSMIntersectionInfo second) {
        final Point2D point1 = first.getPoint();
        final Point2D point2 = second.getPoint();
        final double distance = point2.minus(point1).getLength();
        return mergeDistance < distance;
    }

    private OSMIntersectionInfo createCentroidIntersection(final Set<OSMIntersectionInfo> cluster) {
        double totalLon = 0, totalLat = 0;
        for (final OSMIntersectionInfo intersection : cluster) {
            Point2D point = intersection.getPoint();
            totalLon += point.getX();
            totalLat += point.getY();
        }

        final long id = -Math.abs(cluster.iterator().next().getCenter().getId());
        final double centroidLon = totalLon / cluster.size();
        final double centroidLat = totalLat / cluster.size();
        final OSMNode centroid = new OSMNode(id, centroidLat, centroidLon);
        return new OSMIntersectionInfo(centroid);
    }
}
