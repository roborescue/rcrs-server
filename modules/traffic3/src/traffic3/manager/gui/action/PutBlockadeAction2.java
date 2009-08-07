package traffic3.manager.gui.action;

import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.PathIterator;
import java.awt.geom.AffineTransform;
import java.awt.Rectangle;
import java.awt.Polygon;

import static traffic3.log.Logger.log;
import traffic3.manager.gui.WorldManagerGUI;
import traffic3.objects.TrafficObject;
import traffic3.objects.area.TrafficArea;
import traffic3.objects.area.TrafficAreaNode;
import traffic3.objects.TrafficBlockade;
import traffic3.manager.WorldManagerException;

import java.util.List;
import java.util.ArrayList;


/**
 * Put agents.
 */
public class PutBlockadeAction2 extends TrafficAction {

    private static final int PATH_ITERATOR_COPY_BUF_LENGTH = 6;

    /**
     * Constructor.
     */
    public PutBlockadeAction2() {
        super("put blockade2");
    }

    /**
     * put blockade.
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {
        log(">put blockade2");
        new Thread(new Runnable() {
                public void run() {
                    Point2D point = getPressedPoint();
                    WorldManagerGUI wmgui = getWorldManagerGUI();
                    try {
                        TrafficObject[] copyOfTargetList = wmgui.createCopyOfTargetList();

                        //double width = inputDouble(thisObject, "input blockade width [0-100]%.");
                        for (TrafficObject tobj : copyOfTargetList) {
                            if (!(tobj instanceof TrafficArea)) {
                                log("cannot put blockade to " + tobj);
                                continue;
                            }
                            TrafficArea tarea = (TrafficArea)tobj;
                            TrafficAreaNode[] nodeList = tarea.getNodes();
                            int length = nodeList.length;
                            int[] xs = new int[length];
                            int[] ys = new int[length];
                            for (int i = 0; i < length; i++) {
                                xs[i] = (int)nodeList[i].getX();
                                ys[i] = (int)nodeList[i].getY();
                            }
                            Polygon polygon = new Polygon(xs, ys, length);
                            //alert(polygon, "error");
                            Area area = new Area(polygon);
                            Rectangle rect = area.getBounds();

                            Area subArea = null;
                            int wcount = (int)(rect.getWidth() / WorldManagerGUI.BLOCKADE_SEPARATE_WIDTH + 1);
                            int hcount = (int)(rect.getHeight() / WorldManagerGUI.BLOCKADE_SEPARATE_HEIGHT + 1);
                            double x = rect.getX();
                            double y = rect.getY();
                            double w = rect.getWidth() / wcount;
                            double h = rect.getHeight() / hcount;
                            for (int j = 0; j < hcount; j++) {
                                for (int i = 0; i < wcount; i++) {
                                    subArea = new Area(new Rectangle2D.Double(x + i * w, y + j * h, w, h));
                                    subArea.intersect(area);
                                    PathIterator pathIterator = subArea.getPathIterator(new AffineTransform(1, 0, 0, 1, 0, 0));
                                    List<Point2D> pointList = new ArrayList<Point2D>();
                                    double[] xydList = new double[PATH_ITERATOR_COPY_BUF_LENGTH];
                                    for (; !pathIterator.isDone(); pathIterator.next()) {
                                        int type = pathIterator.currentSegment(xydList);
                                        pointList.add(new Point2D.Double(xydList[0], xydList[1]));
                                    }
                                    if (pointList.size()  ==  0) {
                                        continue;
                                    }
                                    TrafficBlockade tblockade = new TrafficBlockade(wmgui.getWorldManager(), wmgui.getWorldManager().getUniqueID("_"));

                                    tblockade.setCenter(nodeList[0].getX(), nodeList[0].getY());
                                    int[] xyList = new int[pointList.size() * 2];
                                    int index = 0;
                                    double cx = pointList.get(0).getX();
                                    double cy = pointList.get(1).getY();
                                    double xsum = 0;
                                    double ysum = 0;
                                    for (Point2D p : pointList) {
                                        //double d = 0.5+0.499999*Math.random();
                                        //double d = width*(0.8+(Math.random()*0.2))/101.0;
                                        //double d = width/100.0;
                                        double d = 1;
                                        double xx = p.getX() + Math.random() * d - d / 2;
                                        double yy = p.getY() + Math.random() * d - d / 2;
                                        xsum += xx;
                                        ysum += yy;
                                        xyList[index++] = (int)xx;
                                        xyList[index++] = (int)yy;
                                    }
                                    cx = (xsum / pointList.size());
                                    cy = (ysum / pointList.size());
                                    tblockade.setCenter(cx, cy);
                                    tblockade.setLineList(xyList);
                                    wmgui.getWorldManager().appendWithoutCheck(tblockade);
                                    tarea.addBlockade(tblockade);
                                }
                            }
                        }
                        wmgui.createImageInOtherThread();
                    }
                    catch (WorldManagerException exc) {
                        exc.printStackTrace();
                    }
                }
            }, "put blockade2").start();
    }
}