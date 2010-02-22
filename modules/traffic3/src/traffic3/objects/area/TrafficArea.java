package traffic3.objects.area;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.awt.geom.Line2D;
import java.awt.geom.GeneralPath;

import traffic3.manager.WorldManager;
import traffic3.manager.WorldManagerException;
import traffic3.objects.TrafficObject;
import traffic3.objects.TrafficAgent;
import traffic3.objects.TrafficBlockade;
import static traffic3.log.Logger.log;
import static traffic3.log.Logger.alert;
import traffic3.objects.area.event.TrafficAreaListener;
import traffic3.objects.area.event.TrafficAreaEvent;
import org.util.xml.element.TagElement;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityListener;
import rescuecore2.worldmodel.Property;
import rescuecore2.log.Logger;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.StandardWorldModel;

/**
 * Traffic area.
 */
public class TrafficArea extends TrafficObject {
    private String[] edgeIDs;
    private String type;
    private List<TrafficAreaListener> areaListenerList = new ArrayList<TrafficAreaListener>();
    private List<TrafficAgent> agentList = new ArrayList<TrafficAgent>();
    private List<TrafficBlockade> blockadeList = new ArrayList<TrafficBlockade>();

    private TrafficAreaDirectedEdge[] directedEdges;
    private double[] center = new double[2];

    private GeneralPath shape;
    private TrafficAgent[] agentListBuf = null;
    private Line2D[] getLinesCACHE = null;
    private Line2D[] getNeighborLinesCACHE = null;

    private List<TrafficEdge> edges;
    private List<Line2D> blocking;
    private List<Line2D> blockades;
    private List<Line2D> allBlockingLines;

    private Area area;
    private StandardWorldModel world;

    /**
     * Constructor.
     * @param worldManager world manager
     * @param id id of this object (it must be unique in world manager WorldManager.getUniqueID("type"))
     */
    public TrafficArea(WorldManager worldManager, String id) {
        super(worldManager, id);
        edgeIDs = new String[]{};
    }

    /**
     * Constructor.
     * @param worldManager world manager
     * @param id id of this object (it must be unique in world manager WorldManager.getUniqueID("type"))
     */
    public TrafficArea(WorldManager worldManager, String id, TrafficAreaNode[] nodes) {
        super(worldManager, id);
        setNodes(nodes);
        //edgeIDs = new String[]{};
    }

