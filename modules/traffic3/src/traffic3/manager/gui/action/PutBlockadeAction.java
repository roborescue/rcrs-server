package traffic3.manager.gui.action;

import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;

import static traffic3.log.Logger.log;
import traffic3.manager.gui.WorldManagerGUI;
import static org.util.Handy.inputDouble;
import traffic3.objects.TrafficObject;
import traffic3.objects.TrafficBlockade;
import traffic3.objects.area.TrafficArea;
import traffic3.objects.area.TrafficAreaNode;
import traffic3.manager.WorldManagerException;
import org.util.CannotStopEDTException;

/**
 * Put agents.
 */
public class PutBlockadeAction extends TrafficAction {

    /**
     * Constructor.
     */
    public PutBlockadeAction() {
        super("put blockade");
    }

    /**
     * put blockade.
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {
        log(">put blockade");
        new Thread(new Runnable() {
                public void run() {
                    Point2D point = getPressedPoint();
                    WorldManagerGUI wmgui = getWorldManagerGUI();

                    try {
                        TrafficObject[] copyOfTargetList = wmgui.createCopyOfTargetList();
                        double width = inputDouble(wmgui, "input blockade width [0-100]%.");
                        for (TrafficObject tobj : copyOfTargetList) {
                            if (!(tobj instanceof TrafficArea)) {
                                log("cannot put blockade to " + tobj);
                                continue;
                            }
                            TrafficArea tarea = (TrafficArea)tobj;
                            TrafficBlockade tblockade = new TrafficBlockade(wmgui.getWorldManager(), wmgui.getWorldManager().getUniqueID("_"));
                            TrafficAreaNode[] nodeList = tarea.getNodes();
                            //tblockade.setCenter(nodeList[0].getX(), nodeList[0].getY());
                            double cx = tarea.getCenterX();
                            double cy = tarea.getCenterY();
                            int[] xyList = new int[nodeList.length * 2];
                            int index = 0;
                            for (TrafficAreaNode node : nodeList) {
                                //double d = 0.5+0.499999*Math.random();
                                double d = width * (WorldManagerGUI.BLOCKADE_WIDTH_MEAN_DEFAULT + (Math.random() * WorldManagerGUI.BLOCKADE_WIDTH_VALIATY_DEFAULT)) / WorldManagerGUI.UNIT_PERCENT;
                                //double d = width/100.0;
                                double x = node.getX() * d + cx * (1 - d);
                                double y = node.getY() * d + cy * (1 - d);
                                xyList[index++] = (int)x;
                                xyList[index++] = (int)y;
                            }
                            tblockade.setCenter(cx, cy);
                            tblockade.setLineList(xyList);
                            wmgui.getWorldManager().appendWithoutCheck(tblockade);
                            tarea.addBlockade(tblockade);
                        }
                        wmgui.createImageInOtherThread();
                    }
                    catch (CannotStopEDTException exc) {
                        exc.printStackTrace();
                    }
                    catch (WorldManagerException exc) {
                        exc.printStackTrace();
                    }
                }
            }, "put blockade").start();
    }
}