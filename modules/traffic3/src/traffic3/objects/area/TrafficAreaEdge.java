package traffic3.objects.area;

import java.util.ArrayList;
import java.util.List;

import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;

import traffic3.manager.WorldManager;
import traffic3.manager.WorldManagerException;
import traffic3.objects.TrafficObject;
import traffic3.objects.TrafficNode;

import org.util.xml.element.TagElement;

/**
 *
 */
public class TrafficAreaEdge extends TrafficObject {

    private String[] nodeIDs;
    private String[] areaIDs;

    private TrafficAreaNode[] nodes;
    private TrafficArea[] areas;
    private TrafficAreaNode center;

    private GeneralPath path;
    private List<Line2D> lineList = new ArrayList<Line2D>();

    /**
     * Constructor.
     * @param wm world manager
     */
    public TrafficAreaEdge(WorldManager wm) {
        super(wm);
    }

    /**
     * Constructor.
     * @param wm world manager
     * @param id id
     */
    public TrafficAreaEdge(WorldManager wm, String id) {
        super(wm, id);
    }

    public TrafficAreaEdge(WorldManager wm, String id, TrafficAreaNode[] nodes) {
        super(wm, id);
        setNodes(nodes);
    }

    public void createCache() {
        GeneralPath gp = new GeneralPath();
        double lx = nodes[0].getX();
        double ly = nodes[0].getY();
        gp.moveTo(lx, ly);
        List<Line2D> lList = new ArrayList<Line2D>();
        for (int i = 1; i < nodes.length; i++) {
            double x = nodes[i].getX();
            double y = nodes[i].getY();
            gp.lineTo(x, y);
            lList.add(new Line2D.Double(lx, ly, x, y));
            lx = x;
            ly = y;
        }
        path = gp;
        lineList = lList;
    }

    /**
     * set end nodes.
     * @param id1 node1 id
     * @param id2 node2 id
     */
    public void setNodeIDs(String... ids) {
        nodeIDs = ids;
        /*
        if (lineList.size() == 0) {
            TrafficAreaNode n1 = (TrafficAreaNode)getManager().getTrafficObject(nodeId1);
            TrafficAreaNode n2 = (TrafficAreaNode)getManager().getTrafficObject(nodeId2);
            lineList.add(new Line2D.Double(n1.getX(), n1.getY(), n2.getX(), n2.getY()));
            GeneralPath gp = new GeneralPath();
            gp.moveTo(n1.getX(), n1.getY());
            gp.lineTo(n2.getX(), n2.getY());
            path = gp;
        }
        */
        fireChanged();
    }

    public void setNodes(TrafficAreaNode... ns) {
        String[] nids = new String[ns.length];
        for (int i = 0; i < ns.length; i++) {
            ns[i].addChangeListener(new javax.swing.event.ChangeListener() {
                    public void stateChanged(javax.swing.event.ChangeEvent e) {
                        fireChanged();
                        createCache();
                    }
                });
            nids[i] = ns[i].getID();
        }
        nodeIDs = nids;
        nodes = ns;
        fireChanged();
    }    

    /**
     * length.
     * @return length
     */
    public double length() {
        double length = 0;
        for (int i = 1; i < nodes.length; i++) {
            length += nodes[i].getDistance(nodes[i - 1]);
        }
        return length;
    }


    /**
     * get center.
     * @return node
     */
    public TrafficNode getCenter() {
        if (center == null) {
            double x = (nodes[0].getX() + nodes[nodes.length - 1].getX()) / 2;
            double y = (nodes[0].getY() + nodes[nodes.length - 1].getY()) / 2;
            double z = 0;
            try {
                center = getManager().createAreaNode(x, y, z);
            }
            catch (WorldManagerException e) {
                e.printStackTrace();
            }
        }
        return center;
    }

    /*
    public void setLineList(ArrayList<Line2D> line_list) {
    GeneralPath gp = new GeneralPath();
    lineList = line_list;
    for(Line2D line : line_list)
        gp.append(line);
    path = path;
    }
    */

    /**
     * get node1 id.
     * @return node1 id
     */
    public String getEndNodeID1() {
        return nodeIDs[0];
    }

    /**
     * get node2 id.
     * @return node2 id
     */
    public String getEndNodeID2() {
        return nodeIDs[nodeIDs.length - 1];
    }

