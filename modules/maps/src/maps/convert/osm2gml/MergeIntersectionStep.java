package maps.convert.osm2gml;

import maps.convert.ConvertStep;
import maps.convert.osm2gml.debug.DebugPalette;
import maps.convert.osm2gml.debug.LineLayer;
import maps.convert.osm2gml.debug.PointLayer;
import maps.convert.osm2gml.debug.StepVisualizer;
import maps.osm.OSMNode;
import rescuecore2.misc.geometry.Point2D;

import java.util.*;
import java.util.stream.Collectors;

public class MergeIntersectionStep extends ConvertStep {
    private final TemporaryMap map;
    private final double mergeDistance;
    private static final double MERGE_DISTANCE = 10;

    public MergeIntersectionStep(final TemporaryMap map) {
        this.map = map;
        this.mergeDistance = ConvertTools.sizeOfMeters(map.getOSMMap(), MERGE_DISTANCE);
    }

    @Override
    public String getDescription() {
        return "Merging nearby intersections";
    }

    @Override
    protected void step() {
        final Set<OSMIntersectionInfo> intersections = new HashSet<>(map.getOSMIntersections());

        if (intersections.size() < 2) {
            setStatus("Not enough intersections to merge.");
            return;
        }

        final Set<Set<OSMIntersectionInfo>> clusters = clusterIntersections(intersections);
        final Map<OSMIntersectionInfo, OSMIntersectionInfo> replacementMap = mapIntersectionsToCentroids(clusters);

        final Set<OSMIntersectionInfo> createdIntersections = new HashSet<>();
        for (final OSMIntersectionInfo intersectionToCreate : replacementMap.values()) {
            map.addOSMIntersection(intersectionToCreate);
            createdIntersections.add(intersectionToCreate);
        }

        final Set<OSMRoadInfo> currentRoads = new HashSet<>(map.getOSMRoads());
        final Set<OSMRoadInfo> removedRoads = new HashSet<>();
        final Set<OSMRoadInfo> createdRoads = new HashSet<>();
        for (final OSMRoadInfo road : currentRoads) {
            final OSMIntersectionInfo startInt = map.getOSMIntersection(road.getFrom());
            final OSMIntersectionInfo endInt = map.getOSMIntersection(road.getTo());

            final OSMIntersectionInfo newStart = replacementMap.getOrDefault(startInt, startInt);
            final OSMIntersectionInfo newEnd = replacementMap.getOrDefault(endInt, endInt);

            if (newStart.equals(startInt) && newEnd.equals(endInt)) continue;

            map.removeOSMRoad(road);
            removedRoads.add(road);
            if (newStart.equals(newEnd)) continue;

            final OSMRoadInfo roadToCreate =
                    new OSMRoadInfo(newStart.getNode(), newEnd.getNode(), road.getType(), road.getLaneCount());
            map.addOSMRoad(roadToCreate);
            createdRoads.add(roadToCreate);
        }

        final Set<OSMIntersectionInfo> removedIntersections = new HashSet<>();
        for (final OSMIntersectionInfo intersectionToRemove : replacementMap.keySet()) {
            map.removeOSMIntersection(intersectionToRemove);
            removedIntersections.add(intersectionToRemove);
        }

        setStatus("Merged " + intersections.size() + " intersections into " + clusters.size());
        visualizeResults(removedRoads, createdRoads, removedIntersections, createdIntersections);
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
            if (cluster.size() == 1) continue;

            clusters.add(cluster);
        }
        return clusters;
    }

    private boolean exceedMergeDistance(final OSMIntersectionInfo first, final OSMIntersectionInfo second) {
        final Point2D point1 = first.getPoint();
        final Point2D point2 = second.getPoint();
        final double distance = point2.minus(point1).getLength();
        return mergeDistance < distance;
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

    private OSMIntersectionInfo createCentroidIntersection(final Set<OSMIntersectionInfo> cluster) {
        double totalLon = 0, totalLat = 0;
        for (final OSMIntersectionInfo intersection : cluster) {
            Point2D point = intersection.getPoint();
            totalLon += point.getX();
            totalLat += point.getY();
        }

        final long id = -Math.abs(cluster.iterator().next().getNode().getId());
        final double centroidLon = totalLon / cluster.size();
        final double centroidLat = totalLat / cluster.size();
        final OSMNode centroid = new OSMNode(id, centroidLat, centroidLon);
        return new OSMIntersectionInfo(centroid);
    }

    private void visualizeResults(
            final Set<OSMRoadInfo> removedRoads, final Set<OSMRoadInfo> createdRoads,
            final Set<OSMIntersectionInfo> removedIntersections, final Set<OSMIntersectionInfo> createdIntersections) {

        StepVisualizer.create(debug)
                .title("Intersection Merging Results")
                .backgroundLayer(LineLayer.of(map.getOSMRoads())
                        .name("OSM Roads")
                        .color(DebugPalette.MAIN_STROKE))
                .layer(LineLayer.of(removedRoads)
                        .name("Removed Roads")
                        .color(DebugPalette.REMOVED_STROKE))
                .layer(LineLayer.of(createdRoads)
                        .name("Created Roads")
                        .color(DebugPalette.CREATED_STROKE))
                .layer(PointLayer.of(removedIntersections)
                        .name("Removed Intersections")
                        .color(DebugPalette.REMOVED_STROKE))
                .layer(PointLayer.of(createdIntersections)
                        .name("Created Intersections")
                        .color(DebugPalette.CREATED_STROKE))
                .show();
    }
}
