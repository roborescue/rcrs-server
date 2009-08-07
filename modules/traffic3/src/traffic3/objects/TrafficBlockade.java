package traffic3.objects;

import java.util.ArrayList;
import java.util.List;
import java.awt.geom.Line2D;
import java.awt.geom.GeneralPath;

import traffic3.manager.WorldManager;
import traffic3.manager.WorldManagerException;
import traffic3.objects.area.TrafficAreaEdge;
import traffic3.objects.area.TrafficAreaNode;
import traffic3.objects.area.event.TrafficAreaListener;
import static traffic3.log.Logger.alert;
import org.util.xml.element.TagElement;

/**
 *
 */
public class TrafficBlockade extends TrafficObject {

    private List<TrafficAreaListener> areaListenerList = new ArrayList<TrafficAreaListener>();
    private boolean simulateAsOpenSpace = true;
    private String type;

    // cannot be null
    private double centerX;
    private double centerY;
    //private String[] neighbor_area_id_list_;

    // can be null
    private String[] connectorIdList;
    private String[] unconnectorIdList;
    private TrafficAreaNode[] nodeList;
    private GeneralPath shape;

    // these properties will be set at check
    private List<TrafficAreaEdge> connectorList = new ArrayList<TrafficAreaEdge>();
    private List<TrafficAreaEdge> unconnectorList = new ArrayList<TrafficAreaEdge>();
    private List<Line2D> unconnectEdgeList = new ArrayList<Line2D>();
    // private List<TrafficArea> neighborAreaList = new ArrayList<TrafficArea>();
    private List<Line2D> lineList = new ArrayList<Line2D>();

    /**
     * traffic blockade.
     * @param wm world manager
     * @param id id
     */
    public TrafficBlockade(WorldManager wm, String id) {
        super(wm, id);
    }

    /**
     * traffic blockade.
     * @param wm world manager
     * @param id id
     * @param cx center x
     * @param cy center y
     * @param xys xy list
     */
    public TrafficBlockade(WorldManager wm, String id, double cx, double cy, int[] xys) {
        super(wm, id);
        centerX = cx;
        centerY = cy;

        try {
            List<TrafficAreaNode> nodeBuf = new ArrayList<TrafficAreaNode>();
            List<String> directedEdgeIdList = new ArrayList<String>();
            List<String> ndirectedEdgeIdList = new ArrayList<String>();
            GeneralPath gp = new GeneralPath();
            double lastX = xys[0];
            double lastY = xys[1];
            gp.moveTo(lastX, lastY);
            TrafficAreaNode firstNode = new TrafficAreaNode(wm);
            firstNode.setLocation(lastX, lastY, 0);
            wm.appendWithoutCheck(firstNode);
            nodeBuf.add(firstNode);

            TrafficAreaNode lastNode = firstNode;
            TrafficAreaEdge edge;

            int length = xys.length / 2;
            double x;
            double y;
            for (int i = 1; i < length; i++) {
                x = xys[i * 2];
                y = xys[i * 2 + 1];
                gp.lineTo(x, y);
                lineList.add(new Line2D.Double(x, y, lastX, lastY));
                lastX = x;
                lastY = y;
            }
            x = xys[0];
            y = xys[1];
            lineList.add(new Line2D.Double(x, y, lastX, lastY));

            String edgeId = wm.getUniqueID("_");
            edge = new TrafficAreaEdge(wm, edgeId);
            edge.setNodeIDs(lastNode.getID(), firstNode.getID());
            edge.setAreaIDs(new String[]{id});
            wm.appendWithoutCheck(edge);
            ndirectedEdgeIdList.add(edgeId);
            nodeList = nodeBuf.toArray(new TrafficAreaNode[0]);
            connectorIdList = directedEdgeIdList.toArray(new String[0]);
            unconnectorIdList = ndirectedEdgeIdList.toArray(new String[0]);
            shape = gp;
        }
        catch (WorldManagerException e) {
            alert(e, "error");
        }
    }

