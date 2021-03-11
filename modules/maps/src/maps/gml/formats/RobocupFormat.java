package maps.gml.formats;

import maps.gml.GMLMap;
import maps.gml.GMLCoordinates;
import maps.gml.GMLObject;
import maps.gml.GMLShape;
import maps.gml.GMLBuilding;
import maps.gml.GMLRoad;
import maps.gml.GMLSpace;
import maps.gml.GMLNode;
import maps.gml.GMLEdge;
import maps.gml.GMLDirectedEdge;
import maps.gml.GMLMapFormat;
import maps.MapException;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.Namespace;
import org.dom4j.DocumentHelper;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import rescuecore2.misc.Pair;
import rescuecore2.log.Logger;

/**
   A MapFormat that can handle Robocup Rescue GML maps.
 */
public final class RobocupFormat extends GMLMapFormat {
    /** Singleton instance. */
    public static final RobocupFormat INSTANCE = new RobocupFormat();

    private static final String RCR_NAMESPACE_URI = "urn:roborescue:map:gml";
    private static final Namespace RCR_NAMESPACE = DocumentHelper.createNamespace("rcr", RCR_NAMESPACE_URI);

    private static final QName RCR_ROOT_QNAME = DocumentHelper.createQName("map", RCR_NAMESPACE);
    private static final QName RCR_NODE_LIST_QNAME = DocumentHelper.createQName("nodelist", RCR_NAMESPACE);
    private static final QName RCR_EDGE_LIST_QNAME = DocumentHelper.createQName("edgelist", RCR_NAMESPACE);
    private static final QName RCR_BUILDING_LIST_QNAME = DocumentHelper.createQName("buildinglist", RCR_NAMESPACE);
    private static final QName RCR_ROAD_LIST_QNAME = DocumentHelper.createQName("roadlist", RCR_NAMESPACE);
    private static final QName RCR_SPACE_LIST_QNAME = DocumentHelper.createQName("spacelist", RCR_NAMESPACE);
    private static final QName RCR_NODE_QNAME = DocumentHelper.createQName("node", RCR_NAMESPACE);
    private static final QName RCR_EDGE_QNAME = DocumentHelper.createQName("edge", RCR_NAMESPACE);
    private static final QName RCR_BUILDING_QNAME = DocumentHelper.createQName("building", RCR_NAMESPACE);
    private static final QName RCR_ROAD_QNAME = DocumentHelper.createQName("road", RCR_NAMESPACE);
    private static final QName RCR_SPACE_QNAME = DocumentHelper.createQName("space", RCR_NAMESPACE);
    private static final QName RCR_NEIGHBOUR_QNAME = DocumentHelper.createQName("neighbour", RCR_NAMESPACE);

    private static final QName RCR_FLOORS_QNAME = DocumentHelper.createQName("floors", RCR_NAMESPACE);
    private static final QName RCR_BUILDING_CODE_QNAME = DocumentHelper.createQName("buildingcode", RCR_NAMESPACE);
    private static final QName RCR_IMPORTANCE_QNAME = DocumentHelper.createQName("importance", RCR_NAMESPACE);
    private static final QName RCR_CAPACITY_QNAME = DocumentHelper.createQName("capacity", RCR_NAMESPACE);

    // Map from uri prefix to uri for writing XML documents
    private static final Map<String, String> URIS = new HashMap<String, String>();

    private static final Comparator<GMLObject> ID_SORTER = new Comparator<GMLObject>() {
        @Override
        public int compare(GMLObject first, GMLObject second) {
            if (first.getID() < second.getID()) {
                return -1;
            }
            if (first.getID() > second.getID()) {
                return 1;
            }
            return 0;
        }
    };

    static {
        URIS.put("gml", Common.GML_NAMESPACE_URI);
        URIS.put("xlink", Common.XLINK_NAMESPACE_URI);
        URIS.put("rcr", RCR_NAMESPACE_URI);
    }

    private RobocupFormat() {
    }

    @Override
    public Map<String, String> getNamespaces() {
        return Collections.unmodifiableMap(URIS);
    }

    @Override
    public String toString() {
        return "Robocup rescue";
    }

    @Override
    public boolean isCorrectRootElement(String uri, String localName) {
        return RCR_NAMESPACE_URI.equals(uri) && "map".equals(localName);
    }

