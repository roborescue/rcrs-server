package maps.gml.formats;

import maps.gml.GMLMap;
import maps.gml.GMLCoordinates;
import maps.gml.GMLShape;
import maps.gml.GMLBuilding;
import maps.gml.GMLRoad;
import maps.gml.GMLNode;
import maps.gml.GMLEdge;
import maps.gml.GMLDirectedEdge;
import maps.gml.GMLTools;
import maps.gml.GMLMapFormat;

import maps.ConstantConversion;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Attribute;
import org.dom4j.QName;
import org.dom4j.Namespace;
import org.dom4j.XPath;
import org.dom4j.DocumentHelper;

import org.jaxen.SimpleVariableContext;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Collections;

import rescuecore2.misc.Pair;
import rescuecore2.log.Logger;

/**
   A MapFormat that can handle GML maps from Meijo University.
 */
public final class MeijoFormat extends GMLMapFormat {
    /** Singleton instance. */
    public static final MeijoFormat INSTANCE = new MeijoFormat();

    private static final String MEIJO_NAMESPACE_URI = "http://sakura.meijo-u.ac.jp/rcrs";
    private static final String GML_APP_NAMESPACE_URI = "http://www.opengis.net/app";
    private static final Namespace RCRS_NAMESPACE = DocumentHelper.createNamespace("rcrs", MEIJO_NAMESPACE_URI);
    private static final Namespace GML_APP_NAMESPACE = DocumentHelper.createNamespace("app", GML_APP_NAMESPACE_URI);

    private static final QName ROOT_QNAME = DocumentHelper.createQName("Topology", GML_APP_NAMESPACE);
    private static final QName VERSION_QNAME = DocumentHelper.createQName("Version", RCRS_NAMESPACE);
    private static final QName DESCRIPTION_QNAME = DocumentHelper.createQName("Description", RCRS_NAMESPACE);
    private static final QName AREA_QNAME = DocumentHelper.createQName("Area", RCRS_NAMESPACE);
    private static final QName NODE_LIST_QNAME = DocumentHelper.createQName("NodeList", RCRS_NAMESPACE);
    private static final QName EDGE_LIST_QNAME = DocumentHelper.createQName("EdgeList", RCRS_NAMESPACE);
    private static final QName FACE_LIST_QNAME = DocumentHelper.createQName("FaceList", RCRS_NAMESPACE);
    private static final QName FACE_QNAME = DocumentHelper.createQName("Face", RCRS_NAMESPACE);
    private static final QName BUILDING_PROPERTY_QNAME = DocumentHelper.createQName("BuildingProperty", RCRS_NAMESPACE);

    // Map from uri prefix to uri for writing XML documents
    private static final Map<String, String> URIS = new HashMap<String, String>();

    private static final XPath NODE_XPATH = DocumentHelper.createXPath("//app:Topology/rcrs:Area/rcrs:NodeList/gml:Node");
    private static final XPath EDGE_XPATH = DocumentHelper.createXPath("//app:Topology/rcrs:Area/rcrs:EdgeList/gml:Edge");
    private static final XPath FACE_XPATH = DocumentHelper.createXPath("//app:Topology/rcrs:Area/rcrs:FaceList/rcrs:Face");

    private static final XPath NODE_COORDINATES_XPATH = DocumentHelper.createXPath("gml:pointProperty/gml:Point/gml:coordinates");
    private static final XPath EDGE_COORDINATES_XPATH = DocumentHelper.createXPath("gml:centerLineOf/gml:LineString/gml:coordinates");
    private static final XPath FACE_COORDINATES_XPATH = DocumentHelper.createXPath("gml:polygon/gml:LinearRing/gml:coordinates");

    private static final XPath EDGE_START_XPATH = DocumentHelper.createXPath("gml:directedNode[1]//@xlink:href");
    private static final XPath EDGE_END_XPATH = DocumentHelper.createXPath("gml:directedNode[2]/@xlink:href");