    /**
     * check object.
     * @throws Exception exception
     */
    public void checkObject() throws WorldManagerException {

        for (int i = 0; i < connectorIdList.length; i++) {
            String edgeId = connectorIdList[i];
            TrafficAreaEdge edge = (TrafficAreaEdge)getManager().getTrafficObject(edgeId);
            connectorList.add(edge);
        }

        for (int i = 0; i < unconnectorIdList.length; i++) {
            String edgeId = unconnectorIdList[i];
            TrafficAreaEdge edge = (TrafficAreaEdge)getManager().getTrafficObject(edgeId);
            unconnectorList.add(edge);
        }
        /*
          for(String area_id : neighbor_area_id_list_) {
          TrafficArea area = (TrafficArea)getManager().getTrafficObject(area_id);
          neighborAreaList.add(area);
          }
        */
        createUnconnectEdgeList();
        checked = true;
    }

    /**
     * set center.
     * @param cx center x
     * @param cy center y
     */
    public void setCenter(double cx, double cy) {
        centerX = cx;
        centerY = cy;
    }

    /**
     * set type.
     * @param t type
     */
    public void setType(String t) {
        type = t;
    }

    /**
     * get type.
     * @return type
     */
    public String getType() {
        return type;
    }

    /**
     * get center x.
     * @return center x
     */
    public double getCenterX() {
        return centerX;
    }

    /**
     * get center y.
     * @return center y
     */
    public double getCenterY() {
        return centerY;
    }

    /**
     * set line list.
     * @param xys xy list
     */
    public void setLineList(int[] xys) {
        GeneralPath gp = new GeneralPath();
        int length = xys.length / 2;
        List<Line2D> lList = new ArrayList<Line2D>();
        double x;
        double y;
        double lastX = xys[0];
        double lastY = xys[1];
        gp.moveTo(lastX, lastY);
        for (int i = 1; i < length; i++) {
            x = xys[i * 2];
            y = xys[i * 2 + 1];
            gp.lineTo(x, y);
            lList.add(new Line2D.Double(x, y, lastX, lastY));
            lastX = x;
            lastY = y;
        }
        shape = gp;
        x = xys[0];
        y = xys[1];
        lList.add(new Line2D.Double(x, y, lastX, lastY));
        lineList = lList;
        fireChanged();
    }

    /**
     * get line list.
     * @return line
     */
    public Line2D[] getLineList() {
        return lineList.toArray(new Line2D[0]);
    }

    /*
    public void setSimulateAsOpenSpace(boolean simulate_as_open_space) {
        simulateAsOpenSpace = simulate_as_open_space;
    }
    public boolean isSimulateAsOpenSpace() {
        return simulateAsOpenSpace;
    }
    */

    /**
     * set properties.
     * @param gmlElement gml
     * @throws Exception exception
     */
    public void setProperties(TagElement gmlElement) throws WorldManagerException {
        // alert("gml:" + gmlElement, "error");
        String coordinatesText = gmlElement.getTagChild("gml:polygon").getTagChild("gml:LinearRing").getChildValue("gml:coordinates");
        String[] coordinatesTextList = coordinatesText.split(" ");
        GeneralPath gp = new GeneralPath();
        String[] xyText = coordinatesTextList[0].split(",");
        double x = Double.parseDouble(xyText[0]);
        double y = Double.parseDouble(xyText[1]);
        nodeList = new TrafficAreaNode[coordinatesTextList.length];
        gp.moveTo(x, y);
        TrafficAreaNode pos = new TrafficAreaNode(getManager());
        pos.setLocation(x, y, 0);
        getManager().appendWithoutCheck(pos);
        nodeList[0] = pos;
        double xSum = x;
        double ySum = y;
        for (int i = 1; i < coordinatesTextList.length; i++) {
            xyText = coordinatesTextList[i].split(",");
            x = Double.parseDouble(xyText[0]);
            y = Double.parseDouble(xyText[1]);
            xSum += x;
            ySum += y;
            gp.lineTo(x, y);
            pos = getManager().createAreaNode(x, y, 0);
            nodeList[i] = pos;
        }
        centerX = xSum / coordinatesTextList.length;
        centerY = ySum / coordinatesTextList.length;

        TagElement[] directedEdgeTagList = gmlElement.getTagChildren("gml:directedEdge");
        List<String> directedEdgeIdList = new ArrayList<String>();
        List<String> ndirectedEdgeIdList = new ArrayList<String>();
        for (int i = 0; i < directedEdgeTagList.length; i++) {
            String de = directedEdgeTagList[i].getAttributeValue("xlink:href").replaceAll("#", "");
            String or = directedEdgeTagList[i].getAttributeValue("orientation");
            if ("+".equals(or)) {
                directedEdgeIdList.add(de);
            }
            else {
                ndirectedEdgeIdList.add(de);
            }
        }
        connectorIdList = directedEdgeIdList.toArray(new String[0]);
        unconnectorIdList = ndirectedEdgeIdList.toArray(new String[0]);

        /*
          TagElement[] directed_area_tag_list = gmlElement.getTagChildren("gml:directedFace");
          List<String> directed_area_id_list = new ArrayList<String>();
          for(int i=0; i<directed_area_tag_list.length; i++) {
          String de = directed_area_tag_list[i].getAttributeValue("xlink:href").replaceAll("#", "");
          String or = directed_area_tag_list[i].getAttributeValue("orientation");
          if("+".equals(or))
          directed_area_id_list.add(de);
          }
          neighbor_area_id_list_ = directed_area_id_list.toArray(new String[0]);
          alert(neighbor_area_id_list_.length ,"error");
        */

        shape = gp;
    }

