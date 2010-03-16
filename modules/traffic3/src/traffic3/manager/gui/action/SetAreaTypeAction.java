package traffic3.manager.gui.action;

import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;

import static traffic3.log.Logger.log;
import traffic3.manager.gui.WorldManagerGUI;
import traffic3.objects.area.TrafficArea;
import traffic3.objects.TrafficAgent;
import static org.util.Handy.inputString;
import org.util.CannotStopEDTException;

import traffic3.objects.TrafficObject;


/**
 * Put agents.
 */
public class SetAreaTypeAction extends TrafficAction {

    /**
     * Constructor.
     */
    public SetAreaTypeAction() {
        super("set type");
    }

    /**
     * put agents.
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {
        log(">set type");
        new Thread(new Runnable() {
                public void run() {
                    Point2D point = getPressedPoint();
                    WorldManagerGUI wmgui = getWorldManagerGUI();
                    try {
                        String areaType = inputString(wmgui, "Input type.");
                        TrafficObject[] copyOfTargetList = wmgui.createCopyOfTargetList();
                        for (TrafficObject o : copyOfTargetList) {
                            if (o instanceof TrafficArea) {
                                TrafficArea a = (TrafficArea)o;
                                a.setType(areaType);
                            }
                            else if (o instanceof TrafficAgent) {
                                TrafficAgent a = (TrafficAgent)o;
                                a.setType(areaType);
                            }
                        }
                        wmgui.createImageInOtherThread();
                    }
                    catch (CannotStopEDTException exc) {
                        exc.printStackTrace();
                    }
                }
            }, "put agent2").start();
    }
}