    /**
     * <pre>
     * Constructor.
     *
     *          /       /
     *         /       /
     *        /  a3   /
     *       /       /
     * ----[p1]----[p4]----
     *      |       |
     *   a1 |   a   |  a2
     *      |       |
     * ----[p2]----[p3]----
     *
     *
     * apexis p1, p2, p3, p4
     * where p1 = (x1, y1)
     *       p2 = (x2, y2)
     *       p3 = (x3, y3)
     *       p4 = (x4, y4)
     *
     * xyList {x1, y1, x2, y2, x3, y3, x4, y4}
     *         ------  ------  ------  ------
     *           p1      p2      p3      p4
     *            |------||------||------||------|
     * nexts  {      a1,    null,    a2,     a3  }
     * </pre>
     *
     * @param worldManager world manager
     * @param id id of this object (it must be unique in world manager WorldManager.getUniqueID("type"))
     * @param cx x of center of this area
     * @param cy y of center of this area
     * @param xyList apexces of this area
     * @param nexts nexts area it must be correspnds xyList
     */
    public TrafficArea(WorldManager worldManager, String id, double cx, double cy, int[] xyList, String[] nexts, final Area area, StandardWorldModel world) {
        super(worldManager, id);
        edges = new ArrayList<TrafficEdge>();
        this.area = area;
        this.world = world;


        edgeIDs = new String[]{};

        center[0] = cx;
        center[1] = cy;
        //xyList
        //nexts
        try {
            List<TrafficAreaNode> nodeList = new ArrayList<TrafficAreaNode>();
            int apexesLength = xyList.length / 2;
            Logger.debug("Creating traffic area " + id + " with " + apexesLength + " apexes");
            Logger.debug("Neighbours: ");
            for (String next : nexts) {
                Logger.debug("  " + next);
            }
            for (int i = 0; i < apexesLength; i++) {
                double x = xyList[i * 2];
                double y = xyList[i * 2 + 1];
                TrafficAreaNode node = worldManager.createAreaNode(x, y, 0);
                nodeList.add(node);
                Logger.debug("Node " + i + ": " + node);
            }

            String tmp = nexts[0];
            List<String> edgeIDList = new ArrayList<String>();
            for (int index = 0; index < nexts.length; ++index) {
                Logger.debug("Processing edge " + index);
                String next = nexts[index];
                TrafficAreaNode[] nodes = new TrafficAreaNode[2];
                nodes[0] = nodeList.get(index);
                nodes[1] = nodeList.get(index == nexts.length - 1 ? 0 : index + 1);
                Line2D line = new Line2D.Double(nodes[0].getX(), nodes[0].getY(), nodes[1].getX(), nodes[1].getY());

                TrafficEdge tEdge = new TrafficEdge(line, next != null);
                edges.add(tEdge);
                Logger.debug("Traffic edge: " + tEdge);


                String eid = worldManager.getUniqueID("_");
                //                TrafficAreaNode[] nodes = list.toArray(new TrafficAreaNode[0]);
                //                Logger.debug("Node list: " + list);
                TrafficAreaEdge edge = new TrafficAreaEdge(worldManager, eid, nodes);
                if (next == null || "rcrs(-1)".equals(next)) {
                    edge.setAreaIDs(getID());
                    worldManager.appendWithoutCheck(edge);
                    edgeIDList.add(edge.getID());
                }
                else {
                    TrafficAreaEdge matchedEdge = null;
                    TrafficArea narea = (TrafficArea)worldManager.getTrafficObject(next);
                    if (narea != null) {
                        for (String neid : narea.getEdgeIDs()) {
                            TrafficAreaEdge nedge = (TrafficAreaEdge)worldManager.getTrafficObject(neid);
                            if (edge.isSameShape(nedge)) {
                                matchedEdge = nedge;
                            }
                        }
                    }
                    if (matchedEdge != null) {
                        edgeIDList.add(matchedEdge.getID());
                    }
                    else {
                        worldManager.appendWithoutCheck(edge);
                        edgeIDList.add(edge.getID());
                    }
                    edge.setAreaIDs(getID(), next); 
                    //System.out.println(edge + " == [" + getID() + " , " + next + "]");
                }
            }
            edgeIDs = edgeIDList.toArray(new String[0]);

            blocking = null;
            blockades = null;
            allBlockingLines = null;

            area.addEntityListener(new EntityListener() {
                    @Override
                    public void propertyChanged(Entity e, Property p, Object oldValue, Object newValue) {
                        if (p == area.getBlockadesProperty()) {
                            blockades = null;
                            allBlockingLines = null;
                        }
                        if (p == area.getEdges()) {
                            blocking = null;
                            allBlockingLines = null;
                        }
                    }
                });
            
            //directedEdges
        }
        catch (WorldManagerException e) {
            log(e, "error");
        }


        /*
        try {
            List<TrafficAreaNode> nodeList = new ArrayList<TrafficAreaNode>();
            int apexesLength = xyList.length / 2;
            for (int i = 0; i < apexesLength; i++) {
                int x = xyList[i * 2];
                int y = xyList[i * 2 + 1];
                TrafficAreaNode node = worldManager.createAreaNode(x, y, 0);
                nodeList.add(node);
            }
            String tmp = nexts[0];
            int index = 0;
            List<String> edgeIDList = new ArrayList<String>();
            while (index < nexts.length - 1) {
                String next = nexts[index];
                List<TrafficAreaNode> list = new ArrayList<TrafficAreaNode>();
                boolean loop = true;
                for (int i = 0; loop; i++) {
                    String newNext = nexts[index + i];
                    //System.out.println(index + ":" + i + ":" + nexts.length + "[" + next + "|" + newNext + "]");
                    list.add(nodeList.get(index + i));
                    if (index + i >= nexts.length - 1) {
                        if (index + i == nexts.length - 1) {
                            list.add(nodeList.get(0));
                        }
                        index += i;
                        loop = false;
                    }
                    else if ((next == newNext) || (next != null && next.equals(newNext)) || (newNext != null && newNext.equals(next))) {
                        
                    }
                    else {
                        index += i;
                        loop = false;
                    }
                }
                String eid = worldManager.getUniqueID("_");
                TrafficAreaNode[] nodes = list.toArray(new TrafficAreaNode[0]);
                //System.out.println(nodeList+"||"+nodes);
                TrafficAreaEdge edge = new TrafficAreaEdge(worldManager, eid, nodes);
                edgeIDList.add(edge.getID());
                worldManager.appendWithoutCheck(edge);
            }
            edgeIDs = edgeIDList.toArray(new String[0]);
            //directedEdges
        }
        catch (WorldManagerException e) {
            log(e, "error");
        }
        */

        /*
        try {
            List<TrafficAreaNode> nodeBuf = new ArrayList<TrafficAreaNode>();
            List<String> directedEdgeIdList = new ArrayList<String>();
            List<String> notDirectedEdgeIdList = new ArrayList<String>();
            GeneralPath gp = new GeneralPath();

            double lastX = xyList[0];
            double lastY = xyList[1];
            gp.moveTo(lastX, lastY);
            TrafficAreaNode firstNode = worldManager.createAreaNode(lastX, lastY, 0);
            nodeBuf.add(firstNode);

            TrafficAreaNode lastNode = firstNode;
            TrafficAreaEdge edge;
            for (int i = 1; i < nexts.length; i++) {
                double x = xyList[i * 2];
                double y = xyList[i * 2 + 1];
                if (lastX == x && lastY == y) continue;
                gp.lineTo(x, y);

                TrafficAreaNode node = worldManager.createAreaNode(x, y, 0);

                String nextId = nexts[i - 1];
                if ("rcrs(-1)".equals(nextId)) { // not connector
                    String edgeId = worldManager.getUniqueID("_");
                    edge = new TrafficAreaEdge(worldManager, edgeId);
                    edge.setNodeIDs(lastNode.getID(), node.getID());
                    edge.setAreaIDs(new String[]{id});
                    notDirectedEdgeIdList.add(edgeId);
                    worldManager.appendWithoutCheck(edge);
                }
                // connector and it's not exists
                else if (worldManager.getTrafficObject(nextId) == null) {
                    String edgeId = worldManager.getUniqueID("_");
                    edge = new TrafficAreaEdge(worldManager, edgeId);
                    edge.setNodeIDs(lastNode.getID(), node.getID());
                    edge.setAreaIDs(new String[]{id, nextId});
                    directedEdgeIdList.add(edgeId);
                    worldManager.appendWithoutCheck(edge);
                }
                else {  // connector and it's already exists
                    String edgeId = worldManager.getUniqueID("_");
                    edge = new TrafficAreaEdge(worldManager, edgeId);
                    edge.setNodeIDs(lastNode.getID(), node.getID());
                    edge.setAreaIDs(id, nextId);
                    directedEdgeIdList.add(edgeId);
                    worldManager.appendWithoutCheck(edge);
                }

                nodeBuf.add(node);
                lastNode = node;
                lastX = x;
                lastY = y;
            }
            String edgeId = worldManager.getUniqueID("_");
            edge = new TrafficAreaEdge(worldManager, edgeId);
            edge.setNodeIDs(lastNode.getID(), firstNode.getID());
            edge.setAreaIDs(new String[]{id});
            worldManager.appendWithoutCheck(edge);
            notDirectedEdgeIdList.add(edgeId);
            //nodeList = nodeBuf.toArray(new TrafficAreaNode[0]);
            //connectorIdList = directedEdgeIdList.toArray(new String[0]);
            //unconnectorIdList = notDirectedEdgeIdList.toArray(new String[0]);
            shape = gp;
        }
        catch (WorldManagerException e) {
            alert(e, "error");
        }
        */
    }

