package maps.convert.osm2gml;

import maps.convert.ConvertStep;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import rescuecore2.misc.geometry.Point2D;

/**
   This step creates TemporaryObjects from the OSM data.
*/
public class MakeTempObjectsStep extends ConvertStep {
    private TemporaryMap map;

    /**
       Construct a MakeTempObjectsStep.
       @param map The TemporaryMap to populate.
    */
    public MakeTempObjectsStep(TemporaryMap map) {
        super();
        this.map = map;
    }

    @Override
    public String getDescription() {
        return "Generating temporary objects";
    }

    @Override
    protected void step() {
        Collection<OSMRoadInfo> roads = map.getOSMRoadInfo();
        Collection<OSMIntersectionInfo> intersections = map.getOSMIntersectionInfo();
        Collection<OSMBuildingInfo> buildings = map.getOSMBuildingInfo();
        setProgressLimit(roads.size() + intersections.size() + buildings.size());
        generateRoadObjects(roads);
        generateIntersectionObjects(intersections);
        generateBuildingObjects(buildings);
        setStatus("Created " + map.getRoads().size() + " roads, " + map.getIntersections().size() + " intersections, " + map.getBuildings().size() + " buildings");
    }

    private void generateRoadObjects(Collection<OSMRoadInfo> roads) {
        for (OSMRoadInfo road : roads) {
            if (road.getArea() != null) {
                List<DirectedEdge> edges = generateEdges(road);
                if (edges.size() > 2) {
                    map.addRoad(new TemporaryRoad(edges));
                }
            }
            bumpProgress();
        }
    }

    private void generateIntersectionObjects(Collection<OSMIntersectionInfo> intersections) {
        for (OSMIntersectionInfo intersection : intersections) {
            if (intersection.getArea() != null) {
                List<DirectedEdge> edges = generateEdges(intersection);
                if (edges.size() > 2) {
                    map.addIntersection(new TemporaryIntersection(edges));
                }
            }
            bumpProgress();
        }
    }

    private void generateBuildingObjects(Collection<OSMBuildingInfo> buildings) {
        for (OSMBuildingInfo building : buildings) {
            if (building.getArea() != null) {
                List<DirectedEdge> edges = generateEdges(building);
                if (edges.size() > 2) {
                    map.addBuilding(new TemporaryBuilding(edges, building.getBuildingID()));
                }
            }
            bumpProgress();
        }
    }

    private List<DirectedEdge> generateEdges(OSMShape s) {
        List<DirectedEdge> result = new ArrayList<DirectedEdge>();
        Iterator<Point2D> it = s.getVertices().iterator();
        Node first = map.getNode(it.next());
        Node previous = first;
        while (it.hasNext()) {
            Node n = map.getNode(it.next());
            if (!n.equals(previous)) {
                result.add(map.getDirectedEdge(previous, n));
                previous = n;
            }
        }
        if (!previous.equals(first)) {
            result.add(map.getDirectedEdge(previous, first));
        }
        return result;
    }
}
