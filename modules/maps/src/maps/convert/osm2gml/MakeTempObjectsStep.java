package maps.convert.osm2gml;

import maps.convert.ConvertStep;

import java.util.*;

import maps.convert.osm2gml.debug.*;
import rescuecore2.misc.geometry.Point2D;

/**
   This step creates TemporaryObjects from the OSM data.
*/
public class MakeTempObjectsStep extends ConvertStep {
    private final TemporaryMap map;

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
        final Collection<OSMRoadInfo> osmRoads = map.getOSMRoads();
        final Collection<OSMIntersectionInfo> osmIntersection = map.getOSMIntersections();
        final Collection<OSMBuildingInfo> osmBuildings = map.getOSMBuildings();
        setProgressLimit(osmRoads.size() + osmIntersection.size() + osmBuildings.size());

        final Set<TemporaryRoad> createdRoads = generateRoads(osmRoads);
        final Set<TemporaryIntersection> createdIntersections = generateIntersections(osmIntersection);
        final Set<TemporaryBuilding> createdBuildings = generateBuildings(osmBuildings);
        setStatus("Created " + createdRoads.size() + " roads, " + createdIntersections.size() + " intersections, " +
                createdBuildings.size() + " buildings");
        visualizeResults(createdRoads, createdIntersections, createdBuildings);
    }

    private Set<TemporaryRoad> generateRoads(final Collection<OSMRoadInfo> roads) {
        final Set<TemporaryRoad> createdRoads = new HashSet<>();
        for (final OSMRoadInfo road : roads) {
            if (road.getArea() == null) {
                bumpProgress();
                continue;
            }
            final List<DirectedEdge> edges = generateEdges(road);
            if (2 < edges.size()) {
                final TemporaryRoad roadToCreate = new TemporaryRoad(edges);
                map.addRoad(roadToCreate);
                createdRoads.add(roadToCreate);
            }
            bumpProgress();
        }
        return createdRoads;
    }

    private Set<TemporaryIntersection> generateIntersections(final Collection<OSMIntersectionInfo> intersections) {
        final Set<TemporaryIntersection> createdIntersections = new HashSet<>();
        for (final OSMIntersectionInfo intersection : intersections) {
            if (intersection.getArea() == null) {
                bumpProgress();
                continue;
            }
            final List<DirectedEdge> edges = generateEdges(intersection);
            if (2 < edges.size()) {
                final TemporaryIntersection intersectionToCreate = new TemporaryIntersection(edges);
                map.addIntersection(intersectionToCreate);
                createdIntersections.add(intersectionToCreate);
            }
            bumpProgress();
        }
        return createdIntersections;
    }

    private Set<TemporaryBuilding> generateBuildings(final Collection<OSMBuildingInfo> buildings) {
        final Set<TemporaryBuilding> createdBuildings = new HashSet<>();
        for (final OSMBuildingInfo building : buildings) {
            if (building.getArea() == null) {
                bumpProgress();
                continue;
            }
            final List<DirectedEdge> edges = generateEdges(building);
            if (2 < edges.size()) {
                final TemporaryBuilding buildingToCreate = new TemporaryBuilding(edges, building.getBuildingID());
                map.addBuilding(buildingToCreate);
                createdBuildings.add(buildingToCreate);
            }
            bumpProgress();
        }
        return createdBuildings;
    }

    private List<DirectedEdge> generateEdges(final OSMShape shape) {
        final List<DirectedEdge> result = new ArrayList<>();
        final Iterator<Point2D> it = shape.getVertices().iterator();
        final Node first = map.getNode(it.next());
        Node previous = first;
        while (it.hasNext()) {
            final Node n = map.getNode(it.next());
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

    private void visualizeResults(
            final Set<TemporaryRoad> createdRoads, final Set<TemporaryIntersection> createdIntersections,
            final Set<TemporaryBuilding> createdBuildings) {
        StepVisualizer.create(debug)
                .title("Make Temporary Objects Results")
                .layer(PolygonLayer.of(createdRoads)
                        .name("Created Roads")
                        .outlineColor(DebugPalette.MOSS_STROKE)
                        .fillColor(DebugPalette.MOSS_FILL))
                .layer(PolygonLayer.of(createdIntersections)
                        .name("Created Intersections")
                        .outlineColor(DebugPalette.MOSS_STROKE)
                        .fillColor(DebugPalette.MOSS_FILL))
                .layer(PolygonLayer.of(createdBuildings)
                        .name("Created Buildings")
                        .outlineColor(DebugPalette.MOSS_STROKE)
                        .fillColor(DebugPalette.MOSS_FILL))
                .show();
    }
}
