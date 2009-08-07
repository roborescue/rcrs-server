package traffic3.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import traffic3.manager.WorldManager;
import traffic3.manager.WorldManagerException;
import traffic3.objects.area.TrafficArea;
import traffic3.objects.area.TrafficAreaEdge;
import traffic3.objects.area.TrafficAreaNode;

import org.util.xml.parse.ElementParser;
import org.util.xml.parse.XMLParseException;
import org.util.xml.parse.policy.ParserPolicy;
import org.util.xml.element.TagElement;
import org.util.xml.element.Element;

/**
 *
 */
public class RCRSGML0 implements Parser {

    /**
     * get version.
     * @return version
     */
    public String getVersion() {
        return "RCRSGML[0]";
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
        catch (XMLParseException e) {
            e.printStackTrace();
            version[0] = null;
        }
        catch (IOException e) {
            e.printStackTrace();
            version[0] = null;
        }
        return getVersion().equals(version[0]);
    }

    /*
    public boolean isSupported2(InputStream in) {
        final String[] version = new String[1];
        try {
            ElementParser parser = new ElementParser(in);
            final ParserPolicy policy = new ParserPolicy() {
                    public Element allowElement(Element element) {
                        if (element.isTextElement()) {
                            return element;
                        }
                        TagElement tag = (TagElement)element;
                        if ("rcrs:Version".equals(tag.getKey())) {
                            version[0] = tag.getValue();
                        }
                        return null;
                    }
                    public boolean checkEndTag() { return true; }
                    public boolean forceEmptyTag(String key) { return false; }
                    public ParserPolicy getInnerPolicy(Element element) { return this; }
                    public String selectEncoding(String lastTagKey) { return null; }
                    public boolean throwExceptionIfDocumentHasError() { return true; }
                    public boolean finished() { return false; }
                };
            parser.setPolicy(policy);
            parser.parse();
        }
        catch (Exception e) {
            e.printStackTrace();
            version[0] = null;
        }
        return getVersion().equals(version[0]);
    }
    */

    /**
     * input.
     * @param wm world manager
     * @param in input stream
     * @throws Exception exception
     */
    public void input(final WorldManager wm, InputStream in) throws UnsupportedOperationException, XMLParseException, IOException, WorldManagerException {
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
                        if ("gml:Face".equals(tag.getKey())) {
                            TrafficArea tnn = new TrafficArea(wm, tag.getAttributeValue("gml:id"));
                            tnn.setProperties(tag);
                            wm.appendWithoutCheck(tnn);
                            // System.out.println("+n:"+tag.getAttributeValue("gml:id"));
                        }
                        else if ("gml:Node".equals(tag.getKey())) {
                            TrafficAreaNode tnn = new traffic3.objects.area.TrafficAreaNode(wm, tag.getAttributeValue("gml:id"));
                            tnn.setProperties(tag);
                            wm.appendWithoutCheck(tnn);
                        }
                        else if ("gml:Edge".equals(tag.getKey())) {
                            TrafficAreaEdge tnn = new TrafficAreaEdge(wm, tag.getAttributeValue("gml:id"));
                            tnn.setProperties(tag);
                            connectorList.add(tnn);
                            wm.appendWithoutCheck(tnn);
                        }
                        else if (!"connect".equals(tag.getKey()) && !"faces".equals(tag.getKey())) {
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
                    else if ("rcrs:Face".equals(tag.getKey())) {
                        return this;
                    }
                    else if ("rcrs:EdgeList".equals(tag.getKey())) {
                        return this;
                    }
                    else if ("rcrs:FSNodeList".equals(tag.getKey())) {
                        return this;
                    }
                    else if ("rcrs:FSEdgeList".equals(tag.getKey())) {
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
                    else if ("rcrs:Network".equals(tag.getKey())) {
                        return disableAllPolicy;
                    }
                    else if ("rcrs:FreeSpace".equals(tag.getKey())) {
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
        wm.check();
    }

    /**
     * output.
     * @param wm world manager
     * @param out output stream
     * @throws Exception exception
     */
    public void output(WorldManager wm, OutputStream out) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("unsupported");
    }
}