    @Override
    public GMLMap read(Document doc) throws MapException {
        GMLMap result = new GMLMap();
        readNodes(doc, result);
        readEdges(doc, result);
        readBuildings(doc, result);
        readRoads(doc, result);
        readSpaces(doc, result);
        return result;
    }

    @Override
    public Document write(GMLMap map) {
        Element root = DocumentHelper.createElement(RCR_ROOT_QNAME);
        Document result = DocumentHelper.createDocument(root);
        writeNodes(map, root.addElement(RCR_NODE_LIST_QNAME));
        writeEdges(map, root.addElement(RCR_EDGE_LIST_QNAME));
        writeShapes(map.getBuildings(), RCR_BUILDING_QNAME, root.addElement(RCR_BUILDING_LIST_QNAME));
        writeShapes(map.getRoads(), RCR_ROAD_QNAME, root.addElement(RCR_ROAD_LIST_QNAME));
        writeShapes(map.getSpaces(), RCR_SPACE_QNAME, root.addElement(RCR_SPACE_LIST_QNAME));
        return result;
    }

    private void writeNodes(GMLMap map, Element parent) {
        List<GMLNode> nodes = new ArrayList<GMLNode>(map.getNodes());
        Collections.sort(nodes, ID_SORTER);
        for (GMLNode next : nodes) {
            Element e = parent.addElement(Common.GML_NODE_QNAME);
            e.addAttribute(Common.GML_ID_QNAME, String.valueOf(next.getID()));
            e.addElement(Common.GML_POINT_PROPERTY_QNAME).addElement(Common.GML_POINT_QNAME).addElement(Common.GML_COORDINATES_QNAME).setText(next.getCoordinates().toString());
        }
    }

    private void writeEdges(GMLMap map, Element parent) {
        List<GMLEdge> edges = new ArrayList<GMLEdge>(map.getEdges());
        Collections.sort(edges, ID_SORTER);
        for (GMLEdge next : edges) {
            Element e = parent.addElement(Common.GML_EDGE_QNAME);
            e.addAttribute(Common.GML_ID_QNAME, String.valueOf(next.getID()));
            e.addElement(Common.GML_DIRECTED_NODE_QNAME).addAttribute(Common.GML_ORIENTATION_QNAME, "-").addAttribute(Common.XLINK_HREF_QNAME, "#" + next.getStart().getID());
            e.addElement(Common.GML_DIRECTED_NODE_QNAME).addAttribute(Common.GML_ORIENTATION_QNAME, "+").addAttribute(Common.XLINK_HREF_QNAME, "#" + next.getEnd().getID());
        }
    }

    private void writeShapes(Collection<? extends GMLShape> shapes, QName qname, Element parent) {
        List<GMLShape> sorted = new ArrayList<GMLShape>(shapes);
        Collections.sort(sorted, ID_SORTER);
        for (GMLShape next : sorted) {
            Element e = parent.addElement(qname).addAttribute(Common.GML_ID_QNAME, String.valueOf(next.getID())).addElement(Common.GML_FACE_QNAME);
            for (GMLDirectedEdge dEdge : next.getEdges()) {
                String orientation = dEdge.isForward() ? "+" : "-";
                Element dEdgeElement = e.addElement(Common.GML_DIRECTED_EDGE_QNAME).addAttribute(Common.GML_ORIENTATION_QNAME, orientation).addAttribute(Common.XLINK_HREF_QNAME, "#" + dEdge.getEdge().getID());
                Integer neighbour = next.getNeighbour(dEdge);
                if (neighbour != null) {
                    dEdgeElement.addAttribute(RCR_NEIGHBOUR_QNAME, String.valueOf(neighbour));
                }
            }
            if (next instanceof GMLBuilding) {
                GMLBuilding b = (GMLBuilding)next;
                e.addAttribute(RCR_FLOORS_QNAME, String.valueOf(b.getFloors()));
                e.addAttribute(RCR_BUILDING_CODE_QNAME, String.valueOf(b.getCode()));
                e.addAttribute(RCR_IMPORTANCE_QNAME, String.valueOf(b.getImportance()));
                //e.addAttribute(RCR__QNAME, String.valueOf(b.getImportance()));
            }
        }
    }

