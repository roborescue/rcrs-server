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
   This step creates the final GML objects.
*/
public class MakeObjectsStep extends ConvertStep {
    private TemporaryMap map;
    private GMLMap gmlMap;

    /**
       Construct a MakeObjectsStep.
       @param map The TemporaryMap to read.
       @param gmlMap The GMLMap to populate.
    */
    public MakeObjectsStep(TemporaryMap map, GMLMap gmlMap) {
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
        double xMin = Double.POSITIVE_INFINITY;
        double yMin = Double.POSITIVE_INFINITY;
        for (Node next : map.getAllNodes()) {
            xMin = Math.min(xMin, next.getX());
            yMin = Math.min(yMin, next.getY());
        }
        double sizeOf1m = ConvertTools.sizeOf1Metre(map.getOSMMap());
        double scale = 1.0 / sizeOf1m;
        ScaleConversion conversion = new ScaleConversion(xMin, yMin, scale, scale);
        Collection<Node> nodes = map.getAllNodes();
        Collection<Edge> edges = map.getAllEdges();
        setProgressLimit(nodes.size() + edges.size() + (map.getAllObjects().size() * 2));
        Map<Node, GMLNode> nodeMap = new HashMap<Node, GMLNode>();
        Map<Edge, GMLEdge> edgeMap = new HashMap<Edge, GMLEdge>();
        Map<TemporaryObject, GMLShape> shapeMap = new HashMap<TemporaryObject, GMLShape>();
        for (Node n : nodes) {
            GMLNode node = gmlMap.createNode(conversion.convertX(n.getX()), conversion.convertY(n.getY()));
            nodeMap.put(n, node);
            bumpProgress();
        }
        for (Edge e : edges) {
            GMLNode first = nodeMap.get(e.getStart());
            GMLNode second = nodeMap.get(e.getEnd());
            GMLEdge edge = gmlMap.createEdge(first, second);
            edgeMap.put(e, edge);
            bumpProgress();
        }
        for (TemporaryBuilding b : map.getBuildings()) {
            shapeMap.put(b, gmlMap.createBuilding(makeEdges(b, edgeMap)));
            bumpProgress();
        }
        for (TemporaryRoad r : map.getRoads()) {
            shapeMap.put(r, gmlMap.createRoad(makeEdges(r, edgeMap)));
            bumpProgress();
        }
        for (TemporaryIntersection i : map.getIntersections()) {
            shapeMap.put(i, gmlMap.createRoad(makeEdges(i, edgeMap)));
            bumpProgress();
        }
        // Generate neighbour information
        for (TemporaryObject o : map.getAllObjects()) {
            GMLShape s = shapeMap.get(o);
            for (DirectedEdge e : o.getEdges()) {
                TemporaryObject neighbour = o.getNeighbour(e);
                if (neighbour != null) {
                    s.setNeighbour(edgeMap.get(e.getEdge()), shapeMap.get(neighbour).getID());
                }
            }
            bumpProgress();
        }
        setStatus("Created " + gmlMap.getRoads().size() + " roads and " + gmlMap.getBuildings().size() + " buildings");
    }

    private List<GMLDirectedEdge> makeEdges(TemporaryObject o, Map<Edge, GMLEdge> edgeMap) {
        List<DirectedEdge> oldEdges = o.getEdges();
        List<GMLDirectedEdge> result = new ArrayList<GMLDirectedEdge>(oldEdges.size());
        for (DirectedEdge dEdge : oldEdges) {
            result.add(new GMLDirectedEdge(edgeMap.get(dEdge.getEdge()), dEdge.isForward()));
        }
        return result;
    }
}
