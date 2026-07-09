package maps.convert.osm2gml;

import maps.osm.OSMMap;
import maps.osm.OSMNode;
import maps.osm.OSMRoad;
import maps.osm.OSMBuilding;

import maps.convert.ConvertStep;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

/**
   This step scans the OpenStreetMap data and generates information about roads, intersections and buildings.
*/
public class ScanOSMStep extends ConvertStep {
    private final TemporaryMap map;
    private Map<OSMNode, OSMIntersectionInfo> nodeToIntersection;
    private List<OSMIntersectionInfo> intersections;
    private List<OSMRoadInfo> roads;
    private List<OSMBuildingInfo> buildings;

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
        // Initialize local collections
        nodeToIntersection = new HashMap<>();
        intersections = new ArrayList<>();
        roads = new ArrayList<>();
        buildings = new ArrayList<>();
        OSMMap osm = map.getOSMMap();

        setProgressLimit(osm.getRoads().size() + osm.getBuildings().size());
        setStatus("Scanning OSM data to build road graph");

        // Scan roads to build the graph structure (populates this.intersections and this.roads)
        scanRoads();

        // Scan buildings
        scanBuildings();

        setStatus("Created " + roads.size() + " roads, " + intersections.size() + " intersections, " + buildings.size() + " buildings");
        map.setOSMInfo(intersections, roads, buildings);
    }

    private void scanRoads() {
        OSMMap osm = map.getOSMMap();
        for (OSMRoad road : osm.getRoads()) {
            Iterator<Long> it = road.getNodeIDs().iterator();
            OSMNode start = osm.getNode(it.next());
            while (it.hasNext()) {
                OSMNode end = osm.getNode(it.next());
                if (start == end) {
                    System.out.println("Degenerate road: " + road.getID());
                    continue;
                }
                OSMIntersectionInfo from = nodeToIntersection.get(start);
                OSMIntersectionInfo to = nodeToIntersection.get(end);
                if (from == null) {
                    from = new OSMIntersectionInfo(start);
                    nodeToIntersection.put(start, from);
                    intersections.add(from);
                }
                if (to == null) {
                    to = new OSMIntersectionInfo(end);
                    nodeToIntersection.put(end, to);
                    intersections.add(to);
                }
                OSMRoadInfo roadInfo = new OSMRoadInfo(start, end, road.getType(), road.getLaneCount());
                from.addRoadSegment(roadInfo);
                to.addRoadSegment(roadInfo);
                start = end;
                roads.add(roadInfo);
            }
            bumpProgress();
        }
    }

    private void scanBuildings() {
        OSMMap osm = map.getOSMMap();
        for (OSMBuilding building : osm.getBuildings()) {
            buildings.add(new OSMBuildingInfo(building, osm));
            bumpProgress();
        }
    }
}