    private static final SimpleVariableContext FACE_NEIGHBOUR_XPATH_CONTEXT = new SimpleVariableContext();
    private static final String FACE_NEIGHBOUR_XPATH_STRING = "//rcrs:EdgeList/gml:Edge[@gml:id=\"$edgeid\"]/gml:directedFace[@xlink:href!=\"#$faceid\"]/@xlink:href";
    //    private static final XPath FACE_NEIGHBOUR_XPATH = DocumentHelper.createXPath("//rcrs:EdgeList/gml:Edge[@gml:id=\"$edgeid\"]/gml:directedFace[@xlink:href!=\"#$faceid\"]/@xlink:href");
    //    private static final XPath FACE_NEIGHBOUR_XPATH = DocumentHelper.createXPath("//rcrs:EdgeList/gml:Edge[@gml:id=\"$edgeid\"]/gml:directedFace", FACE_NEIGHBOUR_XPATH_CONTEXT);

    private static final double THRESHOLD = 0.0001;


    static {
        URIS.put("gml", Common.GML_NAMESPACE_URI);
        URIS.put("app", GML_APP_NAMESPACE_URI);
        URIS.put("xlink", Common.XLINK_NAMESPACE_URI);
        URIS.put("rcrs", MEIJO_NAMESPACE_URI);

        NODE_XPATH.setNamespaceURIs(URIS);
        EDGE_XPATH.setNamespaceURIs(URIS);
        FACE_XPATH.setNamespaceURIs(URIS);

        NODE_COORDINATES_XPATH.setNamespaceURIs(URIS);
        EDGE_COORDINATES_XPATH.setNamespaceURIs(URIS);
        FACE_COORDINATES_XPATH.setNamespaceURIs(URIS);

        EDGE_START_XPATH.setNamespaceURIs(URIS);
        EDGE_END_XPATH.setNamespaceURIs(URIS);

        //        FACE_NEIGHBOUR_XPATH.setNamespaceURIs(URIS);
    }

    private MeijoFormat() {
    }

    @Override
    public Map<String, String> getNamespaces() {
        return Collections.unmodifiableMap(URIS);
    }

    @Override
    public String toString() {
        return "Meijo";
    }

    @Override
    public boolean isCorrectRootElement(String uri, String localName) {
        return MEIJO_NAMESPACE_URI.equals(uri) && "Topology".equals(localName);
    }

    @Override
    public GMLMap read(Document doc) {
        GMLMap result = new GMLMap();
        readNodes(doc, result);
        // This format has coordinates in mm, so divide by 1000 to convert to m.
        // CHECKSTYLE:OFF:MagicNumber
        result.convertCoordinates(new ConstantConversion(0.001));
        // CHECKSTYLE:ON:MagicNumber
        readEdges(doc, result);
        readFaces(doc, result);
        splitMultipleEdges(result);
        //        checkEdgeOrderAndDirection(result);
        return result;
    }

    @Override
    public Document write(GMLMap map) {
        Element root = DocumentHelper.createElement(ROOT_QNAME);
        Document result = DocumentHelper.createDocument(root);
        writeNodes(map, root.addElement(NODE_LIST_QNAME));
        writeEdges(map, root.addElement(EDGE_LIST_QNAME));
        writeFaces(map, root.addElement(FACE_LIST_QNAME));
        return result;
    }

    private void writeNodes(GMLMap map, Element parent) {
        for (GMLNode next : map.getNodes()) {
            Element e = parent.addElement(Common.GML_NODE_QNAME);
            e.addAttribute(Common.GML_ID_QNAME, String.valueOf(next.getID()));
            e.addElement(Common.GML_POINT_PROPERTY_QNAME).addElement(Common.GML_POINT_QNAME).addElement(Common.GML_COORDINATES_QNAME).setText(next.getCoordinates().toString());
        }
    }

