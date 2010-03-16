package traffic3.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import traffic3.manager.WorldManager;
import traffic3.manager.WorldManagerException;
import traffic3.objects.area.TrafficArea;
import traffic3.objects.area.TrafficAreaEdge;
import traffic3.objects.area.TrafficAreaDirectedEdge;
import traffic3.objects.area.TrafficAreaNode;
//import traffic3.objects.TrafficObject;

import org.util.xml.parse.ElementParser;
import org.util.xml.parse.policy.ParserPolicy;
import org.util.xml.element.TagElement;
import org.util.xml.element.Element;
import org.util.xml.parse.XMLParseException;

/**
 *
 */
public class RCRSGML1 implements Parser {

    /**
     * get version.
     * @return version
     */
    public String getVersion() {
        return "RCRSGML[1]";
    }

    /**
     * get description.
     * @return description
     */
    public String getDescription() {
        return getVersion();
    }

    /**
     * is supported.
     * @param in input stream
     * @return is supported
     */
    public boolean isSupported(InputStream in) {
        final String[] version = new String[1];
        try {
            ElementParser parser = new ElementParser(in);
            final boolean[] flag = new boolean[]{false};
            final ParserPolicy policy = new ParserPolicy() {
                    public Element allowElement(Element element) {
                        if (element.isTextElement()) {
                            return element;
                        }
                        TagElement tag = (TagElement)element;
                        if ("rcrs:Version".equals(tag.getKey())) {
                            version[0] = tag.getValue();
                            flag[0] = true;
                        }
                        return null;
                    }
                    public boolean finished() { return flag[0]; }
                    public boolean checkEndTag() { return true; }
                    public boolean forceEmptyTag(String key) { return false; }
                    public ParserPolicy getInnerPolicy(Element element) { return this; }
                    public String selectEncoding(String lastTagKey) { return null; }
                    public boolean throwExceptionIfDocumentHasError() { return true; }
                };
            parser.setPolicy(policy);
            parser.parse();
        }
        catch (IOException e) {
            e.printStackTrace();
            version[0] = null;
        }
        catch (XMLParseException e) {
            e.printStackTrace();
            version[0] = null;
        }
        return getVersion().equals(version[0]);
    }

