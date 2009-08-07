package traffic3.manager.gui.action;

import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;

import static traffic3.log.Logger.log;
import traffic3.manager.gui.WorldManagerGUI;
import traffic3.manager.WorldManager;
import traffic3.objects.TrafficAgent;
import traffic3.objects.area.TrafficArea;
import traffic3.objects.area.TrafficAreaNode;
import traffic3.objects.area.TrafficAreaEdge;
import traffic3.objects.area.TrafficAreaDirectedEdge;
import static org.util.Handy.inputString;
import static org.util.Handy.inputInt;
import static org.util.Handy.confirm;
import traffic3.manager.WorldManagerException;
import org.util.CannotStopEDTException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.util.List;
import java.util.ArrayList;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.Element;

/**
 * Put agents.
 */
public class ValidateMapAction extends TrafficAction {

    /**
     * Constructor.
     */
    public ValidateMapAction() {
        super("ValidateMap");
    }

    /**
     * put agents.
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {
        log(">put agents2");
        new Thread(new Runnable() {
                public void run() {
                    String styleValue = "margin-left:20px;font-size:8px;";
                    StringBuffer sb = new StringBuffer();
                    sb.append("<html><body style='font-family:Arial;font-weight:bold;'>");
                    sb.append("<div>");
                    sb.append("<div>Node Check</div>");
                    sb.append("<div id='node-tag' style='"+styleValue+"'></div>");
                    sb.append("</div>");
                    sb.append("<div>");
                    sb.append("<div>Edge Check</div>");
                    sb.append("<div id='edge-tag' style='"+styleValue+"'></div>");
                    sb.append("</div>");
                    sb.append("<div>");
                    sb.append("<div>Area Check</div>");
                    sb.append("<div id='area-tag' style='"+styleValue+"'></div>");
                    sb.append("</div>");
                    sb.append("</body></html>");
                    JTextPane tarea = new JTextPane();
                    tarea.setContentType("text/html");
                    tarea.setEditable(false);
                    tarea.setText(sb.toString());
                    try {
                        WorldManagerGUI wmgui = getWorldManagerGUI();
                        WorldManager wm = wmgui.getWorldManager();
                        Point2D point = getPressedPoint();
                        JLabel nodelabel = new JLabel("Node check");
                        JLabel edgelabel = new JLabel("Edge check");
                        JLabel arealabel = new JLabel("Area check");
                        org.util.Handy.show(wmgui, "Validate Map", nodelabel, edgelabel, arealabel,  new JScrollPane(tarea));
                        validateNode(wm, nodelabel, tarea, "node-tag");
                        validateEdge(wm, edgelabel, tarea, "edge-tag");
                        validateArea(wm, arealabel, tarea, "area-tag");
                    }
                    catch (WorldManagerException exc) {
                        exc.printStackTrace();
                    }
                }
            }, "put agent2").start();
    }

    public void validateNode(WorldManager wm, JLabel label, JTextPane tp, String parentID) throws WorldManagerException {
        TrafficAreaNode[] nodes = wm.getAreaNodeList();
        for (int i = 0; i < nodes.length; i++) {
            label.setText("<html><body><div style='width:500px;'>Node check: "+(i+1)+"/"+nodes.length+"</div></body></html>");
            for (int j = 0; j < nodes.length; j++) {
                if (i != j) {
                    if ((nodes[i].getX() == nodes[j].getX()) && (nodes[i].getY() == nodes[j].getY())) {
                        try {
                            HTMLDocument html = (HTMLDocument)tp.getDocument();
                            Element elem = html.getElement(parentID);
                            StringBuffer text = new StringBuffer();
                            text.append("<div>");
                            text.append("Warning: ");
                            text.append(nodes[i].getID() + " and " + nodes[j].getID() + " are same point.");
                            text.append("("+nodes[i].getX()+","+nodes[i].getY()+")");
                            text.append("</div>");
                            html.insertBeforeEnd(elem, text.toString());
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        label.setText("<html><body><div style='width:500px;'>Node check("+nodes.length+") finished.</div></body></html>");
    }

    public void validateEdge(WorldManager wm, JLabel label, JTextPane tp, String parentID) throws WorldManagerException {
        TrafficAreaEdge[] edges = wm.getAreaConnectorEdgeList();
        for (int i = 0; i < edges.length; i++) {
            label.setText("<html><body><div style='width:500px;'>Node check: "+(i+1)+"/"+edges.length+"</div></body></html>");
            try {
                HTMLDocument html = (HTMLDocument)tp.getDocument();
                Element elem = html.getElement(parentID);
                boolean nodeerror = false;
                boolean areaerror = false;
                TrafficAreaNode[] edgenodes = edges[i].getNodes();
                StringBuffer text = new StringBuffer("<div style='color:red;'>");
                for (int j = 0; j < edgenodes.length && !nodeerror; j++) {
                    if (wm.getTrafficObject(edgenodes[j].getID()) == null) {
                        nodeerror = true;
                        text.append("<div>Edge's node does not exist in world manager.</div>");
                        text.append("<div style='margin-left:20px;'>");
                        text.append("<div>Edge: "+edges[i].toString()+"</div>");
                        StringBuffer sb = new StringBuffer("");
                        sb.append("<div>Edge's Nodes: [" + edgenodes[0].getID());
                        for (int k = 1; k < edgenodes.length; k++) {
                            sb.append(",").append(edgenodes[k].getID());
                        }
                        sb.append("]</div>");
                        sb.append("<div>But "+edgenodes[j].getID()+" does not exists in world manager.</div>");
                        sb.append("<div>"+edgenodes[j]+"</div>");
                        sb.append("</div>");
                        text.append(sb.toString());
                    }
                }
                TrafficArea[] edgeareas = edges[i].getAreas();
                if (edgeareas == null) {
                    text.append("<div>Edge's area is not defined[id:"+edges[i].getID()+"]</div>");
                }
                else {
                    for (int j = 0; j < edgeareas.length && !areaerror; j++) {
                        if (wm.getTrafficObject(edgeareas[j].getID()) == null) {
                            areaerror = true;
                            text.append("<div>Edge's area does not exist in world manager.</div>");
                            text.append("<div style='margin-left:20px;'>");
                            text.append("<div>Edge: "+edges[i].toString()+"</div>");
                            StringBuffer sb = new StringBuffer("");
                            sb.append("<div>Edge's Area: [" + edgeareas[0].getID());
                            for (int k = 1; k < edgeareas.length; k++) {
                                sb.append(",").append(edgeareas[k].getID());
                            }
                            sb.append("]</div>");
                            sb.append("<div>But "+edgeareas[j].getID()+" does not exists in world manager.</div>");
                            sb.append("<div>"+edgeareas[j]+"</div>");
                            sb.append("</div>");
                            text.append(sb.toString());
                        } else {
                            TrafficAreaDirectedEdge[] edgeareaedges = edgeareas[j].getDirectedEdges();
                            boolean flag = false;
                            for (int k = 0; k < edgeareaedges.length && !flag; k++) {
                                if (edgeareaedges[k].getEdge() == edges[i]) {
                                    flag = true;
                                }
                            }
                            if (!flag) {
                                areaerror = true;
                                text.append("<div>Area's edge which has the area is not defined .</div>");
                                text.append("<div style='margin-left:20px;'>");
                                text.append("<div>Edge: "+edges[i].toString()+"</div>");
                                text.append("<div>Area: "+edgeareas[j].toString()+"</div>");
                                text.append("</div>");
                            }
                        }
                    }
                }
                text.append("</div>");
                if (nodeerror || areaerror) {
                    html.insertBeforeEnd(elem, text.toString());
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        label.setText("<html><body><div style='width:500px;'>Node check("+edges.length+") finished.</div></body></html>");
    }

    public void validateArea(WorldManager wm, JLabel label, JTextPane tp, String parentID) throws WorldManagerException {
        HTMLDocument html = (HTMLDocument)tp.getDocument();
        Element elem = html.getElement(parentID);
        TrafficArea[] areas = wm.getAreaList();
        for (int i = 0; i < areas.length; i++) {
            label.setText("<html><body><div style='width:500px;'>Node check: "+(i+1)+"/"+areas.length+"</div></body></html>");
            TrafficAreaDirectedEdge[] dedges = areas[i].getDirectedEdges();
            boolean edgeerror = false;
            boolean areaerror = false;
            try {
                StringBuffer text = new StringBuffer("<div style='color:red;'>");
                for (int j = 0; j < dedges.length; j++) {
                    if (wm.getTrafficObject(dedges[j].getEdge().getID()) == null) {
                        edgeerror = true;
                        text.append("<div>Area's edge does not exist in WorldManger.</div>");
                        text.append("<div style='margin-left:20px;'>");
                        text.append("<div>Area: "+areas[i].toString()+"</div>");
                        text.append("<div>Edge: "+dedges[j].getEdge().toString()+"</div>");
                        text.append("</div>");
                    } else {
                        TrafficArea[] areaedgeareas = dedges[j].getAreas();
                        boolean flag = false;
                        if (areaedgeareas == null) {
                            areaerror = true;
                            text.append("<div>Edge's area which has the edge is not defined.</div>");
                            text.append("<div style='margin-left:20px;'>");
                            text.append("<div>Area: "+areas[i].toString()+"</div>");
                            text.append("<div>Edge: "+dedges[j].getEdge().toString()+"</div>");
                            text.append("</div>");
                        }
                        else {
                            for (int k = 0; k < areaedgeareas.length; k++) {
                                if (areaedgeareas[k] == areas[i]) {
                                    flag = true;
                                }
                            }
                            if (!flag) {
                                areaerror = true;
                                text.append("<div>Edge's area which has the edge is not defined.</div>");
                                text.append("<div style='margin-left:20px;'>");
                                text.append("<div>Area: "+areas[i].toString()+"</div>");
                                text.append("<div>Edge: "+dedges[j].getEdge().toString()+"</div>");
                                text.append("</div>");
                            }
                        }
                    }
                }
                text.append("</div>");
                if (edgeerror || areaerror) {
                    html.insertBeforeEnd(elem, text.toString());
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        label.setText("<html><body><div style='width:500px;'>Node check("+areas.length+") finished.</div></body></html>");

        /*
        TrafficAreaNode[] nodes = wm.getAreaNodeList();
        for (int i = 0; i < nodes.length; i++) {
            label.setText("<html><body><div style='width:500px;'>Node check: "+(i+1)+"/"+nodes.length+"</div></body></html>");
            for (int j = 0; j < nodes.length; j++) {
                if (i != j) {
                    if ((nodes[i].getX() == nodes[j].getX()) && (nodes[i].getY() == nodes[j].getY())) {
                        try {
                            HTMLDocument html = (HTMLDocument)tp.getDocument();
                            Element elem = html.getElement(parentID);
                            StringBuffer text = new StringBuffer();
                            text.append("<div>");
                            text.append("warning: ");
                            text.append(nodes[i].getID() + " and " + nodes[j].getID() + " are same point.");
                            text.append("("+nodes[i].getX()+","+nodes[i].getY()+")");
                            text.append("</div>");
                            html.insertBeforeEnd(elem, text.toString());
                            //html.insertAfterStart(elem, text.toString());
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        label.setText("<html><body><div style='width:500px;'>Node check("+nodes.length+") finished.</div></body></html>");
        */
    }

}