    private void readNodes(Document doc, GMLMap result) throws MapException {
        Logger.debug("Reading nodes");
        for (Object next : doc.getRootElement().elements(RCR_NODE_LIST_QNAME)) {
            Element nodeList = (Element)next;
            for (Object nextNode : nodeList.elements(Common.GML_NODE_QNAME)) {
                Element e = (Element)nextNode;
                int id = readID(e);
                String coordinates = readNodeCoordinates(e);
                GMLCoordinates c = new GMLCoordinates(coordinates);
                GMLNode node = new GMLNode(id, c);
                result.addNode(node);
            }
        }
        Logger.debug("Read " + result.getNodes().size() + " nodes");
    }

    private void readEdges(Document doc, GMLMap result) throws MapException {
        Logger.debug("Reading edges");
        for (Object next : doc.getRootElement().elements(RCR_EDGE_LIST_QNAME)) {
            Element edgeList = (Element)next;
            for (Object nextEdge : edgeList.elements(Common.GML_EDGE_QNAME)) {
                Element e = (Element)nextEdge;
                int id = readID(e);
                int startID = -1;
                int endID = -1;
                for (Object directedNode : e.elements(Common.GML_DIRECTED_NODE_QNAME)) {
                    Element directedNodeElement = (Element)directedNode;
                    if ("-".equals(directedNodeElement.attributeValue(Common.GML_ORIENTATION_QNAME))) {
                        if (startID != -1) {
                            throw new MapException("Edge has multiple start nodes: " + e);
                        }
                        startID = readHref(directedNodeElement, "start node");
                    }
                    if ("+".equals(directedNodeElement.attributeValue(Common.GML_ORIENTATION_QNAME))) {
                        if (endID != -1) {
                            throw new MapException("Edge has multiple end nodes: " + e);
                        }
                        endID = readHref(directedNodeElement, "end node");
                    }
                }
                GMLEdge edge = new GMLEdge(id, result.getNode(startID), result.getNode(endID), false);
                result.addEdge(edge);
            }
        }
        Logger.debug("Read " + result.getEdges().size() + " edges");
    }

    private void readBuildings(Document doc, GMLMap result) throws MapException {
        Logger.debug("Reading buildings");
        for (Object next : doc.getRootElement().elements(RCR_BUILDING_LIST_QNAME)) {
            Element buildingList = (Element)next;
            for (Object nextBuilding : buildingList.elements(RCR_BUILDING_QNAME)) {
                Element e = (Element)nextBuilding;
                Pair<List<GMLDirectedEdge>, List<Integer>> edges = readEdges(e, result);
                GMLBuilding b = new GMLBuilding(readID(e), edges.first(), edges.second());
                Element f = e.element(Common.GML_FACE_QNAME);
                int floors = readInt(f, RCR_FLOORS_QNAME, 1);
                int code = readInt(f, RCR_BUILDING_CODE_QNAME, 0);
                int importance = readInt(f, RCR_IMPORTANCE_QNAME, 1);
                int capacity = readInt(f, RCR_CAPACITY_QNAME, 0);
                b.setFloors(floors);
                b.setCode(code);
                b.setImportance(importance);
                b.setCapacity(capacity);
                result.addBuilding(b);
            }
        }
        Logger.debug("Read " + result.getBuildings().size() + " buildings");
    }

    private void readRoads(Document doc, GMLMap result) throws MapException {
        Logger.debug("Reading roads");
        for (Object next : doc.getRootElement().elements(RCR_ROAD_LIST_QNAME)) {
            Element roadList = (Element)next;
            for (Object nextRoad : roadList.elements(RCR_ROAD_QNAME)) {
                Element e = (Element)nextRoad;
                Pair<List<GMLDirectedEdge>, List<Integer>> edges = readEdges(e, result);
                GMLRoad r = new GMLRoad(readID(e), edges.first(), edges.second());
                result.addRoad(r);
            }
        }
        Logger.debug("Read " + result.getRoads().size() + " roads");
    }

