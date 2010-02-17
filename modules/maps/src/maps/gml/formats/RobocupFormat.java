package maps.gml.formats;

import maps.gml.GMLMap;
import maps.gml.GMLCoordinates;
import maps.gml.GMLShape;
import maps.gml.GMLBuilding;
import maps.gml.GMLRoad;
import maps.gml.GMLSpace;
import maps.gml.GMLNode;
import maps.gml.GMLEdge;
import maps.gml.GMLDirectedEdge;
import maps.gml.MapFormat;
import maps.gml.CoordinateSystem;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Attribute;
import org.dom4j.QName;
import org.dom4j.Namespace;
import org.dom4j.XPath;
import org.dom4j.DocumentHelper;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

import rescuecore2.misc.Pair;
//import rescuecore2.log.Logger;

/**
   A MapFormat that can handle Robocup Rescue GML maps.
 */
public final class RobocupFormat implements MapFormat {
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

    //    private static final QName RCR_TYPE_QNAME = DocumentHelper.createQName("type", RCR_NAMESPACE);
    //    private static final QName RCR_PASSABLE_QNAME = DocumentHelper.createQName("passable", RCR_NAMESPACE);

    // Map from uri prefix to uri for writing XML documents
    private static final Map<String, String> URIS = new HashMap<String, String>();

    private static final XPath NODE_XPATH = DocumentHelper.createXPath("//rcr:nodelist/gml:Node");
    private static final XPath EDGE_XPATH = DocumentHelper.createXPath("//rcr:edgelist/gml:Edge");
    private static final XPath BUILDING_XPATH = DocumentHelper.createXPath("//rcr:buildinglist/rcr:building");
    private static final XPath ROAD_XPATH = DocumentHelper.createXPath("//rcr:roadlist/rcr:road");
    private static final XPath SPACE_XPATH = DocumentHelper.createXPath("//rcr:spacelist/rcr:space");

    //    private static final XPath EDGE_REF_XPATH = DocumentHelper.createXPath("rcr:edge/@xlink:href");

    private static final XPath NODE_COORDINATES_XPATH = DocumentHelper.createXPath("gml:pointProperty/gml:Point/gml:coordinates");

    private static final XPath EDGE_START_XPATH = DocumentHelper.createXPath("gml:directedNode[@orientation='-']/@xlink:href");
    private static final XPath EDGE_END_XPATH = DocumentHelper.createXPath("gml:directedNode[@orientation='+']/@xlink:href");

    private static final XPath SHAPE_EDGE_XPATH = DocumentHelper.createXPath("gml:Face/gml:directedEdge");

    //    private static final XPath SHAPE_XPATH = DocumentHelper.createXPath("gml:polygon/gml:LinearRing/gml:coordinates/text()");

    /*
    // Node-related XPath expressions
    private static final XPath NODE_PATH = DocumentHelper.createXPath("//gml:Node");

    // Edge-related XPath expressions
    private static final XPath EDGE_PATH = DocumentHelper.createXPath("//gml:Edge");
    private static final XPath EDGE_COORDINATES_PATH = DocumentHelper.createXPath("gml:centerLineOf/gml:LineString/gml:coordinates/text()");

    // Face-related XPath expressions
    private static final XPath FACE_PATH = DocumentHelper.createXPath("gml:Face");
    */

    static {
        URIS.put("gml", Common.GML_NAMESPACE_URI);
        URIS.put("xlink", Common.XLINK_NAMESPACE_URI);
        URIS.put("rcr", RCR_NAMESPACE_URI);

        NODE_XPATH.setNamespaceURIs(URIS);
        EDGE_XPATH.setNamespaceURIs(URIS);
        BUILDING_XPATH.setNamespaceURIs(URIS);
        ROAD_XPATH.setNamespaceURIs(URIS);
        SPACE_XPATH.setNamespaceURIs(URIS);

        NODE_COORDINATES_XPATH.setNamespaceURIs(URIS);

        EDGE_START_XPATH.setNamespaceURIs(URIS);
        EDGE_END_XPATH.setNamespaceURIs(URIS);

        SHAPE_EDGE_XPATH.setNamespaceURIs(URIS);

        /*
          NODE_PATH.setNamespaceURIs(URIS);
          NODE_COORDINATES_PATH.setNamespaceURIs(URIS);
          EDGE_PATH.setNamespaceURIs(URIS);
          EDGE_START_PATH.setNamespaceURIs(URIS);
          EDGE_END_PATH.setNamespaceURIs(URIS);
          EDGE_COORDINATES_PATH.setNamespaceURIs(URIS);
          FACE_PATH.setNamespaceURIs(URIS);
          FACE_EDGE_PATH.setNamespaceURIs(URIS);
        */
    }

    /**
       Construct a new RobocupFormat instance.
    */
    public RobocupFormat() {
    }

    @Override
    public String toString() {
        return "Robocup rescue";
    }

    @Override
    public boolean looksValid(Document doc) {
        Element root = doc.getRootElement();
        return root.getQName().equals(RCR_ROOT_QNAME);
    }

