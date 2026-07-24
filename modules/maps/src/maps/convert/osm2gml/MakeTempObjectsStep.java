package maps.convert.osm2gml;

import maps.convert.ConvertStep;

import java.util.*;

import maps.convert.osm2gml.debug.*;
import rescuecore2.misc.geometry.Point2D;

/**
 * Creates {@link TemporaryObject}s from OSM data.
 */
public class MakeTempObjectsStep extends ConvertStep {
    private final TemporaryMap map;

    /**
     * Constructs a new {@code MakeTempObjectsStep}.
     *
     * @param map the map
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
        final Collection<OSMIntersectionInfo> osmIntersections = map.getOSMIntersections();
        final Collection<OSMBuildingInfo> osmBuildings = map.getOSMBuildings();
        setProgressLimit(osmRoads.size() + osmIntersections.size() + osmBuildings.size());

        final Set<TemporaryObject> roads = generateObjects(osmRoads);
        final Set<TemporaryObject> intersections = generateObjects(osmIntersections);
        final Set<TemporaryObject> buildings = generateObjects(osmBuildings);
        setStatus("Created " + roads.size() + " roads, " + intersections.size() + " intersections, " +
                buildings.size() + " buildings");
        visualizeResults(roads, intersections, buildings);
    }

    private <T extends OSMObjectInfo> Set<TemporaryObject> generateObjects(Collection<T> osmShapes) {
        Set<TemporaryObject> created = new LinkedHashSet<>();

        for (OSMObjectInfo shape : osmShapes) {
            if (shape.getArea() == null) {
                bumpProgress();
                continue;
            }

            List<DirectedEdge> edges = generateEdges(shape);
            if (2 < edges.size()) {
                TemporaryObject newObject = shape.createTemporaryObject(edges);
                map.addTemporaryObject(newObject);
                created.add(newObject);
            }
            bumpProgress();
        }
        return created;
    }

    private List<DirectedEdge> generateEdges(OSMObjectInfo shape) {
        List<DirectedEdge> result = new ArrayList<>();
        Iterator<Point2D> it = shape.getVertices().iterator();
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

    private void visualizeResults(
            Set<TemporaryObject> roads, Set<TemporaryObject> intersections, Set<TemporaryObject> buildings) {

        StepVisualizer.create(debug)
                .title("Make Temporary Objects Results")
                .layer(PolygonLayer.of(roads)
                        .name("Created Roads")
                        .outlineColor(DebugPalette.SKY_STROKE)
                        .fillColor(DebugPalette.SKY_FILL))
                .layer(PolygonLayer.of(intersections)
                        .name("Created Intersections")
                        .outlineColor(DebugPalette.AZURE_STROKE)
                        .fillColor(DebugPalette.AZURE_FILL))
                .layer(PolygonLayer.of(buildings)
                        .name("Created Buildings")
                        .outlineColor(DebugPalette.MOSS_STROKE)
                        .fillColor(DebugPalette.MOSS_FILL))
                .show();
    }
}