    private void readSpaces(Document doc, GMLMap result) throws MapException {
        Logger.debug("Reading spaces");
        for (Object next : doc.getRootElement().elements(RCR_SPACE_LIST_QNAME)) {
            Element spaceList = (Element)next;
            for (Object nextSpace : spaceList.elements(RCR_SPACE_QNAME)) {
                Element e = (Element)nextSpace;
                Pair<List<GMLDirectedEdge>, List<Integer>> edges = readEdges(e, result);
                GMLSpace s = new GMLSpace(readID(e), edges.first(), edges.second());
                result.addSpace(s);
            }
        }
        Logger.debug("Read " + result.getSpaces().size() + " spaces");
    }

    private Pair<List<GMLDirectedEdge>, List<Integer>> readEdges(Element e, GMLMap map) throws MapException {
        List<GMLDirectedEdge> edges = new ArrayList<GMLDirectedEdge>();
        List<Integer> neighbours = new ArrayList<Integer>();
        Element faceElement =  e.element(Common.GML_FACE_QNAME);
        if (faceElement == null) {
            throw new MapException("Shape does not contain a gml:Face: " + e);
        }
        for (Object nextEdge : faceElement.elements(Common.GML_DIRECTED_EDGE_QNAME)) {
            Element directedEdge = (Element)nextEdge;
            //            Logger.debug("Next directed edge: " + directedEdge);
            int nextID = readHref(directedEdge, "underlying edge");
            String orientation = directedEdge.attributeValue(Common.GML_ORIENTATION_QNAME);
            boolean forward;
            if (orientation == null) {
                throw new MapException("Directed edge has no orientation attribute: " + e);
            }
            if ("+".equals(orientation)) {
                forward = true;
            }
            else if ("-".equals(orientation)) {
                forward = false;
            }
            else {
                throw new MapException("Directed edge has invalid orientation attribute: " + e);
            }
            GMLEdge edge = map.getEdge(nextID);
            GMLDirectedEdge dEdge = new GMLDirectedEdge(edge, forward);
            String neighbourString = directedEdge.attributeValue(RCR_NEIGHBOUR_QNAME);
            Integer neighbourID = null;
            if (neighbourString != null) {
                try {
                    neighbourID = Integer.valueOf(neighbourString);
                }
                catch (NumberFormatException ex) {
                    throw new MapException("Directed edge has invalid neighbour: " + e, ex);
                }
                edge.setPassable(true);
            }
            edges.add(dEdge);
            neighbours.add(neighbourID);
        }
        if (edges.isEmpty()) {
            throw new MapException("Shape contains no edges: " + e);
        }
        return new Pair<List<GMLDirectedEdge>, List<Integer>>(edges, neighbours);
    }

    private int readID(Element e) throws MapException {
        String s = e.attributeValue(Common.GML_ID_QNAME);
        if (s == null) {
            throw new MapException("No ID attribute found: " + e);
        }
        try {
            return Integer.parseInt(s);
        }
        catch (NumberFormatException ex) {
            throw new MapException("Couldn't parse ID attribute", ex);
        }
    }

    private String readNodeCoordinates(Element node) throws MapException {
        Element pointProperty = node.element(Common.GML_POINT_PROPERTY_QNAME);
        if (pointProperty == null) {
            throw new MapException("Couldn't find gml:pointProperty child of node");
        }
        Element point = pointProperty.element(Common.GML_POINT_QNAME);
        if (point == null) {
            throw new MapException("Couldn't find gml:Point child of node");
        }
        Element coords = point.element(Common.GML_COORDINATES_QNAME);
        if (coords == null) {
            throw new MapException("Couldn't find gml:coordinates child of node");
        }
        return coords.getText();
    }

    private int readHref(Element e, String type) throws MapException {
        String href = e.attributeValue(Common.XLINK_HREF_QNAME);
        if (href == null || href.length() == 0) {
            throw new MapException("Edge has no " + type + " ID");
        }
        try {
            return Integer.parseInt(href.substring(1));
        }
        catch (NumberFormatException ex) {
            throw new MapException("Edge has invalid " + type + " ID");
        }
    }

    private int readInt(Element e, QName attributeName, int defaultValue) throws MapException {
        String s = e.attributeValue(attributeName);
        if (s == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(s);
        }
        catch (NumberFormatException ex) {
            throw new MapException("Attribute " + attributeName + " is not an integer: " + e);
        }
    }
}