    @Override
    public GMLMap read(Document doc) {
        GMLMap result = new GMLMap(CoordinateSystem.LATLON);
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
            e.addElement(Common.GML_DIRECTED_NODE_QNAME).addAttribute(Common.GML_ORIENTATION_QNAME, "-").addAttribute(Common.XLINK_HREF_QNAME, "#" + next.getStart().getID());
            e.addElement(Common.GML_DIRECTED_NODE_QNAME).addAttribute(Common.GML_ORIENTATION_QNAME, "+").addAttribute(Common.XLINK_HREF_QNAME, "#" + next.getEnd().getID());
        }
    }

    private void writeShapes(Collection<? extends GMLShape> shapes, QName qname, Element parent) {
        for (GMLShape next : shapes) {
            Element e = parent.addElement(qname).addAttribute(Common.GML_ID_QNAME, String.valueOf(next.getID())).addElement(Common.GML_FACE_QNAME);
            for (GMLDirectedEdge dEdge : next.getEdges()) {
                String orientation = dEdge.isForward() ? "+" : "-";
                Element dEdgeElement = e.addElement(Common.GML_DIRECTED_EDGE_QNAME).addAttribute(Common.GML_ORIENTATION_QNAME, orientation).addAttribute(Common.XLINK_HREF_QNAME, "#" + dEdge.getEdge().getID());
                Integer neighbour = next.getNeighbour(dEdge);
                if (neighbour != null) {
                    dEdgeElement.addAttribute(RCR_NEIGHBOUR_QNAME, String.valueOf(neighbour));
                }
            }
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
            //            Logger.debug("Read node " + node);
        }
    }

    private void readEdges(Document doc, GMLMap result) {
        for (Object next : EDGE_XPATH.selectNodes(doc)) {
            Element e = (Element)next;
            int id = readID(e);
            int startID = Integer.parseInt(((Attribute)EDGE_START_XPATH.evaluate(e)).getValue().substring(1));
            int endID = Integer.parseInt(((Attribute)EDGE_END_XPATH.evaluate(e)).getValue().substring(1));
            GMLEdge edge = new GMLEdge(id, result.getNode(startID), result.getNode(endID), false);
            result.addEdge(edge);
            //            Logger.debug("Read edge " + edge);
        }
    }

    private void readBuildings(Document doc, GMLMap result) {
        //        Logger.debug("Reading buildings");
        for (Object next : BUILDING_XPATH.selectNodes(doc)) {
            Element e = (Element)next;
            //            Logger.debug("Next element: " + e);
            Pair<List<GMLDirectedEdge>, List<Integer>> edges = readEdges(e, result);
            //            Logger.debug("Read building: " + edges);
            GMLBuilding b = new GMLBuilding(readID(e), edges.first(), edges.second());
            //            Logger.debug("New building: " + b);
            result.addBuilding(b);
        }
    }

    private void readRoads(Document doc, GMLMap result) {
        for (Object next : ROAD_XPATH.selectNodes(doc)) {
            Element e = (Element)next;
            Pair<List<GMLDirectedEdge>, List<Integer>> edges = readEdges(e, result);
            GMLRoad r = new GMLRoad(readID(e), edges.first(), edges.second());
            result.addRoad(r);
        }
    }

    private void readSpaces(Document doc, GMLMap result) {
        for (Object next : SPACE_XPATH.selectNodes(doc)) {
            Element e = (Element)next;
            Pair<List<GMLDirectedEdge>, List<Integer>> edges = readEdges(e, result);
            GMLSpace s = new GMLSpace(readID(e), edges.first(), edges.second());
            result.addSpace(s);
        }
    }

    private Pair<List<GMLDirectedEdge>, List<Integer>> readEdges(Element e, GMLMap map) {
        List<GMLDirectedEdge> edges = new ArrayList<GMLDirectedEdge>();
        List<Integer> neighbours = new ArrayList<Integer>();
        //        Logger.debug("Reading edges");
        for (Object nextEdge : SHAPE_EDGE_XPATH.selectNodes(e)) {
            Element directedEdge = (Element)nextEdge;
            //            Logger.debug("Next directed edge: " + directedEdge);
            int nextID = Integer.parseInt(directedEdge.attributeValue(Common.XLINK_HREF_QNAME).substring(1));
            boolean forward = "+".equals(directedEdge.attributeValue(Common.GML_ORIENTATION_QNAME));
            GMLDirectedEdge dEdge = new GMLDirectedEdge(map.getEdge(nextID), forward);
            String neighbourString = directedEdge.attributeValue(RCR_NEIGHBOUR_QNAME);
            Integer neighbourID = null;
            if (neighbourString != null) {
                neighbourID = Integer.valueOf(neighbourString);
            }
            edges.add(dEdge);
            neighbours.add(neighbourID);
        }
        return new Pair<List<GMLDirectedEdge>, List<Integer>>(edges, neighbours);
    }

    private int readID(Element e) {
        return Integer.parseInt(e.attributeValue(Common.GML_ID_QNAME));
    }
}