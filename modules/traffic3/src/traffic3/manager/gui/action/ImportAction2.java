package traffic3.manager.gui.action;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import javax.swing.JFileChooser;
import java.awt.geom.Point2D;

import traffic3.manager.gui.IO;
import traffic3.manager.gui.UserCancelException;
import static traffic3.log.Logger.log;
import static traffic3.log.Logger.alert;
import traffic3.manager.WorldManager;
import traffic3.objects.area.TrafficArea;
import traffic3.objects.area.TrafficAreaNode;
import traffic3.objects.area.TrafficAreaEdge;

import org.util.xml.element.TagElement;
import org.util.xml.io.XMLIO;

/**
 * Import action.
 */
public class ImportAction2 extends TrafficAction {

    /**
     * Constructor.
     */
    public ImportAction2() {
        super("Import from old RCRS");
        putValue("ShortDescription", "<html>Import from file.<br/>World data will not be cleared. So if you want to open new file, then you should clear world at first.</html>");
    }

    /**
     * import.
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {
        try {
            log(">import");
            JFileChooser fc = new JFileChooser();
            int v = fc.showOpenDialog(getWorldManagerGUI());
            if (v == JFileChooser.APPROVE_OPTION) {
                open(fc.getSelectedFile());
            }
        }
        catch (UserCancelException exc) {
            log("cancelled by user.");
        }
        catch (FileNotFoundException exc) {
            alert(exc, "error");
        }
        catch (Exception exc) {
            alert(exc, "error");
        }
    }

    Map<String, TrafficAreaEdge> idEdgeMap = new HashMap<String, TrafficAreaEdge>();
    public void open(File f) throws Exception {
        WorldManager worldManager = getWorldManagerGUI().getWorldManager();
        TagElement tag = XMLIO.read(f);
        /*
        for (TagElement pt : tag.getTagChildren("poslists")) {
            for (TagElement t : pt.getTagChildren("poslist")) {
                List<TrafficAreaNode> tanList = new ArrayList<TrafficAreaNode>();
                for (String pointText : t.getValue().split(" ")) {
                    String[] xytext = pointText.split(",");
                    double x = Double.parseDouble(xytext[0]);
                    double y = Double.parseDouble(xytext[1]);
                    TrafficAreaNode n = worldManager.createAreaNode(x, y, 0.0);
                    tanList.add(n);
                }
                String id = t.getAttributeValue("id");
                TrafficAreaEdge tae = new TrafficAreaEdge(worldManager, id);
                tae.setDirectedNodes(tanList.get(0).getID(), tanList.get(tanList.size()-1).getID());
                idEdgeMap(tae.getID(), tae);
                worldManager.appendWithoutCheck(tae);
            }
        }
        */
        Map<String, TagElement> idEdgeTag = new HashMap<String, TagElement>();
        for (TagElement pt : tag.getTagChildren("poslists")) {
            TagElement[] ptc = pt.getTagChildren("poslist");
            log("read edge tags: " + ptc.length);
            for (TagElement t : ptc) {
                String id = t.getAttributeValue("id");
                idEdgeTag.put(id, t);
            }
        }

        for (TagElement pt : tag.getTagChildren("areas")) {
            for (TagElement t : pt.getTagChildren("area")) {
                String areaID = t.getAttributeValue("id");
                log("area: " + areaID);
                double centerX = 0;
                double centerY = 0;
                int[] xys = null;
                String[] nexts = null;
                List<Point2D> pointList = new ArrayList<Point2D>();
                List<String> nextList = new ArrayList<String>();
                Point2D lastP = null;
                for (String poslistid : t.getTagChild("poslists").getValue().split(" ")) {
                    TagElement poslistTag = idEdgeTag.get(poslistid);
                    String next = "rcrs(-1)";
                    String areaID1 = poslistTag.getAttributeValue("area1");
                    String areaID2 = poslistTag.getAttributeValue("area2");
                    if (!"-1".equals(areaID1) && !"-1".equals(areaID2)) {
                        if (areaID1.equals(areaID)) {
                            next = areaID2;
                        } else {
                            next = areaID1;
                        }
                    }
                    ArrayList<Point2D> pbuf = new ArrayList<Point2D>();
                    for (String pointText : poslistTag.getValue().split(" ")) {
                        String[] xytext = pointText.split(",");
                        double x = Double.parseDouble(xytext[0]);
                        double y = Double.parseDouble(xytext[1]);
                        pbuf.add(new Point2D.Double(x, y));
                    }
                    sort(lastP, pbuf, pointList);
                    for (Point2D p : pbuf) {
                        if (p != lastP) {
                            pointList.add(p);
                            nextList.add(next);
                            lastP = p;
                        }
                    }
                }
                if (pointList.get(0).equals(pointList.get(pointList.size()-1))) {
                    pointList.remove(pointList.size() - 1);
                    nextList.remove(nextList.size() - 1);
                }

                int length = pointList.size();
                xys = new int[length * 2];
                nexts = new String[length];
                double sumX = 0;
                double sumY = 0;
                for (int i = 0; i < length; i++) {
                    int x = (int)pointList.get(i).getX();
                    int y = (int)pointList.get(i).getY();
                    xys[i * 2] = x;
                    xys[i * 2 + 1] = y;
                    sumX += x;
                    sumY += y;
                    nexts[i] = nextList.get(i);
                }
                centerX = sumX / length;
                centerY = sumY / length;
                TrafficArea area = new TrafficArea(worldManager, areaID, centerX, centerY, xys, nexts, null, null);
                worldManager.appendWithoutCheck(area);
            }
        }
        //TrafficObject to = 
        //worldManager.appendWithoutCheck(to);
        worldManager.check();
        alert("finished");
    }

    public void sort(Point2D last, List<Point2D> list, List<Point2D> total) {
        if (last == null) {
            return;
        }
        if (last.equals(list.get(0))) {
            return;
        }
        if (last.equals(list.get(list.size() - 1))) {
            reverse(list);
            return;
        }
        if (list.get(0).equals(total.get(0))) {
            reverse(total);
            return;
        }
        if (list.get(list.size() - 1).equals(total.get(0))) {
            reverse(list);
            reverse(total);
            return;
        }
        StringBuffer sb = new StringBuffer();
        sb.append("Error: sort: \n");
        sb.append("last: ").append(last).append("\n");
        sb.append("points: ").append(list).append("\n");
        sb.append("total edges:").append(total).append("\n");
        throw new RuntimeException(sb.toString());
    }
    public void reverse(List list) {
        Object[] buf = list.toArray();
        list.clear();
        for (int i = 0; i < buf.length; i++) {
            list.add(buf[buf.length - 1 - i]);
        }
    }
}
