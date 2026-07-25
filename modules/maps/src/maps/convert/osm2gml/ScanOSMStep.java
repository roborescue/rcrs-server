package maps.convert.osm2gml;

import maps.convert.osm2gml.debug.*;
import maps.osm.OSMMap;
import maps.osm.OSMNode;
import maps.osm.OSMRoad;

import maps.convert.ConvertStep;

import java.util.*;

/**
   This step scans the OpenStreetMap data and generates information about roads, intersections and buildings.
*/
public class ScanOSMStep extends ConvertStep {
    private final TemporaryMap map;

    /**
       Construct a ScanOSMStep.
       @param map The OSMMap to scan.
    */
    public ScanOSMStep(TemporaryMap map) {
        this.map = map;
    }

    @Override
    public String getDescription() {
        return "Scanning OpenStreetMap data";
    }

    @Override
    protected void step() {
        final OSMMap osm = map.getOSMMap();
        setProgressLimit(osm.getRoads().size() + osm.getBuildings().size());
        setStatus("Scanning OSM data to build road graph");

        // Scan roads to build the graph structure (populates this.intersections and this.roads)
        final ScanRoadsResults results = scanRoads(osm);

        // Scan buildings
        final Set<OSMBuildingInfo> createdBuildings = scanBuildings(osm);

        setStatus("Created " + results.createdRoads.size() + " roads, " + results.createdIntersections.size() +
                " intersections, " + createdBuildings.size() + " buildings");
        visualizeResults(results.createdRoads, results.createdIntersections, createdBuildings);
    }

    private record ScanRoadsResults(Set<OSMRoadInfo> createdRoads, Set<OSMIntersectionInfo> createdIntersections) {}

    private ScanRoadsResults scanRoads(final OSMMap osm) {
        final Set<OSMRoadInfo> createdRoads = new HashSet<>();
        final Set<OSMIntersectionInfo> createdIntersections = new HashSet<>();
        for (final OSMRoad road : osm.getRoads()) {
            final Iterator<Long> it = road.getNodeIDs().iterator();
            OSMNode start = map.getOSMMap().getNode(it.next());
            while (it.hasNext()) {
                final OSMNode end = map.getOSMMap().getNode(it.next());
                if (start.equals(end)) {
                    System.out.println("Degenerate road: " + road.getId());
                    start = end;
                    continue;
                }

                final OSMIntersectionInfo from = new OSMIntersectionInfo(start);
                final OSMIntersectionInfo to = new OSMIntersectionInfo(end);
                map.addOSMIntersection(from);
                createdIntersections.add(from);
                map.addOSMIntersection(to);
                createdIntersections.add(to);

                final OSMRoadInfo roadInfo = new OSMRoadInfo(start, end, road.getType(), road.getLaneCount());
                map.addOSMRoad(roadInfo);
                createdRoads.add(roadInfo);

                start = end;
            }
            bumpProgress();
        }

        return new ScanRoadsResults(createdRoads, createdIntersections);
    }

    private Set<OSMBuildingInfo> scanBuildings(final OSMMap osm) {
        final Set<OSMBuildingInfo> createdBuildings = new HashSet<>();
        osm.getBuildings().forEach(building -> {
            final OSMBuildingInfo buildingToCreate = new OSMBuildingInfo(building, map.getOSMMap());
            map.addOSMBuilding(buildingToCreate);
            createdBuildings.add(buildingToCreate);
            bumpProgress();
        });
        return createdBuildings;
    }

    private void visualizeResults(
            final Set<OSMRoadInfo> createdRoads, final Set<OSMIntersectionInfo> createdIntersections,
            final Set<OSMBuildingInfo> createdBuildings) {
        StepVisualizer.create(debug)
                .title("Scan OSM")
                .layer(LineLayer.of(createdRoads)
                        .name("Created Roads")
                        .color(DebugPalette.MOSS_STROKE))
                .layer(PointLayer.of(createdIntersections)
                        .name("Created Intersections")
                        .color(DebugPalette.MOSS_STROKE))
                .layer(PolygonLayer.of(createdBuildings)
                        .name("Created Buildings")
                        .outlineColor(DebugPalette.MOSS_STROKE)
                        .fillColor(DebugPalette.MOSS_FILL))
                .show();
    }
}
