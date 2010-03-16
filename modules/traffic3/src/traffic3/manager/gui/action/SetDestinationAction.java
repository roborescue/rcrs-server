package traffic3.manager.gui.action;

import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;

import static traffic3.log.Logger.log;
import traffic3.manager.gui.WorldManagerGUI;
import traffic3.objects.TrafficAgent;
import traffic3.objects.TrafficObject;
import traffic3.objects.area.TrafficAreaNode;
import traffic3.manager.WorldManagerException;


/**
 * Put agents.
 */
public class SetDestinationAction extends TrafficAction {

    /**
     * Constructor.
     */
    public SetDestinationAction() {
        super("set destination");
    }

    /**
     * put agents.
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {
        log(">set destination");
        new Thread(new Runnable() {
                public void run() {
                    Point2D point = getPressedPoint();
                    WorldManagerGUI wmgui = getWorldManagerGUI();
                    try {
                        TrafficAreaNode node = wmgui.getWorldManager().createAreaNode(point.getX(), point.getY(), 0);
                        for (TrafficObject o : wmgui.getTargetList().values()) {
                            if (o instanceof TrafficAgent) {
                                TrafficAgent agent = (TrafficAgent)o;
                                agent.setDestination(node);
                            }
                        }
                    }
                    catch (WorldManagerException exc) {
                        exc.printStackTrace();
                    }
                }
            }, "set destination").start();
    }
}
