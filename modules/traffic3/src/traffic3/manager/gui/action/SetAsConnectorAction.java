package traffic3.manager.gui.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.KeyStroke;
import java.util.Arrays;

import static traffic3.log.Logger.alert;
import traffic3.manager.gui.WorldManagerGUI;
import traffic3.manager.WorldManager;
import traffic3.manager.WorldManagerException;
import traffic3.manager.WorldManagerListener;
import traffic3.manager.WorldManagerEvent;
import traffic3.objects.area.TrafficArea;
import traffic3.objects.area.TrafficAreaNode;
import traffic3.objects.area.TrafficAreaEdge;
import traffic3.objects.area.TrafficAreaDirectedEdge;
import traffic3.objects.TrafficObject;

/**
 * Rec.
 */
public class SetAsConnectorAction extends TrafficAction {

    /**
     * Constructor.
     */
    public SetAsConnectorAction() {
        super("Set as connector");
    }

    /**
     * switch rec.
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {
        try {
            WorldManagerGUI wmgui = getWorldManagerGUI();
            WorldManager wm = wmgui.getWorldManager();
            List<TrafficAreaNode> nodeList = new ArrayList<TrafficAreaNode>();
            Map<String, TrafficObject> targetMap = wmgui.getTargetList();
            for (TrafficObject o : targetMap.values()) {
                if (o instanceof TrafficAreaNode) {
                    TrafficAreaNode node = (TrafficAreaNode)o;
                    nodeList.add(node);
                }
                else {
                    alert("[" + o + "] is not TrafficAreaNode.(skipped)");
                }
            }
            if (nodeList.size() != 2) {
                throw new WorldManagerException("The number of selected node must be 2.");
            }
            TrafficAreaNode n1 = nodeList.get(0);
            TrafficAreaNode n2 = nodeList.get(1);

            TrafficAreaEdge[] allEdges = wm.getAreaConnectorEdgeList();
            List<TrafficAreaEdge> selectedEdgeList = new ArrayList<TrafficAreaEdge>();
            for (TrafficAreaEdge edge : allEdges) {
                if (edge.has(n1, n2)) {
                    int n1i = edge.indexOf(n1);
                    int n2i = edge.indexOf(n2);
                    if (Math.abs(n1i - n2i) > 1) {
                        TrafficAreaNode[] nodes = edge.getNodes();
                        if (n1i == 0 && nodes[0] == nodes[nodes.length - 1]) {
                            n1i = nodes.length - 1;
                        }
                        if (n2i == 0 && nodes[0] == nodes[nodes.length - 1]) {
                            n2i = nodes.length - 1;
                        }
                    }
                    if (Math.abs(n1i - n2i) == 1)
                        selectedEdgeList.add(edge);
                }
            }
            TrafficAreaEdge e1 = selectedEdgeList.get(0);
            TrafficAreaEdge e2 = selectedEdgeList.get(1);
            TrafficArea a1 = e1.getAreas()[0];
            TrafficArea a2 = e2.getAreas()[0];
            if (e1.getAreas().length != 1 || e2.getAreas().length != 1) {
                throw new WorldManagerException("The number of edge's area must be 1.");
            }
            String id = wm.getUniqueID("_");
            TrafficAreaNode[] ns = new TrafficAreaNode[]{n1, n2};
            TrafficAreaEdge edge = new TrafficAreaEdge(wm, id, ns);
            wm.appendWithoutCheck(edge);
            createConnector(e1, n1, n2, a1, a2, edge);
            createConnector(e2, n1, n2, a2, a1, edge);

            wm.check();
        }
        catch (WorldManagerException exc) {
            alert(exc);
        }
    }

    private void createConnector(TrafficAreaEdge e, TrafficAreaNode n1, TrafficAreaNode n2, TrafficArea a1, TrafficArea a2, TrafficAreaEdge cedge) throws WorldManagerException {
        WorldManagerGUI wmgui = getWorldManagerGUI();
        WorldManager wm = wmgui.getWorldManager();
        TrafficAreaNode[] nodes = e.getNodes();
        int ni1 = e.indexOf(n1);
        int ni2 = e.indexOf(n2);

        if (ni1 == -1 || ni2 == -1) {
            throw new WorldManagerException("error!");
        }
        if (Math.abs(ni1 - ni2) > 1) {
            if (ni1 == 0 && nodes[0] == nodes[nodes.length - 1]) {
                ni1 = nodes.length - 1;
            }
            else if (ni2 == 0 && nodes[0] == nodes[nodes.length - 1]) {
                ni2 = nodes.length - 1;
            }
        }
        if (Math.abs(ni1 - ni2) > 1) {
            throw new WorldManagerException("error!" + ni1 + ":" + ni2);
        }
        int min = Math.min(ni1, ni2);
        int max = Math.max(ni1, ni2);
        TrafficAreaEdge targetEdge = null;
        if (min == 0 && max == e.getNodes().length - 1) {
            wm.remove(e);
            String id = wm.getUniqueID("_");
            List<TrafficAreaDirectedEdge> dedgeList = new ArrayList(Arrays.asList(a1.getDirectedEdges()));
            TrafficAreaDirectedEdge de = findDirectedEdge(dedgeList, e);
            boolean positive = de.getDirection();
            int index = dedgeList.indexOf(de);
            dedgeList.remove(index);
            dedgeList.add(index, new TrafficAreaDirectedEdge(wm, id, cedge, (ni1 == min)));
            a1.setDirectedEdges(dedgeList.toArray(new TrafficAreaDirectedEdge[0]));
        }
        else if (min == 0) {
            String id2 = wm.getUniqueID("_");
            String id3 = wm.getUniqueID("_");
            String id4 = wm.getUniqueID("_");
            TrafficAreaNode[] ns2 = Arrays.copyOfRange(nodes, max, nodes.length);
            TrafficAreaEdge edge1 = cedge;
            TrafficAreaEdge edge2 = new TrafficAreaEdge(wm, id2, ns2);
            edge1.setAreas(a1, a2);
            edge2.setAreas(a1);
            wm.remove(e);
            wm.appendWithoutCheck(edge2);

            List<TrafficAreaDirectedEdge> dedgeList = new ArrayList<TrafficAreaDirectedEdge>(Arrays.asList(a1.getDirectedEdges()));
            TrafficAreaDirectedEdge de = findDirectedEdge(dedgeList, e);
            boolean positive = de.getDirection();
            int index = dedgeList.indexOf(de);
            dedgeList.remove(index);
            dedgeList.add(index, new TrafficAreaDirectedEdge(wm, id3, edge2, positive));
            dedgeList.add(index, new TrafficAreaDirectedEdge(wm, id4, edge1, (ni1 == min)));
            a1.setDirectedEdges(dedgeList.toArray(new TrafficAreaDirectedEdge[0]));
        }
        else if (max == e.getNodes().length - 1) {
            String id1 = wm.getUniqueID("_");
            String id3 = wm.getUniqueID("_");
            String id4 = wm.getUniqueID("_");
            TrafficAreaNode[] ns1 = Arrays.copyOfRange(nodes, 0, min + 1);
            TrafficAreaEdge edge1 = new TrafficAreaEdge(wm, id1, ns1);
            TrafficAreaEdge edge2 = cedge;
            edge1.setAreas(a1);
            edge2.setAreas(a1, a2);
            wm.remove(e);
            wm.appendWithoutCheck(edge1);

            List<TrafficAreaDirectedEdge> dedgeList = new ArrayList<TrafficAreaDirectedEdge>(Arrays.asList(a1.getDirectedEdges()));
            TrafficAreaDirectedEdge de = findDirectedEdge(dedgeList, e);
            boolean positive = de.getDirection();
            int index = dedgeList.indexOf(de);
            dedgeList.remove(index);
            dedgeList.add(index, new TrafficAreaDirectedEdge(wm, id3, edge2, (ni1 == min)));
            dedgeList.add(index, new TrafficAreaDirectedEdge(wm, id4, edge1, positive));
            a1.setDirectedEdges(dedgeList.toArray(new TrafficAreaDirectedEdge[0]));
        }
        else {
            String id1 = wm.getUniqueID("_");
            String id3 = wm.getUniqueID("_");
            String id4 = wm.getUniqueID("_");
            String id5 = wm.getUniqueID("_");
            String id6 = wm.getUniqueID("_");
            TrafficAreaNode[] ns1 = Arrays.copyOfRange(nodes, 0, min + 1);
            TrafficAreaNode[] ns3 = Arrays.copyOfRange(nodes, max, nodes.length);
            TrafficAreaEdge edge1 = new TrafficAreaEdge(wm, id1, ns1);
            TrafficAreaEdge edge2 = cedge;
            TrafficAreaEdge edge3 = new TrafficAreaEdge(wm, id3, ns3);
            edge1.setAreas(a1);
            edge2.setAreas(a1, a2);
            edge3.setAreas(a1);
            wm.remove(e);
            wm.appendWithoutCheck(edge1);
            wm.appendWithoutCheck(edge3);

            List<TrafficAreaDirectedEdge> dedgeList = new ArrayList<TrafficAreaDirectedEdge>(Arrays.asList(a1.getDirectedEdges()));
            TrafficAreaDirectedEdge de = findDirectedEdge(dedgeList, e);
            boolean positive = de.getDirection();
            int index = dedgeList.indexOf(de);
            dedgeList.remove(index);
            dedgeList.add(index, new TrafficAreaDirectedEdge(wm, id4, edge3, positive));
            dedgeList.add(index, new TrafficAreaDirectedEdge(wm, id5, edge2, (ni1 == min)));
            dedgeList.add(index, new TrafficAreaDirectedEdge(wm, id6, edge1, positive));
            a1.setDirectedEdges(dedgeList.toArray(new TrafficAreaDirectedEdge[0]));
        }
    }

    public TrafficAreaDirectedEdge findDirectedEdge(List<TrafficAreaDirectedEdge> dedgeList, TrafficAreaEdge e) {
        for (TrafficAreaDirectedEdge de : dedgeList) {
            if (de.getEdge().equals(e)) {
                return de;
            }
        }
        return null;
    }
}
