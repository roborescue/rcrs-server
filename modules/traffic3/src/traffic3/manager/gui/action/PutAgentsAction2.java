package traffic3.manager.gui.action;

import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;

import static traffic3.log.Logger.log;
import traffic3.manager.gui.WorldManagerGUI;
import traffic3.objects.TrafficAgent;
import traffic3.objects.area.TrafficArea;
import static org.util.Handy.inputString;
import static org.util.Handy.inputInt;
import static org.util.Handy.confirm;
import traffic3.manager.WorldManagerException;
import org.util.CannotStopEDTException;

import java.util.List;
import java.util.ArrayList;


/**
 * Put agents.
 */
public class PutAgentsAction2 extends TrafficAction {

    /**
     * Constructor.
     */
    public PutAgentsAction2() {
        super("put agents2");
    }

    /**
     * put agents.
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {
        log(">put agents2");
        new Thread(new Runnable() {
                public void run() {
                    Point2D point = getPressedPoint();
                    WorldManagerGUI wmgui = getWorldManagerGUI();
                    try {
                        int number = inputInt(wmgui, "How many agent do you want to put per area?");
                        String message = "Input group type.(If empty, new group will not be created.)";
                        String groupName = inputString(wmgui, message);
                        List<TrafficAgent> agentBuf = new ArrayList<TrafficAgent>();
                        double radius = WorldManagerGUI.AGENT_RADIUS_DEFAULT;
                        double velocity = WorldManagerGUI.AGENT_VELOCITY_DEFAULT;
                        /*
                        for (TrafficArea area : wmgui.getWorldManager().getAreaList()) {
                            for (int i = 0; i < number; i++) {
                                TrafficAgent agent = new TrafficAgent(wmgui.getWorldManager(), WorldManagerGUI.AGENT_RADIUS_DEFAULT, WorldManagerGUI.AGENT_VELOCITY_DEFAULT);
                                agent.setType(groupName);
                                agentBuf.add(agent);
                                agent.setLocation(area.getCenterX(), area.getCenterY(), 0);
                                wmgui.getWorldManager().appendWithoutCheck(agent);
                            }
                        }
                        */
                       Boolean b = (Boolean)wmgui.getValue("simulating");
                        if (!b.booleanValue()) {
                            if (confirm(wmgui, "Simulation was not started.\n Do you want to start simulation now?")) {
                                wmgui.setValue("simulating", true);
                            }
                        }
                     }
                    catch (CannotStopEDTException exc) {
                        exc.printStackTrace();
                    }
                    //                    catch (WorldManagerException exc) {
                    //                        exc.printStackTrace();
                    //                    }
                }
            }, "put agent2").start();
    }
}