package traffic3.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import traffic3.manager.WorldManager;
import traffic3.manager.WorldManagerException;
import traffic3.objects.TrafficAgent;

import org.util.xml.parse.ElementParser;
import org.util.xml.parse.policy.ParserPolicy;
import org.util.xml.element.TagElement;
import org.util.xml.element.Element;
import org.util.xml.parse.XMLParseException;

/**
 *
 */
public class RCRSAgent1 implements Parser {

    private int agentCounter = 0;

    /**
     * get version.
     * @return version
     */
    public String getVersion() {
        return "RCRSAgent[1]";
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

    /**
     * input.
     * @param wm world manager
     * @param in input stream
     * @throws Exception exception
     */
    public void input(final WorldManager wm, InputStream in) throws XMLParseException, IOException, WorldManagerException {
        final WorldManagerException[] exception = new WorldManagerException[1];
        ElementParser parser = new ElementParser(in);
        final List<TrafficAgent> agentList = new ArrayList<TrafficAgent>();
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

        final ParserPolicy agentPolicy = new ParserPolicy() {
                public Element allowElement(Element element) {
                    if (exception[0] != null) {
                        return null;
                    }
                    if (!element.isTagElement()) {
                        return element;
                    }
                    TagElement tag = (TagElement)element;
                    Element result = null;
                    //                    try {
                        if ("agent".equals(tag.getKey())) {
                            String id = tag.getAttributeValue("id");
                            if (id == null) {
                                id = "agent" + (++agentCounter);
                            }
                            String type = tag.getAttributeValue("type");
                            final double radius = 200;
                            final double vLimit = 0.7;
                            /*
                            TrafficAgent agent = new TrafficAgent(wm, id, radius, vLimit);
                            agent.setType(type);
                            TagElement location = tag.getTagChild("location");
                            double x = Double.parseDouble(location.getChildValue("x"));
                            double y = Double.parseDouble(location.getChildValue("y"));
                            double z = Double.parseDouble(location.getChildValue("z", "0"));
                            agent.setLocation(x, y, z);
                            wm.appendWithoutCheck(agent);
                            */
                        }
                        else if ("location".equals(tag.getKey())) {
                            result = element;
                        }
                        else if ("x".equals(tag.getKey())) {
                            result = element;
                        }
                        else if ("y".equals(tag.getKey())) {
                            result = element;
                        }
                        else if ("area".equals(tag.getKey())) {
                            result = element;
                        }
                        else {
                            System.out.println("skipped: " + tag.getKey());
                        }
                        //                    }
                    //                    catch (WorldManagerException e) {
                    //                        exception[0] = e;
                    //                    }
                    return result;
                }
                public ParserPolicy getInnerPolicy(Element element) {
                    if (!element.isTagElement()) {
                        return holdAllPolicy;
                    }
                    return holdAllPolicy;
                }
                public boolean checkEndTag() { return true; }
                public boolean forceEmptyTag(String key) { return false; }
                public String selectEncoding(String lastTagKey) { return null; }
                public boolean throwExceptionIfDocumentHasError() { return true; }
                public boolean finished() { return false; }
            };

        final ParserPolicy agentListPolicy = new ParserPolicy() {
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
                    if ("agent_list".equals(tag.getKey())) {
                        return agentPolicy;
                    }
                    return this;
                }
                public boolean checkEndTag() { return true; }
                public boolean forceEmptyTag(String key) { return false; }
                public String selectEncoding(String lastTagKey) { return encoding; }
                public boolean throwExceptionIfDocumentHasError() { return true; }
                public boolean finished() { return false; }
            };
        parser.setPolicy(agentListPolicy);
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
    public void output(WorldManager wm, OutputStream out) throws IOException {
        Map<String, String> map = new HashMap<String, String>();
        UniqueID agentID = new UniqueID("agent", map);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
        bw.write("<agent_list xmlns:rcrs=\"http://sakura.meijo-u.ac.jp/..\">");
        bw.newLine();
        bw.write("   <rcrs:Version>" + getVersion() + "</rcrs:Version>");
        bw.newLine();
        for (TrafficAgent agent : wm.getAgentList()) {
            bw.write("   <agent id=\""+agentID.map(agent.getID())+"\" type=\"" + agent.getType() + "\">");
            bw.newLine();
            bw.write("      <location type=\"CoordinateAndArea\">");
            bw.newLine();
            bw.write("         <x>" + agent.getX() + "</x>");
            bw.newLine();
            bw.write("         <y>" + agent.getY() + "</y>");
            bw.newLine();
            bw.write("         <area>" + agent.getArea().getID() + "</area>");
            bw.newLine();
            bw.write("      </location>");
            bw.newLine();
            bw.write("   </agent>");
            bw.newLine();
        }
        bw.write("</agent_list>");
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
