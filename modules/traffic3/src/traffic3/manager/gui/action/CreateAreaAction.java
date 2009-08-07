package traffic3.manager.gui.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.KeyStroke;

import static traffic3.log.Logger.alert;
import traffic3.manager.gui.WorldManagerGUI;
import traffic3.manager.WorldManager;
import traffic3.manager.WorldManagerException;
import traffic3.manager.WorldManagerListener;
import traffic3.manager.WorldManagerEvent;
import traffic3.objects.area.TrafficArea;
import traffic3.objects.area.TrafficAreaNode;
import traffic3.objects.TrafficObject;

/**
 * Rec.
 */
public class CreateAreaAction extends TrafficAction {

    /**
     * Constructor.
     */
    public CreateAreaAction() {
        super("Create Area");
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
            //Map<String, TrafficObject> targetMap = wmgui.getTargetList();
            for (TrafficObject o : wmgui.getOrderedTargets()) {
                if (o instanceof TrafficAreaNode) {
                    TrafficAreaNode node = (TrafficAreaNode)o;
                    nodeList.add(node);
                }
                else {
                    alert("[" + o + "] is not TrafficAreaNode.(skipped)");
                }
            }
            if (nodeList.size() < 3) {
                throw new WorldManagerException("The number of selected node must be larger than 2.");
            }
            TrafficArea area = new TrafficArea(wm, wm.getUniqueID("_area"),nodeList.toArray(new TrafficAreaNode[0]));
            wm.appendWithoutCheck(area);
            wm.check();
        }
        catch (WorldManagerException exc) {
            alert(exc);
        }
    }
}