    private void writeEdges(GMLMap map, Element parent) {
        for (GMLEdge next : map.getEdges()) {
            Element e = parent.addElement(Common.GML_EDGE_QNAME);
            e.addAttribute(Common.GML_ID_QNAME, String.valueOf(next.getID()));
            e.addElement(Common.GML_DIRECTED_NODE_QNAME).addAttribute(Common.GML_ORIENTATION_QNAME, "+").addAttribute(Common.XLINK_HREF_QNAME, "#" + next.getStart().getID());
            e.addElement(Common.GML_DIRECTED_NODE_QNAME).addAttribute(Common.GML_ORIENTATION_QNAME, "+").addAttribute(Common.XLINK_HREF_QNAME, "#" + next.getEnd().getID());
            // Add directed faces
            // This will be real slow
            for (GMLShape shape : map.getAllShapes()) {
                for (GMLDirectedEdge edge : shape.getEdges()) {
                    if (edge.getEdge() == next) {
                        e.addElement(Common.GML_DIRECTED_FACE_QNAME).addAttribute(Common.GML_ORIENTATION_QNAME, "+").addAttribute(Common.XLINK_HREF_QNAME, "#" + shape.getID());
                    }
                }
            }
        }
    }

    private void writeFaces(GMLMap map, Element parent) {
        for (GMLShape next : map.getAllShapes()) {
            Element e = parent.addElement(FACE_QNAME);
            if (next instanceof GMLBuilding) {
                parent.addElement(BUILDING_PROPERTY_QNAME);
            }
            e = e.addElement(Common.GML_FACE_QNAME);
            e.addAttribute(Common.GML_ID_QNAME, String.valueOf(next.getID()));
            for (GMLDirectedEdge edge : next.getEdges()) {
                String orientation = "-";
                if (edge.getEdge().isPassable()) {
                    orientation = "+";
                }
                e.addElement(Common.GML_DIRECTED_EDGE_QNAME).addAttribute(Common.GML_ORIENTATION_QNAME, orientation).addAttribute(Common.XLINK_HREF_QNAME, "#" + edge.getEdge().getID());
            }
            e.addElement(Common.GML_POLYGON_QNAME).addElement(Common.GML_LINEAR_RING_QNAME).addElement(Common.GML_COORDINATES_QNAME).setText(GMLTools.getCoordinatesString(next.getCoordinates()));
        }
    }

    private void readNodes(Document doc, GMLMap result) {
        for (Object next : NODE_XPATH.selectNodes(doc)) {
            Element e = (Element)next;
            int id = readID(e);
            String coordinates = ((Element)NODE_COORDINATES_XPATH.evaluate(e)).getText();
            GMLCoordinates c = new GMLCoordinates(coordinates);
            GMLNode node = new GMLNode(id, c);
            result.addNode(node);
            Logger.debug("Read node " + node);
        }
    }

    private void readEdges(Document doc, GMLMap result) {
        for (Object next : EDGE_XPATH.selectNodes(doc)) {
            Element e = (Element)next;
            int id = readID(e);
            //            Logger.debug("Children: " + e.elements());
            //            Logger.debug("Start: " + EDGE_START_XPATH.evaluate(e));
            int startID = Integer.parseInt(((Attribute)EDGE_START_XPATH.evaluate(e)).getValue().substring(1));
            int endID = Integer.parseInt(((Attribute)EDGE_END_XPATH.evaluate(e)).getValue().substring(1));
            GMLEdge edge = new GMLEdge(id, result.getNode(startID), result.getNode(endID), false);
            // Read the edge coordinates
            edge.setPoints(GMLTools.getCoordinatesList(((Element)EDGE_COORDINATES_XPATH.evaluate(e)).getText()));
            result.addEdge(edge);
            Logger.debug("Read edge " + edge);
        }
    }

