package maps.gml.formats;

import maps.gml.GMLMap;
import maps.gml.GMLNode;
import maps.gml.GMLDirectedEdge;
import maps.gml.GMLCoordinates;
import maps.gml.GMLMapFormat;
import maps.MapTools;
import maps.CoordinateConversion;
import maps.ScaleConversion;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Collections;

import rescuecore2.log.Logger;

/**
   A MapFormat that can handle maps from Japan's Geospatial Information Authority.
 */
public final class GeospatialInformationAuthorityFormat extends GMLMapFormat {
    /** Singleton instance. */
    public static final GeospatialInformationAuthorityFormat INSTANCE = new GeospatialInformationAuthorityFormat();

    private static final String FGD_NAMESPACE_URI = "http://fgd.gsi.go.jp/spec/2008/FGD_GMLSchema";

    private static final Namespace FGD_NAMESPACE = DocumentHelper.createNamespace("fgd", FGD_NAMESPACE_URI);

    private static final QName DATASET_QNAME = DocumentHelper.createQName("Dataset", FGD_NAMESPACE);
    private static final QName BUILDING_QNAME = DocumentHelper.createQName("BldL", FGD_NAMESPACE);
    private static final QName ROAD_QNAME = DocumentHelper.createQName("RdEdg", FGD_NAMESPACE);
    private static final QName LOC_QNAME = DocumentHelper.createQName("loc", FGD_NAMESPACE);
    private static final QName CURVE_QNAME = DocumentHelper.createQName("Curve", Common.GML_3_2_NAMESPACE);
    private static final QName SEGMENTS_QNAME = DocumentHelper.createQName("segments", Common.GML_3_2_NAMESPACE);
    private static final QName LINE_STRING_SEGMENT_QNAME = DocumentHelper.createQName("LineStringSegment", Common.GML_3_2_NAMESPACE);
    private static final QName POS_LIST_QNAME = DocumentHelper.createQName("posList", Common.GML_3_2_NAMESPACE);

    // Map from uri prefix to uri for XPath expressions and for output
    private static final Map<String, String> URIS = new HashMap<String, String>();

    static {
        URIS.put("gml", Common.GML_NAMESPACE_URI);
        URIS.put("xlink", Common.XLINK_NAMESPACE_URI);
        URIS.put("fgd", FGD_NAMESPACE_URI);
    }

    private GeospatialInformationAuthorityFormat() {
    }

    @Override
    public String toString() {
        return "Japan Geospatial Information Authority";
    }

    @Override
    public Map<String, String> getNamespaces() {
        return Collections.unmodifiableMap(URIS);
    }

    @Override
    public boolean isCorrectRootElement(String uri, String localName) {
        return FGD_NAMESPACE_URI.equals(uri) && "Dataset".equals(localName);
    }

    @Override
    public GMLMap read(Document doc) {
        GMLMap result = new GMLMap();
        readBuildings(doc, result);
        readRoads(doc, result);
        // Convert from lat/lon to metres
        double scale = 1.0 / MapTools.sizeOf1Metre((result.getMinY() + result.getMaxY()) / 2, (result.getMinX() + result.getMaxX()) / 2);
        CoordinateConversion conversion = new ScaleConversion(result.getMinX(), result.getMinY(), scale, scale);
        result.convertCoordinates(conversion);
        return result;
    }

    @Override
    public Document write(GMLMap map) {
        // Not implemented
        throw new RuntimeException("GeospatialInformationAuthorityFormat.write not implemented");
    }

    private void readBuildings(Document doc, GMLMap result) {
        List elements = doc.getRootElement().elements(BUILDING_QNAME);
        Logger.debug("Found " + elements.size() + " buildings");
        for (Object next : elements) {
            Element e = (Element)next;
            try {
                Element posList = e.element(LOC_QNAME).element(CURVE_QNAME).element(SEGMENTS_QNAME).element(LINE_STRING_SEGMENT_QNAME).element(POS_LIST_QNAME);
                String coords = posList.getText();
                List<GMLDirectedEdge> edges = readEdges(coords, result);
                result.createBuilding(edges);
            }
            catch (NullPointerException ex) {
                Logger.debug("Building with wonky outline found: " + ex);
            }
        }
    }

    private void readRoads(Document doc, GMLMap result) {
        List elements = doc.getRootElement().elements(ROAD_QNAME);
        Logger.debug("Found " + elements.size() + " roads");
        for (Object next : elements) {
            Element e = (Element)next;
            try {
                Element posList = e.element(LOC_QNAME).element(CURVE_QNAME).element(SEGMENTS_QNAME).element(LINE_STRING_SEGMENT_QNAME).element(POS_LIST_QNAME);
                String coords = posList.getText();
                createEdges(coords, result);
            }
            catch (NullPointerException ex) {
                Logger.debug("Road with wonky outline found: " + ex);
            }
        }
    }

    private List<GMLDirectedEdge> readEdges(String coordinatesString, GMLMap map) {
        List<GMLDirectedEdge> edges = new ArrayList<GMLDirectedEdge>();
        StringTokenizer tokens = new StringTokenizer(coordinatesString, " \t\n\r");
        GMLCoordinates lastApex = null;
        GMLNode fromNode = null;
        GMLNode toNode = null;
        while (tokens.hasMoreTokens()) {
            String north = tokens.nextToken();
            String east = tokens.nextToken();
            double x = Double.parseDouble(east);
            double y = Double.parseDouble(north);
            GMLCoordinates nextApex = new GMLCoordinates(x, y);
            toNode = map.createNode(nextApex);
            if (lastApex != null) {
                edges.add(new GMLDirectedEdge(map.createEdge(fromNode, toNode), true));
            }
            lastApex = nextApex;
            fromNode = toNode;
        }
        return edges;
    }

    private void createEdges(String coordinatesString, GMLMap map) {
        StringTokenizer tokens = new StringTokenizer(coordinatesString, " \t\n\r");
        GMLCoordinates lastApex = null;
        GMLNode fromNode = null;
        GMLNode toNode = null;
        while (tokens.hasMoreTokens()) {
            String north = tokens.nextToken();
            String east = tokens.nextToken();
            double x = Double.parseDouble(east);
            double y = Double.parseDouble(north);
            GMLCoordinates nextApex = new GMLCoordinates(x, y);
            toNode = map.createNode(nextApex);
            if (lastApex != null) {
                map.createEdge(fromNode, toNode);
            }
            lastApex = nextApex;
            fromNode = toNode;
        }
    }
}