    /**
     * check object.
     * @throws Exception exception
     */
    public void checkObject() throws WorldManagerException {
        //directedAreaList = new TrafficArea[directedAreaIdList.size()];
        //for (int i = 0; i < directedAreaIdList.length; i++) {
        //    directedAreaList[i] = (TrafficArea)getManager().getTrafficObject(directedAreaIdList[i]);
        //}
        if (nodes == null) {
            TrafficAreaNode[] ns = new TrafficAreaNode[nodeIDs.length];
            for (int i = 0; i < nodeIDs.length; i++) {
                ns[i] = (TrafficAreaNode)getManager().getTrafficObject(nodeIDs[i]);
                if (ns[i] == null) {
                    throw new WorldManagerException("Error: Node cannot be found: " + nodeIDs + ": " + toString());
                }
            }
            setNodes(ns);
        }
        if (areas == null && areaIDs != null) {
            areas = new TrafficArea[areaIDs.length];
            for (int i = 0; i < areaIDs.length; i++) {
                areas[i] = (TrafficArea)getManager().getTrafficObject(areaIDs[i]);
                if (areas[i] == null) {
                    throw new WorldManagerException("Error: Edges area cannot be found: id:" + areaIDs[i] );
                }
            }
        }
        checked = true;
        createCache();
    }