    /*
    private class EdgeBuffer {
        EdgeBuffer (TrafficAreanode[] nodes, String type) {
            
        }
    }
    */

    public List<TrafficEdge> getEdges() {
        return edges;
    }

    public List<Line2D> getBlockingLines() {
        if (blocking == null) {
            blocking = new ArrayList<Line2D>();
            for (TrafficEdge edge : edges) {
                if (!edge.isPassable()) {
                    blocking.add(edge.getLine());
                }
            }
        }
        return blocking;
    }

    public List<Line2D> getBlockadeLines() {
        if (blockades == null) {
            blockades = new ArrayList<Line2D>();
            if (area.isBlockadesDefined()) {
                for (EntityID blockadeID : area.getBlockades()) {
                    Blockade b = (Blockade)world.getEntity(blockadeID);
                    int[] apexes = b.getApexes();
                    for (int i = 0; i < apexes.length - 2; i += 2) {
                        blockades.add(new Line2D.Double(apexes[i], apexes[i + 1], apexes[i + 2], apexes[i + 3]));
                    }
                    // Close the shape
                    blockades.add(new Line2D.Double(apexes[apexes.length - 2], apexes[apexes.length - 1], apexes[0], apexes[1]));
                }
            }
        }
        return blockades;
    }

    public List<Line2D> getAllBlockingLines() {
        if (allBlockingLines == null) {
            allBlockingLines = new ArrayList<Line2D>();
            allBlockingLines.addAll(getBlockingLines());
            allBlockingLines.addAll(getBlockadeLines());
        }
        return allBlockingLines;
    }


