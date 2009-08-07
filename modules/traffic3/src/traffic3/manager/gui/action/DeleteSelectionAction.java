package traffic3.manager.gui.action;

import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;

import static traffic3.log.Logger.log;
import traffic3.manager.gui.WorldManagerGUI;
import traffic3.manager.WorldManager;
import traffic3.objects.TrafficAgent;
import traffic3.objects.TrafficObject;
import traffic3.objects.area.TrafficAreaNode;
import traffic3.manager.WorldManagerException;


/**
 * Put agents.
 */
public class DeleteSelectionAction extends TrafficAction {

    /**
     * Constructor.
     */
    public DeleteSelectionAction() {
        super("Delete selection");
    }

    /**
     * put agents.
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {
        log(">delete destination");
        new Thread(new Runnable() {
                public void run() {
                    Point2D point = getPressedPoint();
                    WorldManagerGUI wmgui = getWorldManagerGUI();
                    WorldManager wm = wmgui.getWorldManager();
                    try {
                        TrafficObject[] targets = wmgui.createCopyOfTargetList();
                        for (int i = 0; i < targets.length; i++) {
                            wm.remove(targets[i]);
                        }
                    }
                    catch (WorldManagerException exc) {
                        exc.printStackTrace();
                    }
                    wmgui.createImageInOtherThread();
                }
            }, "set destination").start();
    }
}