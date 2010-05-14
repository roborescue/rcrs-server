package maps.gml.formats;

import maps.gml.GMLMap;
import maps.gml.GMLNode;
import maps.gml.GMLDirectedEdge;
import maps.gml.GMLCoordinates;
import maps.gml.GMLBuilding;
import maps.gml.GMLRoad;
import maps.gml.GMLMapFormat;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.XPath;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Collection;
import java.util.Collections;

import rescuecore2.log.Logger;

// TO DO: Handle inner boundaries

/**
   A MapFormat that can handle maps from the UK Ordnance Survey.
 */
public final class OrdnanceSurveyFormat extends GMLMapFormat {
    /** Singleton instance. */
    public static final OrdnanceSurveyFormat INSTANCE = new OrdnanceSurveyFormat();

    private static final String FEATURE_CODE_BUILDING = "10021";
    private static final String FEATURE_CODE_ROAD = "10172";
    private static final String FEATURE_CODE_FOOTPATH = "10183";

    private static final String FEATURE_CODE_OPEN_SPACE = "10053";
    private static final String FEATURE_CODE_GENERAL_SPACE = "10056";

    private static final String OSGB_NAMESPACE_URI = "http://www.ordnancesurvey.co.uk/xml/namespaces/osgb";

    private static final Namespace OSGB_NAMESPACE = DocumentHelper.createNamespace("osgb", OSGB_NAMESPACE_URI);

    private static final QName FEATURE_COLLECTION_QNAME = DocumentHelper.createQName("FeatureCollection", OSGB_NAMESPACE);
    private static final QName TOPOGRAPHIC_AREA_QNAME = DocumentHelper.createQName("TopographicArea", OSGB_NAMESPACE);

    private static final XPath BUILDING_XPATH = DocumentHelper.createXPath("//osgb:topographicMember/osgb:TopographicArea[osgb:featureCode[text()='" + FEATURE_CODE_BUILDING + "']]");
    //    private static final XPath ROAD_XPATH = DocumentHelper.createXPath("//osgb:topographicMember/osgb:TopographicArea[osgb:featureCode[text()='" + FEATURE_CODE_ROAD + "' or text()='" + FEATURE_CODE_FOOTPATH + "']]");
    private static final XPath ROAD_XPATH = DocumentHelper.createXPath("//osgb:topographicMember/osgb:TopographicArea[osgb:featureCode[text()='" + FEATURE_CODE_ROAD + "']]");
    private static final XPath SPACE_XPATH = DocumentHelper.createXPath("//osgb:topographicMember/osgb:TopographicArea[osgb:featureCode[text()='" + FEATURE_CODE_OPEN_SPACE + "' or text()='" + FEATURE_CODE_GENERAL_SPACE + "']]");
    private static final XPath SHAPE_XPATH = DocumentHelper.createXPath("osgb:polygon/gml:Polygon/gml:outerBoundaryIs/gml:LinearRing/gml:coordinates");
    private static final XPath INNER_RING_XPATH = DocumentHelper.createXPath("osgb:polygon/gml:Polygon/gml:innerBoundaryIs/gml:LinearRing/gml:coordinates");

    // Map from uri prefix to uri for XPath expressions
    private static final Map<String, String> URIS = new HashMap<String, String>();

    private static final int FID_PREFIX_LENGTH = 4;

    static {
        URIS.put("gml", Common.GML_NAMESPACE_URI);
        URIS.put("xlink", Common.XLINK_NAMESPACE_URI);
        URIS.put("osgb", OSGB_NAMESPACE_URI);

        BUILDING_XPATH.setNamespaceURIs(URIS);
        ROAD_XPATH.setNamespaceURIs(URIS);
        SPACE_XPATH.setNamespaceURIs(URIS);
        SHAPE_XPATH.setNamespaceURIs(URIS);
        INNER_RING_XPATH.setNamespaceURIs(URIS);
    }

    private OrdnanceSurveyFormat() {
    }

    @Override
    public String toString() {
        return "Ordnance survey";
    }

    @Override
    public Map<String, String> getNamespaces() {
        return Collections.unmodifiableMap(URIS);
    }

    @Override
    public boolean isCorrectRootElement(String uri, String localName) {
        return OSGB_NAMESPACE_URI.equals(uri) && "FeatureCollection".equals(localName);
    }

    @Override
    public GMLMap read(Document doc) {
        GMLMap result = new GMLMap();
        readBuildings(doc, result);
        readRoads(doc, result);
        readSpaces(doc, result);
        return result;
    }

    @Override
    public Document write(GMLMap map) {
        // Not implemented
        throw new RuntimeException("OrdnanceSurveyFormat.write not implemented");
    }

    private void readBuildings(Document doc, GMLMap result) {
        for (Object next : BUILDING_XPATH.selectNodes(doc)) {
            Logger.debug("Found building element: " + next);
            Element e = (Element)next;
            //            String fid = e.attributeValue("fid");
            //            long id = Long.parseLong(fid.substring(FID_PREFIX_LENGTH)); // Strip off the 'osgb' prefix
            String coordinatesString = ((Element)SHAPE_XPATH.evaluate(e)).getText();
            List<GMLDirectedEdge> edges = readEdges(coordinatesString, result);
            GMLBuilding b = result.createBuilding(edges);
        }
    }

    private void readRoads(Document doc, GMLMap result) {
        for (Object next : ROAD_XPATH.selectNodes(doc)) {
            Logger.debug("Found road element: " + next);
            Element e = (Element)next;
            //            String fid = e.attributeValue("fid");
            //            long id = Long.parseLong(fid.substring(FID_PREFIX_LENGTH)); // Strip off the 'osgb' prefix
            String coordinatesString = ((Element)SHAPE_XPATH.evaluate(e)).getText();
            Object inner = INNER_RING_XPATH.evaluate(e);
            if ((inner instanceof Collection) && ((Collection)inner).isEmpty()) {
                List<GMLDirectedEdge> edges = readEdges(coordinatesString, result);
                GMLRoad road = result.createRoad(edges);
            }
            else {
                Logger.debug("Inner ring found: ignoring");
                Logger.debug("Found: " + inner);
            }
        }
    }

    private void readSpaces(Document doc, GMLMap result) {
        /*
        for (Object next : SPACE_XPATH.selectNodes(doc)) {
            Logger.debug("Found space element: " + next);
            Element e = (Element)next;
            String fid = e.attributeValue("fid");
            long id = Long.parseLong(fid.substring(4)); // Strip off the 'osgb' prefix
            String coordinatesString = ((Element)SHAPE_XPATH.evaluate(e)).getText();
            List<GMLEdge> edges = readEdges(coordinatesString, result);
            result.createSpace(edges);
        }
        */
    }

    private List<GMLDirectedEdge> readEdges(String coordinatesString, GMLMap map) {
        List<GMLDirectedEdge> edges = new ArrayList<GMLDirectedEdge>();
        StringTokenizer tokens = new StringTokenizer(coordinatesString, " ");
        GMLCoordinates lastApex = null;
        GMLNode fromNode = null;
        GMLNode toNode = null;
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            GMLCoordinates nextApex = new GMLCoordinates(token);
            toNode = map.createNode(nextApex);
            if (lastApex != null) {
                edges.add(new GMLDirectedEdge(map.createEdge(fromNode, toNode), true));
            }
            lastApex = nextApex;
            fromNode = toNode;
        }
        return edges;
    }
}