    /**
     * add blockade.
     * @param blockade blockade
     */
    public void addBlockade(TrafficBlockade blockade) {
        blockadeList.add(blockade);

        getNeighborLinesCACHE = null;
        for (TrafficArea na : getNeighborAreas()) {
            na.clearNeighborWallListCache();
        }
    }

    public void setNodes(TrafficAreaNode... ns) {
        TrafficAreaNode[] nstmp = new TrafficAreaNode[ns.length + 1];
        for (int i = 0; i <= ns.length; i++) {
            nstmp[i] = ns[i % ns.length];
        }
        ns = nstmp;
        // <?>
        List<TrafficAreaNode> nodeList = new ArrayList<TrafficAreaNode>();
        TrafficAreaEdge edge = new TrafficAreaEdge(getManager(), getManager().getUniqueID("_"), ns);
        edge.setAreas(this);
        try {
            getManager().appendWithoutCheck(edge);
            setDirectedEdges(new TrafficAreaDirectedEdge(getManager(), getManager().getUniqueID("_"), edge, true));
        }
        catch (Exception e) {
            log(e);
        }
        createCache();
    }

    public String[] getEdgeIDs() {
        return edgeIDs;
    }

    public void setDirectedEdges(TrafficAreaDirectedEdge... des) {
        directedEdges = des;
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < des.length; i++) {
            list.add(des[i].getEdge().getID());
            des[i].addChangeListener(new javax.swing.event.ChangeListener() {
                    public void stateChanged(javax.swing.event.ChangeEvent e) {
                        createCache();
                        getNeighborLinesCACHE = null;
                        fireChanged();
                    }
                });
            //directedEdgeList.addChangeListener();
            //directedEdgeList.add(des[i]);
        }
        edgeIDs = list.toArray(new String[0]);
        createCache();
    }

    public TrafficArea[] getNeighborAreas() {
        List<TrafficArea> list = new ArrayList<TrafficArea>();
        for (TrafficAreaDirectedEdge dedge : directedEdges) {
            TrafficArea[] areas = dedge.getAreas();
            if (areas.length >= 2) {
                for (TrafficArea area : areas) {
                    if (area != this) {
                        list.add(area);
                    }
                }
            }
        }
        return list.toArray(new TrafficArea[0]);
    }

    /**
     * remove blockade.
     * @param blockade blockade
     */
    public void removeBlockade(TrafficBlockade blockade) {
        blockadeList.remove(blockade);

        getNeighborLinesCACHE = null;
        for (TrafficArea na : getNeighborAreas()) {
            na.clearNeighborWallListCache();
        }
    }

    /**
     * set blockade list.
     * @param blockadeList blockade list
     */
    public void setBlockadeList(TrafficBlockade[] blockades) {
        blockadeList.clear();
        for (TrafficBlockade blockade : blockades) {
            blockadeList.add(blockade);
        }

        getNeighborLinesCACHE = null;
        for (TrafficArea na : getNeighborAreas()) {
            na.clearNeighborWallListCache();
        }
    }

    public TrafficAreaNode[] getNodes() {
        List<TrafficAreaNode> list = new ArrayList<TrafficAreaNode>();
        for (int j = 0; j <directedEdges.length; j++) {
            TrafficAreaDirectedEdge dedge = directedEdges[j];
            TrafficAreaNode[] nodes = dedge.getNodes();
            for (int i = 0; i < nodes.length - 1; i++) {
                list.add(nodes[i]);
            }
        }
        return list.toArray(new TrafficAreaNode[0]);
    }

    /**
     * get blockade list.
     * @return blockade list
     */
    public TrafficBlockade[] getBlockadeList() {
        return blockadeList.toArray(new TrafficBlockade[0]);
    }

    /**
     * check object.
     * @throws Exception has some errors.
     */
    public void checkObject() throws WorldManagerException {
        if (directedEdges == null) {
            if (edgeIDs.length < 1) {
                throw new WorldManagerException("length of Area's edge < 1.");
            }
            TrafficAreaDirectedEdge[] dEdges = new TrafficAreaDirectedEdge[edgeIDs.length];
            for (int i = 0; i < edgeIDs.length; i++) {
                TrafficAreaEdge edge = (TrafficAreaEdge)getManager().getTrafficObject(edgeIDs[i]);
                if (edge == null) {
                    throw new WorldManagerException("cannot find " + edge + " of " + this);
                }
                else if (edge.getNodes() == null) {
                    throw new WorldManagerException("edge's nodes is null: edge: " + edge + ", area: " + this);
                }
                dEdges[i] = new TrafficAreaDirectedEdge(getManager(), getManager().getUniqueID("_"), edge, true);
            }
            if (dEdges.length > 1) {
                if (dEdges[0].getFirstNode().equals(dEdges[1].getFirstNode()) || dEdges[0].getFirstNode().equals(dEdges[1].getLastNode())) {
                dEdges[0].setDirection(!dEdges[0].getDirection());
                }
                for (int i = 1; i < dEdges.length; i++) {
                    if (!dEdges[i - 1].getLastNode().equals(dEdges[i].getFirstNode())) {
                        dEdges[i].setDirection(!dEdges[i].getDirection());
                    }
                    if (!dEdges[i - 1].getLastNode().equals(dEdges[i].getFirstNode())) {
                        for (int j = i; j < dEdges.length; j++) {
                            if (dEdges[i - 1].getLastNode().equals(dEdges[j].getFirstNode())) {
                                TrafficAreaDirectedEdge tmp = dEdges[i];
                                dEdges[i] = dEdges[j];
                                dEdges[j] = tmp;
                            }
                            else if (dEdges[i - 1].getLastNode().equals(dEdges[j].getLastNode())) {
                                TrafficAreaDirectedEdge tmp = dEdges[i];
                                dEdges[i] = dEdges[j];
                                dEdges[j] = tmp;
                                dEdges[i].setDirection(!dEdges[i].getDirection());
                            }
                        }
                    }
                }
            }
            setDirectedEdges(dEdges);
        }
        checked = true;
        createCache();
    }

    /**
     * set type of this area.
     * @param t type
     */
    public void setType(String t) {
        type = t;
    }

    /**
     * get type of this area.
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
        return center[0];
    }

    /**
     * get center y.
     * @return center y
     */
    public double getCenterY() {
        return center[1];
    }

    /**
     * get distance from an area.
     * return the distance between center points of the areas.
     * @param area area
     * @return distance from the area
     */
    public double getDistance(TrafficArea area) {
        double dx = getCenterX() - area.getCenterX();
        double dy = getCenterY() - area.getCenterY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * get connector(border) between this area and next area.
     * @param area area
     * @return connector(border) which can be plural because an area can have two entrance.
     */
    public TrafficAreaEdge[] getConnector(TrafficArea area) {
        List<TrafficAreaEdge> list = new ArrayList<TrafficAreaEdge>();
        for (TrafficAreaDirectedEdge edge : directedEdges) {
            for (TrafficArea a : edge.getAreas()) {
                if (a == area) {
                    list.add(edge.getEdge());
                }
            }
        }
        return list.toArray(new TrafficAreaEdge[0]);
    }

    /**
     * get agent list.
     * @return agent list
     */
    public TrafficAgent[] getAgentList() {
        if (agentListBuf == null) {
            agentListBuf = agentList.toArray(new TrafficAgent[0]);
        }
        return agentListBuf;
    }

    /**
     * add agent into this area.
     * @param agent agent
     */
    public void addAgent(TrafficAgent agent) {
        agentList.add(agent);
        agentListBuf = null;
        for (TrafficAreaListener listener : areaListenerList) {
            listener.entered(new TrafficAreaEvent(this, agent));
        }
    }

    /**
     * remove agent from this area.
     * @param agent agent
     */
    public void removeAgent(TrafficAgent agent) {
        agentList.remove(agent);
        agentListBuf = null;
        for (TrafficAreaListener listener : areaListenerList) {
            listener.exited(new TrafficAreaEvent(this, agent));
        }
    }

    /**
     * this method is not working.
     * @param s space
     */
    public void setSimulateAsOpenSpace(boolean s) {
        throw new RuntimeException("warning: traffic3.object.area.TrafficArea.java: setSimulateAsOpenSpace(boolean)");
        //simulateAsOpenSpace = s;
    }

    /**
     * this megthod is not working.
     * @return simulate as open space
     */
    public boolean isSimulateAsOpenSpace() {
        throw new RuntimeException("warning: traffic3.object.area.TrafficArea.java: isSimulateAsOpenSpace()");
        //return simulateAsOpenSpace;
    }

    public TrafficAreaDirectedEdge[] getDirectedEdges() {
        return directedEdges;
        //return directedEdgeList.toArray(new TrafficAreaDirectedEdge[0]);
    }

    //static int testcounter = 0;
    /**
     * set properties with xml.
     * @param gmlElement xml
     * @throws Exception exception
     */
    public void setProperties(TagElement gmlElement) throws WorldManagerException {
        TagElement[] directedEdgeTagList = gmlElement.getTagChildren("gml:directedEdge");
        edgeIDs = new String[directedEdgeTagList.length];
        List<String> directedEdgeIdList = new ArrayList<String>();
        List<String> notDirectedEdgeIdList = new ArrayList<String>();
        for (int i = 0; i < directedEdgeTagList.length; i++) {
            String de = directedEdgeTagList[i].getAttributeValue("xlink:href").replaceAll("#", "");
            String or = directedEdgeTagList[i].getAttributeValue("orientation");
            edgeIDs[i] = de;
            if ("+".equals(or)) {
                directedEdgeIdList.add(de);
            }
            else {
                notDirectedEdgeIdList.add(de);
            }
        }
    }

    /**
     * get unconnected edge list as Line2D.
     * @return unconnected edge list
     */
    public Line2D[] getLines() {
        if (getLinesCACHE == null) {
            Logger.debug(this + " computing edges");
            List<Line2D> lineList = new ArrayList<Line2D>();
            Logger.debug(directedEdges.length + " directed edges");
            for (int i = 0; i < directedEdges.length; i++) {
                Logger.debug("Next directed edge: " + directedEdges[i]);
                //                if (directedEdges[i].getAreas().length < 2) {
                for (Line2D line : directedEdges[i].getLines()) {
                    lineList.add(line);
                }
                //                }
            }
            for (TrafficBlockade blockade : getBlockadeList()) {
                for (Line2D line : blockade.getLineList()) {
                    lineList.add(line);
                }
            }
            Logger.debug("Lines");
            for (Line2D next : lineList) {
                Logger.debug(next.getX1() + ", " + next.getY1() + " -> " + next.getX2() + ", " + next.getY2());
            }
            getLinesCACHE = lineList.toArray(new Line2D[0]);
        }
        return getLinesCACHE;
    }

    /**
     * clear neighbor wall list cache.
     */
    public void clearNeighborWallListCache() {
        getNeighborLinesCACHE = null;
    }

    /**
     * get neighbor wall list.
     * @return neighbor wall list
     */
    public Line2D[] getNeighborLines() {
        if (getNeighborLinesCACHE == null) {
            List<Line2D> list = new ArrayList<Line2D>();
            for (Line2D line : getLines()) {
                list.add(line);
            }
            for (TrafficArea na : getNeighborAreas()) {
                for (Line2D line : na.getLines()) {
                    list.add(line);
                }
            }
            getNeighborLinesCACHE = list.toArray(new Line2D[0]);
        }
        return getNeighborLinesCACHE;
   }

    /**
     * get shape.
     * @return shape
     */
    public GeneralPath getShape() {
        if (shape == null) {
            createCache();
        }
        return shape;
    }

    public void createCache() {
        if (directedEdges == null || directedEdges.length == 0) {
            return;
        }
        GeneralPath gp = new GeneralPath();
        TrafficAreaNode lastNode = null;
        double xsum = 0;
        double ysum = 0;
        int csum = 0;
        List<Line2D> wallList = new ArrayList<Line2D>();
        for (TrafficAreaDirectedEdge edge : directedEdges) {
            for (TrafficAreaNode node : edge.getNodes()) {
                if (lastNode == null) {
                    gp.moveTo(node.getX(), node.getY());
                }
                else {
                    gp.lineTo(node.getX(), node.getY());
                }
                xsum += node.getX();
                ysum += node.getY();
                csum++;
                lastNode = node;
            }
            TrafficArea[] areas = edge.getAreas();
            if (areas != null) {
                if (areas.length < 2) {
                    for (Line2D line : edge.getLines()) {
                        wallList.add(line);
                    }
                }
            }
        }
        center[0] = xsum/csum;
        center[1] = ysum/csum;
        gp.closePath();
        shape = gp;
        //unconnectEdgeList = wallList;
    }

    /**
     * whether this area include  a point (x, y, z).
     * @param x x
     * @param y y
     * @param z z
     * @return contain
     */
    public boolean contains(double x, double y, double z) {
        return shape.contains(x, y);
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
     * to string.
     * @return explanation
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("TrafficArea[");
        sb.append("id:").append(getID()).append(";");
        sb.append("type:").append(getType()).append(";");
        sb.append("edges:{");
        sb.append(directedEdges[0].getEdge().getID());
        for (int i=1; i<directedEdges.length; i++) {
            sb.append(",").append(directedEdges[i].getEdge().getID());
        }
        sb.append("};");
        sb.append("]");
        return sb.toString();
    }

    /**
     * to long string.
     * @return explanation
     */
    public String toLongString() {
        StringBuffer sb = new StringBuffer();
        sb.append("<div><div style='font-size:18;'>TrafficArea(id:" + getID() + ")</div>");
        sb.append("type: " + getType() + "<br/>");
        if (isChecked()) {
            sb.append("checked object.<br/>");
        }
        else {
            sb.append("unchecked object.<br/>");
        }
        sb.append("center: (" + center[0] + "," + center[1] + ")<br/>");

        /*
        sb.append("<div style='font-size:15;'>Node List</div>");
        sb.append("<div style='font-size:12;padding:0 0 0 30px;'>");
        for (int i = 0; i < nodeList.length; i++) {
            sb.append(nodeList[i]).append("<br/>");
        }
        sb.append("</div>"); 
       */
        sb.append("<div style='font-size:15;'>Directed Edge List</div>");
        sb.append("<div style='font-size:12;padding:0 0 0 30px;'>");
        for (int i = 0; i < directedEdges.length; i++) {
            sb.append(directedEdges[i]).append("<br/>");
        }
        sb.append("</div>");

        sb.append("<div style='font-size:15;'>Connected Edge List</div>");
        sb.append("<div style='font-size:12;padding:0 0 0 30px;'>");
        /*
        for (int i = 0; i < connectorList.size(); i++) {
            TrafficAreaEdge tae = connectorList.get(i);
            if (tae == null) {
                continue;
            }
            sb.append(tae.toLongString()).append("<br/>");
        }
        */
        sb.append("</div>");

        sb.append("<div style='font-size:15;'>Unconnected Edge List</div>");
        sb.append("<div style='font-size:12;padding:0 0 0 30px;'>");
        /*
        for (int i = 0; i < unconnectorList.size(); i++) {
            sb.append(unconnectorList.get(i).toString()).append("<br/>");
        }
        */
        sb.append("</div>");

        sb.append("<div style='font-size:15;'>Neighbor area List</div>");
        sb.append("<div style='font-size:12;padding:0 0 0 30px;'>");
        /*
        for (TrafficArea area : getNeighborAreas()) {
            sb.append(area.toString()).append("<br/>");
        }
        */
        sb.append("</div>");

        sb.append("<div style='font-size:15;'>Agents</div>");
        sb.append("<div style='font-size:12;padding:0 0 0 30px;'>");
        TrafficAgent[] tal = getAgentList();
        sb.append("<div>Number of Agents: ").append(tal.length).append("</div>");
        
        for (TrafficAgent agent : tal) {
            sb.append(agent.toString()).append("<br/>");
        }
        sb.append("</div>");

        sb.append("<div style='font-size:15;'>Blockades</div>");
        sb.append("<div style='font-size:12;padding:0 0 0 30px;'>");
        for (TrafficBlockade blockade : getBlockadeList()) {
            sb.append(blockade.toString()).append("<br/>");
        }
        sb.append("</div>");

        sb.append("</div>");
        return sb.toString();
    }
}