    private void readFaces(Document doc, GMLMap result) {
        //        Logger.debug("Reading buildings");
        for (Object next : FACE_XPATH.selectNodes(doc)) {
            //            Logger.debug("Reading " + next);
            Element e = (Element)next;
            String type = e.attributeValue("type");
            Element gmlFace = e.element(Common.GML_FACE_QNAME);
            int id = readID(gmlFace);
            //            Logger.debug("ID = " + id);
            //            Logger.debug("Type = " + type);
            Pair<List<GMLDirectedEdge>, List<Integer>> edges = readEdges(gmlFace, result, id);
            //            Logger.debug("Edges: " + edges);
            GMLShape shape = null;
            if ("building".equals(type)) {
                shape = new GMLBuilding(id, edges.first(), edges.second());
            }
            else {
                shape = new GMLRoad(id, edges.first(), edges.second());
            }
            //            Logger.debug("Computing coordinates");
            String coordsString = ((Element)FACE_COORDINATES_XPATH.evaluate(gmlFace)).getText();
            //            Logger.debug("coordsString = " + coordsString);
            List<GMLCoordinates> coords = GMLTools.getCoordinatesList(coordsString);
            //            Logger.debug("coords = " + coords);
            shape.setCoordinates(coords);
            result.add(shape);
            Logger.debug("Read shape: " + shape);
        }
    }

    private Pair<List<GMLDirectedEdge>, List<Integer>> readEdges(Element e, GMLMap map, int faceID) {
        List<GMLDirectedEdge> edges = new ArrayList<GMLDirectedEdge>();
        List<Integer> neighbours = new ArrayList<Integer>();

        //        Logger.debug("Reading edges for face " + faceID);
        for (Object next : e.elements(Common.GML_DIRECTED_EDGE_QNAME)) {
            Element dEdge = (Element)next;
            boolean passable = "+".equals(dEdge.attributeValue(Common.GML_ORIENTATION_QNAME));
            int edgeID = Integer.parseInt(dEdge.attributeValue(Common.XLINK_HREF_QNAME).substring(1));
            //            Logger.debug("Edge ID: " + edgeID);
            //            Logger.debug("Passable? " + passable);
            edges.add(new GMLDirectedEdge(map.getEdge(edgeID), true));
            XPath xpath = makeFaceNeighbourXPath(edgeID, faceID);
            //            FACE_NEIGHBOUR_XPATH_CONTEXT.setVariableValue("edgeid", String.valueOf(edgeID));
            //            FACE_NEIGHBOUR_XPATH_CONTEXT.setVariableValue("faceid", String.valueOf(faceID));
            Object o = xpath.evaluate(e);
            //            Logger.debug("Neighbours: " + o);
            if (o == null) {
                neighbours.add(null);
            }
            else if (o instanceof Collection && ((Collection)o).isEmpty()) {
                neighbours.add(null);
            }
            else {
                int neighbourID = Integer.parseInt(((Attribute)o).getValue().substring(1));
                neighbours.add(neighbourID);
            }
            //            Logger.debug("Edge list     : " + edges);
            //            Logger.debug("Neighbour list: " + neighbours);
        }
        //        Logger.debug("Finished reading edges for face " + faceID);
        return new Pair<List<GMLDirectedEdge>, List<Integer>>(edges, neighbours);
    }

    private int readID(Element e) {
        return Integer.parseInt(e.attributeValue(Common.GML_ID_QNAME));
    }

    private XPath makeFaceNeighbourXPath(int edgeID, int faceID) {
        String path = FACE_NEIGHBOUR_XPATH_STRING.replace("$edgeid", String.valueOf(edgeID)).replace("$faceid", String.valueOf(faceID));
        //        Logger.debug("Neighbour XPath: " + path);
        XPath result = DocumentHelper.createXPath(path);
        result.setNamespaceURIs(URIS);
        return result;
    }

    private void splitMultipleEdges(GMLMap map) {
        // Look for edges that have more then 2 GMLCoordinates and split them into multiple edges
        for (GMLEdge edge : map.getEdges()) {
            if (edge.getPoints().size() != 2) {
                // Split this edge
                Iterator<GMLCoordinates> it = edge.getPoints().iterator();
                GMLCoordinates first = it.next();
                List<GMLEdge> newEdges = new ArrayList<GMLEdge>();
                while (it.hasNext()) {
                    GMLCoordinates second = it.next();
                    GMLNode n1 = map.createNode(first);
                    GMLNode n2 = map.createNode(second);
                    GMLEdge newEdge = map.createEdge(n1, n2);
                    newEdges.add(newEdge);
                    first = second;
                }
                // Update any shapes that reference the old edge
                for (GMLShape shape : map.getAllShapes()) {
                    replaceEdge(shape, edge, newEdges);
                }
                map.removeEdge(edge);
                //                Logger.debug("Split " + edge);
                //                Logger.debug("New edges: " + newEdges);
            }
        }
    }