    /**
     * input.
     * @param worldManager world manager
     * @param in input stream
     * @throws Exception exception
     */
    public void input(final WorldManager worldManager, InputStream in) throws XMLParseException, IOException, WorldManagerException {
        ElementParser parser = new ElementParser(in);
        final WorldManagerException[] exception = new WorldManagerException[1];
        final List<TrafficAreaEdge> connectorList = new ArrayList<TrafficAreaEdge>();

        final ParserPolicy holdAllPolicy = new ParserPolicy() {
                public Element allowElement(Element element) {
                    if (exception[0] != null) {
                        return null;
                    }
                    return element;
                }
                public boolean checkEndTag() { return true; }
                public boolean forceEmptyTag(String key) { return false; }
                public ParserPolicy getInnerPolicy(Element element) { return this; }
                public String selectEncoding(String lastTagKey) { return null; }
                public boolean throwExceptionIfDocumentHasError() { return true; }
                public boolean finished() { return false; }
            };

        final ParserPolicy disableAllPolicy = new ParserPolicy() {
                public Element allowElement(Element element) {
                    return null;
                }
                public boolean checkEndTag() { return true; }
                public boolean forceEmptyTag(String key) { return false; }
                public ParserPolicy getInnerPolicy(Element element) { return this; }
                public String selectEncoding(String lastTagKey) { return null; }
                public boolean throwExceptionIfDocumentHasError() { return true; }
                public boolean finished() { return false; }
            };


        final ParserPolicy areaPolicy = new ParserPolicy() {
                public Element allowElement(Element element) {
                    if (exception[0] != null) {
                        return null;
                    }
                    if (!element.isTagElement()) {
                        return element;
                    }
                    TagElement tag = (TagElement)element;
                    try {
                        if ("rcrs:Face".equals(tag.getKey())) {
                            //System.err.println(tag);
                            TagElement gtag = tag.getTagChild("gml:Face");
                            TrafficArea tnn = new TrafficArea(worldManager, gtag.getAttributeValue("gml:id"));
                            tnn.setProperties(gtag);
                            tnn.setType(tag.getAttributeValue("type"));
                            worldManager.appendWithoutCheck(tnn);
                            // System.out.println("+n:"+tag.getAttributeValue("gml:id"));
                        }
                        else if ("gml:Node".equals(tag.getKey())) {
                            TrafficAreaNode tnn = new traffic3.objects.area.TrafficAreaNode(worldManager, tag.getAttributeValue("gml:id"));
                            tnn.setProperties(tag);
                            worldManager.appendWithoutCheck(tnn);
                        }
                        else if ("gml:Edge".equals(tag.getKey())) {
                            TrafficAreaEdge tnn = new TrafficAreaEdge(worldManager, tag.getAttributeValue("gml:id"));
                            tnn.setProperties(tag);
                            connectorList.add(tnn);
                            worldManager.appendWithoutCheck(tnn);
                        }
                        else if ("rcrs:BuildingProperty".equals(tag.getKey())) {
                            System.out.println("skipped: " + tag);
                        }
                        else if ("rcrs:EdgeList".equals(tag.getKey())) {
                            System.out.println("skipped: " + tag);
                        }
                        else {
                            System.out.println("skipped: " + tag.getKey());
                        }
                    }
                    catch (WorldManagerException e) {
                        exception[0] = e;
                    }
                    return null;
                }
                public ParserPolicy getInnerPolicy(Element element) {
                    if (!element.isTagElement()) {
                        return holdAllPolicy;
                    }
                    TagElement tag = (TagElement)element;
                    if ("rcrs:FaceList".equals(tag.getKey())) {
                        return this;
                    }
                    else if ("rcrs:EdgeList".equals(tag.getKey())) {
                        return this;
                    }
                    else if ("rcrs:NodeList".equals(tag.getKey())) {
                        return this;
                    }
                    else {
                        return holdAllPolicy;
                    }
                }
                public boolean checkEndTag() { return true; }
                public boolean forceEmptyTag(String key) { return false; }
                public String selectEncoding(String lastTagKey) { return null; }
                public boolean throwExceptionIfDocumentHasError() { return true; }
                public boolean finished() { return false; }
            };

        final ParserPolicy topologyPolicy = new ParserPolicy() {
                String encoding;
                public Element allowElement(Element element) {
                    if (exception[0] != null) {
                        return null;
                    }
                    if (encoding == null) {
                        if (element.isTagElement()) {
                            TagElement te = (TagElement)element;
                            if (te.isPI()) {
                                encoding = te.getAttributeValue("encoding");
                            }
                        }
                        if (encoding == null) {
                            encoding = "utf-8";
                        }
                    }
                    return element;
                }
                public ParserPolicy getInnerPolicy(Element element) {
                    if (!element.isTagElement()) {
                        return this;
                    }
                    TagElement tag = (TagElement)element;
                    //System.out.println("gml>"+tag.getKey());
                    if ("Topology".equals(tag.getKey())) {
                        return this;
                    }
                    else if ("rcrs:Area".equals(tag.getKey())) {
                        return areaPolicy;
                    }
                    return this;
                }
                public boolean checkEndTag() { return true; }
                public boolean forceEmptyTag(String key) { return false; }
                public String selectEncoding(String lastTagKey) { return encoding; }
                public boolean throwExceptionIfDocumentHasError() { return true; }
                public boolean finished() { return false; }
            };

        parser.setPolicy(topologyPolicy);
        parser.parse();

        if (exception[0] != null) {
            throw exception[0];
        }

        worldManager.check();
    }

