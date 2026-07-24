package maps.convert.osm2gml;

import maps.gml.GMLMap;
import maps.gml.GMLNode;
import maps.gml.GMLEdge;
import maps.gml.GMLDirectedEdge;
import maps.gml.GMLShape;
import maps.convert.ConvertStep;
import maps.ScaleConversion;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

/**
 * This step creates the final GML objects from the temporary OSM representations.
 *
 * <p>
 * It handles the calculation of the map's bounding box and scale (converting coordinates
 * to meters), and translates all {@code Node}, {@code Edge}, and
 * {@code TemporaryObject} instances into their corresponding GML representations
 * (nodes, edges, buildings, roads, and intersections). Finally, it establishes
 * the topological neighbor relationships between the generated shapes.
 */
public class MakeObjectsStep extends ConvertStep {
    private final TemporaryMap map;
    private final GMLMap gmlMap;

    /**
     * Constructs a {@code MakeObjectsStep}.
     *
     * @param map    The {@code TemporaryMap} to road data from.
     * @param gmlMap The {@code GMLMap} to populate with the final objects.
     */
    public MakeObjectsStep(final TemporaryMap map, final GMLMap gmlMap) {
        super();
        this.map = map;
        this.gmlMap = gmlMap;
    }

    @Override
    public String getDescription() {
        return "Generating GML objects";
    }

    @Override
    protected void step() {
        final ScaleConversion conversion = createScaleConversion();
        final Collection<Node> nodes = map.getAllNodes();
        final Collection<Edge> edges = map.getAllEdges();
        final Collection<TemporaryObject> objects = map.getAllObjects();

        // Set up progress tracking based on total nodes, edges, and object processing phases.
        setProgressLimit(nodes.size() + edges.size() + (objects.size() * 2));

        final Map<Node, GMLNode> nodeMap = new HashMap<>();
        final Map<Edge, GMLEdge> edgeMap = new HashMap<>();
        final Map<TemporaryObject, GMLShape> shapeMap = new HashMap<>();

        convertNodes(nodes, conversion, nodeMap);
        convertEdges(edges, nodeMap, edgeMap);
        convertObjects(objects, edgeMap, shapeMap);
        generateNeighbours(objects, edgeMap, shapeMap);

        setStatus("Created " + gmlMap.getRoads().size() + " roads and " + gmlMap.getBuildings().size() + " buildings");
    }

    // Calculate map bounds and initialize scale conversion (1 unit = 1meter).
    private ScaleConversion createScaleConversion() {
        double xMin = Double.POSITIVE_INFINITY;
        double yMin = Double.POSITIVE_INFINITY;
        for (final Node next : map.getAllNodes()) {
            xMin = Math.min(xMin, next.getX());
            yMin = Math.min(yMin, next.getY());
        }

        final double sizeOf1m = ConvertTools.sizeOf1Metre(map.getOSMMap());
        final double scale = 1.0 / sizeOf1m;
        return new ScaleConversion(xMin, yMin, scale, scale);
    }

    // Convert all nodes into GML nodes with scaled coordinates.
    private void convertNodes(
            final Collection<Node> nodes,
            final ScaleConversion conversion,
            final Map<Node, GMLNode> nodeMap) {
        for (final Node node : nodes) {
            final double x = conversion.convertX(node.getX());
            final double y = conversion.convertY(node.getY());
            final GMLNode gmlNode = gmlMap.createNode(x, y);
            nodeMap.put(node, gmlNode);
            bumpProgress();
        }
    }

    // Convert all edges into GML edges using mapped nodes.
    private void convertEdges(
            final Collection<Edge> edges,
            final Map<Node, GMLNode> nodeMap,
            final Map<Edge, GMLEdge> edgeMap) {
        for (final Edge edge : edges) {
            final GMLNode first   = nodeMap.get(edge.getStart());
            final GMLNode second  = nodeMap.get(edge.getEnd());
            final GMLEdge gmlEdge = gmlMap.createEdge(first, second);
            edgeMap.put(edge, gmlEdge);
            bumpProgress();
        }
    }

    // Convert all temporary objects into GML shapes.
    private void convertObjects(
            final Collection<TemporaryObject> objects,
            final Map<Edge, GMLEdge> edgeMap,
            final Map<TemporaryObject, GMLShape> shapeMap) {
        for (final TemporaryObject object : objects) {
            final List<GMLDirectedEdge> gmlEdges = makeEdges(object, edgeMap);
            final GMLShape shape = switch (object) {
                case TemporaryBuilding     ignored -> gmlMap.createBuilding(gmlEdges);
                case TemporaryRoad         ignored -> gmlMap.createRoad(gmlEdges);
                case TemporaryIntersection ignored -> gmlMap.createRoad(gmlEdges);
                default -> throw new IllegalStateException("Unsupported object type");
            };
            shapeMap.put(object, shape);
            bumpProgress();
        }
    }

    // Generate and assign neighbor information for adjacent shapes.
    private void generateNeighbours(
            final Collection<TemporaryObject> objects,
            final Map<Edge, GMLEdge> edgeMap,
            final Map<TemporaryObject, GMLShape> shapeMap) {
        for (final TemporaryObject object : objects) {
            final GMLShape shape = shapeMap.get(object);
            for (final DirectedEdge edge : object.getEdges()) {
                final TemporaryObject neighbour = object.getNeighbor(edge);
                if (neighbour != null && shapeMap.containsKey(neighbour)) {
                    final GMLEdge gmlEdge = edgeMap.get(edge.getEdge());
                    final int neighbourID = shapeMap.get(neighbour).getID();
                    shape.setNeighbour(gmlEdge, neighbourID);
                }
            }
            bumpProgress();
        }
    }

    // Helper method to convert a list of directed edges into GML directed Edges.
    private List<GMLDirectedEdge> makeEdges(
            final TemporaryObject object, final Map<Edge, GMLEdge> edgeMap) {
        final List<DirectedEdge> directedEdges = object.getEdges();
        final List<GMLDirectedEdge> gmlEdges = new ArrayList<>(directedEdges.size());
        for (final DirectedEdge edge : directedEdges) {
            final GMLEdge gmlEdge = edgeMap.get(edge.getEdge());
            final GMLDirectedEdge gmlDirectedEdge = new GMLDirectedEdge(gmlEdge, edge.isForward());
            gmlEdges.add(gmlDirectedEdge);
        }
        return gmlEdges;
    }
}