    private void createUnconnectEdgeList() {
        for (TrafficAreaEdge edge : unconnectorList) {
            for (Line2D line : edge.getLines()) {
                unconnectEdgeList.add(line);
            }
        }
    }

    /**
     * get connector edge list.
     * @return connector edge list
     */
    public TrafficAreaEdge[] getConnectorEdgeList() {
        return connectorList.toArray(new TrafficAreaEdge[0]);
    }

    /**
     * get unconnector edge list.
     * @return unconnector edge list
     */
    public TrafficAreaEdge[] getUnConnectorEdgeList() {
        return unconnectorList.toArray(new TrafficAreaEdge[0]);
    }

    /**
     * get uncconected edge list.
     * @return unconnected edge list
     */
    public Line2D[] getUnconnectedEdgeList() {
        return unconnectEdgeList.toArray(new Line2D[0]);
    }

    /**
     * get shape.
     * @return shape
     */
    public GeneralPath getShape() {
        return shape;
    }

    /**
     * contains.
     * @param x x
     * @param y y
     * @param z z
     * @return contain contain
     */
    public boolean contains(double x, double y, double z) {
        return shape.contains(x, y);
    }

    /**
     * get node list.
     * @return node list
     */
    public TrafficAreaNode[] getNodeList() {
        return nodeList;
    }

    /**
     * add traffic area listener.
     * @param listener listener
     */
    public void addTrafficAreaListener(TrafficAreaListener listener) {
        areaListenerList.add(listener);
    }

    /**
     * remove traffic area listener.
     * @param listener listener
     */
    public void removeTrafficAreaListener(TrafficAreaListener listener) {
        areaListenerList.remove(listener);
    }

    /**
     * explanation.
     * @return explanation
     */
    public String toString() {
        return "TrafficBlockade[id:" + getID() + ";type:" + getType() + ";]";
    }

    /**
     * detailed explanation.
     * @return detailed explanation
     */
    public String toLongString() {
        StringBuffer sb = new StringBuffer();
        sb.append("<div><div style='font-size:18;'>TrafficBlockade(id:" + getID() + ")</div>");
        sb.append("type: " + getType() + "<br/>");
        if (isChecked()) {
            sb.append("checked object.<br/>");
        }
        else {
            sb.append("unchecked object.<br/>");
        }
        sb.append("center: (" + centerX + "," + centerY + ")<br/>");

        sb.append("<div style='font-size:15;'>Node List</div>");
        sb.append("<div style='font-size:12;padding:0 0 0 30px;'>");
        for (int i = 0; i < nodeList.length; i++) {
            sb.append(nodeList[i]).append("<br/>");
        }
        sb.append("</div>");

        sb.append("<div style='font-size:15;'>Connected Edge List</div>");
        sb.append("<div style='font-size:12;padding:0 0 0 30px;'>");
        for (int i = 0; i < connectorList.size(); i++) {
            TrafficAreaEdge tae = connectorList.get(i);
            if (tae == null) {
                continue;
            }
            sb.append(tae.toLongString()).append("<br/>");
        }
        sb.append("</div>");

        sb.append("<div style='font-size:15;'>Unconnected Edge List</div>");
        sb.append("<div style='font-size:12;padding:0 0 0 30px;'>");
        for (int i = 0; i < unconnectorList.size(); i++) {
            sb.append(unconnectorList.get(i).toString()).append("<br/>");
        }
        sb.append("</div>");

        sb.append("</div>");
        return sb.toString();
    }
}