    private void replaceEdge(GMLShape shape, GMLEdge oldEdge, List<GMLEdge> newEdges) {
        List<GMLDirectedEdge> newShapeEdges = new ArrayList<GMLDirectedEdge>();
        List<Integer> newShapeNeighbours = new ArrayList<Integer>();
        boolean found = false;
        for (GMLDirectedEdge e : shape.getEdges()) {
            if (e.getEdge().equals(oldEdge)) {
                found = true;
                GMLNode start = e.getStartNode();
                Integer neighbour = shape.getNeighbour(e);
                for (GMLEdge next : newEdges) {
                    GMLDirectedEdge newDEdge = new GMLDirectedEdge(next, start);
                    newShapeEdges.add(newDEdge);
                    newShapeNeighbours.add(neighbour);
                    start = newDEdge.getEndNode();
                }
            }
            else {
                newShapeEdges.add(e);
                newShapeNeighbours.add(shape.getNeighbour(e));
            }
        }
        if (found) {
            shape.setEdges(newShapeEdges);
            Iterator<GMLDirectedEdge> it = newShapeEdges.iterator();
            Iterator<Integer> ix = newShapeNeighbours.iterator();
            while (it.hasNext() && ix.hasNext()) {
                shape.setNeighbour(it.next(), ix.next());
            }
        }
    }

    /*
    private void checkEdgeOrderAndDirection(GMLMap map) {
        Set<GMLDirectedEdge> remaining = new HashSet<GMLDirectedEdge>();
        List<GMLDirectedEdge> reordered = new ArrayList<GMLDirectedEdge>();
        for (GMLShape shape : map.getAllShapes()) {
            remaining.clear();
            reordered.clear();
            remaining.addAll(shape.getEdges());
            //            Iterator<GMLDirectedEdge> it = shape.getEdges().iterator();
            GMLDirectedEdge edge = shape.getEdges().get(0);
            GMLNode start = edge.getEndNode();
            //            Logger.debug("Reordering " + remaining.size() + " edges for " + shape);
            //            Logger.debug("Original order");
            //            for (GMLDirectedEdge e : shape.getEdges()) {
            //                logEdge(e);
            //            }
            //            Logger.debug("First edge");
            //            logEdge(edge);
            remaining.remove(edge);
            reordered.add(edge);
            while (!remaining.isEmpty()) {
                edge = null;
                // Find the next edge
                for (GMLDirectedEdge next : remaining) {
                    if (closeEnough(next.getStartNode(), start)) {
                        edge = next;
                        break;
                    }
                    if (closeEnough(next.getEndNode(), start)) {
                        edge = next;
                        edge.reverse();
                        break;
                    }
                }
                if (edge == null) {
                    throw new RuntimeException("Failed to reorder edges: found discontinuity in shape outline");
                }
                //                Logger.debug("Next edge");
                //                logEdge(edge);
                remaining.remove(edge);
                reordered.add(edge);
                start = edge.getEndNode();
            }
            //            Logger.debug("Reordered");
            //            for (GMLDirectedEdge e : reordered) {
            //                logEdge(e);
            //            }
            shape.reorderEdges(reordered);
        }
    }
    */

    //    private void logEdge(GMLDirectedEdge e) {
    //        Logger.debug(e.getEdge().getID() + ": " + e.getStartNode().getID() + " -> " + e.getEndNode().getID());
    //    }

    private boolean closeEnough(GMLNode n1, GMLNode n2) {
        if (n1 == n2) {
            return true;
        }
        double dx = n1.getX() - n2.getX();
        double dy = n1.getY() - n2.getY();
        return (dx > -THRESHOLD
                && dx < THRESHOLD
                && dy > -THRESHOLD
                && dy < THRESHOLD);
    }
}