    /**
     * output.
     * @param worldManager world manager
     * @param out output stream
     * @throws Exception exceptoin
     */
    public void output(WorldManager worldManager, OutputStream out) throws IOException {
        Map<String, String> map = new HashMap<String, String>();
        UniqueID nodeID = new UniqueID("node", map);
        UniqueID edgeID = new UniqueID("edge", map);
        UniqueID areaID = new UniqueID("area", map);

        String description = "no name";
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
        bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        bw.newLine();
        bw.write("<Topology xmlns=\"http://www.opengis.net/app\"");
        bw.newLine();
        bw.write("   xmlns:sch=\"http://www.ascc.net/xml/schematron\"");
        bw.newLine();
        bw.write("   xmlns:xlink=\"http://www.w3.org/1999/xlink\"");
        bw.newLine();
        bw.write("   xmlns:gml=\"http://www.opengis.net/gml\"");
        bw.newLine();
        bw.write("   xmlns:app=\"http://www.opengis.net/app\"");
        bw.newLine();
        bw.write("   xmlns:xsl=\"http://www.w3.org/2001/XMLSchema-instance\"");
        bw.newLine();
        bw.write("   xsl:schemaLocation=\"http://www.opengis.net/app/networkExamples.xsd\"");
        bw.newLine();
        bw.write("   xmlns:rcrs=\"http://sakura.meijo-u.ac.jp/rcrs\">");
        bw.newLine();
        bw.write("<rcrs:Version>" + getVersion() + "</rcrs:Version>");
        bw.newLine();
        bw.write("<rcrs:Description>" + description + "</rcrs:Description>");
        bw.newLine();
        bw.write("<rcrs:Area>");
        bw.newLine();

        bw.write("<rcrs:NodeList>");
        bw.newLine();
        for (TrafficAreaNode n : worldManager.getAreaNodeList()) {
            bw.write("<gml:Node gml:id=\"" + nodeID.map(n.getID()) + "\">");
            bw.newLine();
            bw.write("<gml:pointProperty>");
            bw.newLine();
            bw.write("<gml:Point>");
            bw.newLine();
            bw.write("<gml:coordinates>" + n.getX() + "," + n.getY() + "</gml:coordinates>");
            bw.newLine();
            bw.write("</gml:Point>");
            bw.newLine();
            bw.write("</gml:pointProperty>");
            bw.newLine();
            bw.write("</gml:Node>");
            bw.newLine();
        }
        bw.write("</rcrs:NodeList>");
        bw.newLine();

        bw.write("<rcrs:EdgeList>");
        bw.newLine();
        for (TrafficAreaEdge e : worldManager.getAreaConnectorEdgeList()) {
            bw.write("<gml:Edge gml:id=\"" + edgeID.map(e.getID()) + "\">");
            bw.newLine();
            for (TrafficAreaNode node : e.getNodes()) {
                if (node == null) {
                    System.err.println(node);
                    System.err.println(e.toString());
                }
                bw.write("<gml:directedNode orientation=\"+\" xlink:href=\"#" + nodeID.map(node.getID()) + "\"/>");
                bw.newLine();
            }
            for (TrafficArea face : e.getAreas()) {
                if (face == null) {
                    System.err.println("Edge's directed face is " + face);
                    System.err.println("Edge is " + e.toString());
                }
                bw.write("<gml:directedFace orientation=\"+\" xlink:href=\"#" + areaID.map(face.getID()) + "\"/>");
                bw.newLine();
            }
            bw.write("<gml:centerLineOf>");
            bw.newLine();
            bw.write("<gml:LineString>");
            bw.newLine();

            bw.write("<gml:coordinates>");
            java.awt.geom.Point2D p = null;
            for (java.awt.geom.Line2D line : e.getLines()) {
                if (p == null) {
                    p = line.getP1();
                    bw.write(p.getX() + "," + p.getY());
                }
                p = line.getP2();
                bw.write(" " + p.getX() + "," + p.getY());
            }
            bw.write("</gml:coordinates>");

            bw.newLine();
            bw.write("</gml:LineString>");
            bw.newLine();
            bw.write("</gml:centerLineOf>");
            bw.newLine();
            bw.write("</gml:Edge>");
            bw.newLine();
        }
        bw.write("</rcrs:EdgeList>");
        bw.newLine();

        bw.write("<rcrs:FaceList>");
        bw.newLine();
        for (TrafficArea area : worldManager.getAreaList()) {
            String type = area.getType();
            if (type == null) {
                type = "open space";
            }
            bw.write("<rcrs:Face type=\"" + type + "\">");
            bw.newLine();
            bw.write("<rcrs:BuildingProperty></rcrs:BuildingProperty>");
            bw.newLine();
            bw.write("<gml:Face gml:id=\"" + areaID.map(area.getID()) + "\">");
            bw.newLine();
            for (TrafficAreaDirectedEdge dedge : area.getDirectedEdges()) {
                TrafficAreaEdge edge = dedge.getEdge();
                bw.write("<gml:directedEdge orientation=\"");
                if (dedge.getDirection()) {
                    bw.write("+");
                } else  {
                    bw.write("-");
                }
                bw.write("\" xlink:href=\"#" + edgeID.map(edge.getID()) + "\"/>");
                bw.newLine();
            }
            bw.write("<gml:polygon>");
            bw.newLine();
            bw.write("<gml:LinearRing>");
            bw.newLine();
            bw.write("<gml:coordinates>");
            for (TrafficAreaDirectedEdge dedge : area.getDirectedEdges()) {
                TrafficAreaNode[] nodes = dedge.getNodes();
                for (int i = 0; i < nodes.length - 1; i++) {
                    TrafficAreaNode node = nodes[i];
                    bw.write(node.getX() + "," + node.getY() + " ");
                }
            }
            bw.write("</gml:coordinates>");
            bw.newLine();
            bw.write("</gml:LinearRing>");
            bw.newLine();
            bw.write("</gml:polygon>");
            bw.newLine();

            bw.write("</gml:Face>");
            bw.newLine();
            bw.write("</rcrs:Face>");
            bw.newLine();
            /*
              <gml:directedEdge orientation="+" xlink:href="#5803"/>
              <gml:directedEdge orientation="-" xlink:href="#5811"/>
              <gml:directedEdge orientation="+" xlink:href="#5807"/>
              <gml:directedEdge orientation="-" xlink:href="#5810"/>
            */
        }
        bw.write("</rcrs:FaceList>");
        bw.newLine();

        bw.write("</rcrs:Area>");
        bw.newLine();
        bw.write("</Topology>");
        bw.newLine();
        bw.flush();
        bw.close();
    }

    private class UniqueID {
        String pre = null;
        Map<String, String> map = null;
        int c = 1;
        UniqueID(String p, Map<String, String> m) {
            pre = p;
            map = m;
        }
        public String next() {
            return pre + (c++);
        }
        public String map(String id) {
            String m = map.get(id);
            if (m == null) {
                m = next();
                map.put(id, m);
            }
            return m;
        }
    }
}
