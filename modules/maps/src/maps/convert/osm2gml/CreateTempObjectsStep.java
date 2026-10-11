package maps.convert.osm2gml;

import maps.convert.ConvertStep;

import java.util.*;

import maps.convert.osm2gml.debug.*;
import maps.osm.OSMNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rescuecore2.misc.geometry.Point2D;

/**
 * Creates {@link TemporaryObject}s from OSM data.
 */
public class CreateTempObjectsStep extends ConvertStep {
    private final TemporaryMap map;

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateTempObjectsStep.class);

    /**
     * Constructs a new {@code MakeTempObjectsStep}.
     *
     * @param map the map
     */
    public CreateTempObjectsStep(TemporaryMap map) {
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
        logCount(TemporaryRoad.class, osmRoads.size(), roads.size());
        logCount(TemporaryIntersection.class, osmIntersections.size(), intersections.size());
        logCount(TemporaryBuilding.class, osmBuildings.size(), buildings.size());
        visualizeResults(roads, intersections, buildings);
    }

    private <T extends OSMObjectInfo> Set<TemporaryObject> generateObjects(Collection<T> osmShapes) {
        Set<TemporaryObject> created = new LinkedHashSet<>();

        for (OSMObjectInfo shape : osmShapes) {
            if (shape.getArea() == null) {
                logNoArea(shape);
                bumpProgress();
                continue;
            }

            List<DirectedEdge> edges = generateEdges(shape);
            if (edges.size() < 3) {
                logTooFewEdges(shape, edges.size());
                bumpProgress();
                continue;
            }

            TemporaryObject newObject = shape.createTemporaryObject(edges);
            map.addTemporaryObject(newObject);
            created.add(newObject);
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
                .title("Make Temporary Objects")
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

    // Logs how many temporary objects of one type were created from the given number of OSM shapes.
    private void logCount(Class<? extends TemporaryObject> type, int osmCount, int createdCount) {
        LOGGER.info("TEMP_OBJECTS_COUNT object_type={} osm_count={} created_count={}",
                type.getSimpleName(), osmCount, createdCount);
    }

    // Logs a shape skipped because it has no area (some vertices are unset)
    private void logNoArea(OSMObjectInfo shape) {
        if (shape instanceof OSMRoadInfo road) {
            logRoadNoArea(road);
            return;
        }
        LOGGER.info("TEMP_OBJECT_SKIPPED reason=NO_AREA shape_type={} shape={}",
                shape.getClass().getSimpleName(), shape);
    }

    // Logs a road skipped because some corners are unset, with the degree of both end intersections
    private void logRoadNoArea(OSMRoadInfo road) {
        LOGGER.info("TEMP_OBJECT_SKIPPED reason=NO_AREA shape_type=OSMRoadInfo start_id={} end_id={} " +
                        "start_degree={} end_degree={} start_left_set={} start_right_set={} " +
                        "end_left_set={} end_right_set={}",
                road.getStart().getId(), road.getEnd().getId(),
                degreeOf(road.getStart()), degreeOf(road.getEnd()),
                road.getStartLeft() != null, road.getStartRight() != null,
                road.getEndLeft() != null, road.getEndRight() != null);
    }

    // Returns the number of roads connected to the intersection at the node
    private Object degreeOf(OSMNode node) {
        return map.getOSMIntersectionDegree(map.getOSMIntersection(node));
    }

    // Logs a shape skipped because snapping its vertices to nodes left fewer than 3 edges
    private void logTooFewEdges(OSMObjectInfo shape, int edgeCount) {
        LOGGER.info("TEMP_OBJECT_SKIPPED reason=TOO_FEW_EDGES shape_type={} vertex_count={} edge_count={} shape={}",
                shape.getClass().getSimpleName(), shape.getVertices().size(), edgeCount, shape);
    }
}