    public int indexOf(TrafficAreaNode n) {
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i].equals(n)) {
                return i;
            }
        }
        return -1;
    }

    public boolean has(TrafficAreaNode... ns) {
        boolean match = true;
        for (int i = 0; i < ns.length && match; i++) {
            boolean submatch = false;
            for (int j = 0; j < nodes.length && !submatch; j++) {
                submatch = ns[i].equals(nodes[j]);
            }
            match = submatch;
        }
        return match;
    }

    /**
     * this AreaEdge has a line or not.
     * @param o line
     * @return has o or not
     */
    public boolean has(Line2D o) {
        for (int i = 0; i < lineList.size(); i++) {
            if ((lineList.get(i).getP1().getX() == o.getP1().getX() && lineList.get(i).getP2().getX() == o.getP2().getX())
                && (lineList.get(i).getP1().getY() == o.getP1().getY() && lineList.get(i).getP2().getY() == o.getP2().getY())) {
                return true;
            }
            if ((lineList.get(i).getP1().getX() == o.getP2().getX() && lineList.get(i).getP2().getX() == o.getP1().getX())
                && (lineList.get(i).getP1().getY() == o.getP2().getY() && lineList.get(i).getP2().getY() == o.getP1().getY())) {
                return true;
            }
            //System.out.print(lineList.get(i).getP1()+o.getP1().toString()+", ");
            //System.out.println(lineList.get(i).getP2()+o.getP2().toString());
        }
        return false;
    }

    /**
     * get next area.
     * @param now area
     * @return next area
     */
    public TrafficArea getNextArea(TrafficArea now) {
        if (areas == null) {
            return null;
        }
        if (areas.length == 1) {
            return null;
        }
        assert areas.length == 2 : "three directed area!";

        //alert("<html>"+now+" :</br> "+toLongString()+"</html>", "error");
        if (areas[0] == now) {
            return areas[1];
        }
        if (areas[1] == now) {
            return areas[0];
        }
        return null;
    }

    /**
     * get directed area.
     * @return directed areas
     */
    public TrafficArea[] getAreas() {
        return areas;
    }

    /**
     * set directed area id list.
     * @param ids ids
     */
    public void setAreaIDs(String... ids) {
        areaIDs = ids;
    }

    public void setAreas(TrafficArea... as) {
        String[] ids = new String[as.length];
        for (int i = 0; i < as.length; i++) {
            ids[i] = as[i].getID();
        }
        areas = as;
    }

    /**
     * get line list.
     * @return line list
     */
    public Line2D[] getLines() {
        return lineList.toArray(new Line2D[0]);
    }

    /**
     * distance.
     * @param x x
     * @param y y
     * @return distance
     */
    public double distance(double x, double y) {
        if (lineList.size() > 0) {
            double min = lineList.get(0).ptSegDist(x, y);
            for (int i = 1; i < lineList.size(); i++) {
                min = Math.min(min, lineList.get(i).ptSegDist(x, y));
            }
            return min;
        }
        else {
            return Double.NaN;
        }
    }

    public boolean isSameShape(TrafficAreaEdge o) {
        TrafficAreaNode[] myNodes = getNodes();
        TrafficAreaNode[] oNodes = o.getNodes();
        if (myNodes.length != oNodes.length)  {
            return false;
        }
        boolean match = true;
        for (int i = 0; i < myNodes.length; i++) {
            if (!myNodes[i].getID().equals(oNodes[i].getID())) {
                match = false;
                break;
            }
        }
        if (match) {
            return true;
        }
        for (int i = 0; i < myNodes.length; i++) {
            if (!myNodes[myNodes.length - 1 - i].getID().equals(oNodes[i].getID())) {
                return false;
            }
        }
        return true;
    }

    /**
     * set properties.
     * @param gmlElement gml element
     * @throws Exception exception
     */
    public void setProperties(TagElement gmlElement) throws WorldManagerException {
        // System.out.println("gml edge:"+gmlElement);
        TagElement[] ids = gmlElement.getTagChildren("gml:directedNode");
        String id1 = ids[0].getAttributeValue("xlink:href").replaceAll("#", "");
        String id2 = ids[1].getAttributeValue("xlink:href").replaceAll("#", "");
        TagElement centerLineOfTag = gmlElement.getTagChild("gml:centerLineOf");
        TagElement lineStringTag = centerLineOfTag.getTagChild("gml:LineString");
        String coordinatesText = lineStringTag.getChildValue("gml:coordinates");
        String[] coordinatesTextList = coordinatesText.split(" ");
        List<String> nIDList = new ArrayList<String>();
        GeneralPath gp = new GeneralPath();
        String[] xy = coordinatesTextList[0].split(",");
        double lx = Double.parseDouble(xy[0]);
        double ly = Double.parseDouble(xy[1]);
        gp.moveTo(lx, ly);
        nIDList.add(getManager().createAreaNode(lx, ly, 0).getID());
        for (int i = 1; i < coordinatesTextList.length; i++) {
            xy = coordinatesTextList[i].split(",");
            double x = Double.parseDouble(xy[0]);
            double y = Double.parseDouble(xy[1]);
            String nid = getManager().createAreaNode(x, y, 0).getID();
            if (!nid.equals(nIDList.get(nIDList.size() - 1))) {
                nIDList.add(nid);
            }
            gp.lineTo(x, y);
            lineList.add(new Line2D.Double(lx, ly, x, y));
            lx = x;
            ly = y;
        }
        path = gp;
        TagElement[] fids = gmlElement.getTagChildren("gml:directedFace");
        areaIDs = new String[fids.length];
        for (int i = 0; i < fids.length; i++) {
            areaIDs[i] = fids[i].getAttributeValue("xlink:href").replaceAll("#", "");
        }
        /*
        String[] eids = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            eids[i] = ids[i].getAttributeValue("xlink:href").replaceAll("#", "");
        }
        */
        setNodeIDs(nIDList.toArray(new String[0]));
    }

    /**
     * get path.
     * @return path
     */
    public GeneralPath getPath() {
        if (path == null) {
            createCache();
        }
        return path;
    }

    public TrafficAreaNode[] getNodes() {
        return nodes;
    }

    /**
     * description.
     * @return description
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("TrafficAreaEdge[");
        sb.append("id:").append(getID()).append(";");
        sb.append("nodes:{");
        if (nodes == null) {
            sb.append("null");
        }
        else {
            sb.append(nodes[0].getID());
            for (int i = 1; i < nodes.length; i++) {
                sb.append(",").append(nodes[i].getID());
            }
        }
        sb.append("};");
        sb.append("areas:");
        if (areas == null) {
            sb.append("null");
        }
        else if (areas.length == 0) {
            sb.append("{}");
        }
        else {
            sb.append("{");
            for (int i = 0; i < areas.length; i++) {
                if (areas[i] == null) {
                    sb.append(",").append("null");
                }
                else {
                    sb.append(",").append(areas[i].getID());
                }
            }
            sb.append("}");
        }
        sb.append(";");
        return sb.toString();
    }

    /**
     * detailed description.
     * @return detailed description
     */
    public String toLongString() {
        StringBuffer sb = new StringBuffer();
        sb.append("<div><div style='font-size:18;'>TrafficAreaEdge(id:" + getID() + ")</div>");
        sb.append("<div>id: ").append(getID()).append("</div>");
        sb.append("<div style='margin-left:50px;'>");
        for (int i = 0; i < nodes.length; i++) {
            sb.append("<div>").append(nodes[i]).append("</div>");
        }
        if (areas != null) {
            for (int i = 0; i < areas.length; i++) {
                sb.append("<div>area").append(i + 1).append(":  ").append(areas[i]).append("</div>");
            }
        }
        sb.append("</div>");
        sb.append("</div>");
        return sb.toString();
    }
}
