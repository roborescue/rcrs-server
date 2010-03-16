package traffic3.manager.gui.action;

import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;

import static traffic3.log.Logger.log;
import traffic3.manager.gui.WorldManagerGUI;
import traffic3.objects.TrafficAgent;
import static org.util.Handy.inputString;
import static org.util.Handy.inputInt;
import static org.util.Handy.confirm;
import org.util.CannotStopEDTException;
import traffic3.manager.WorldManagerException;

import java.util.List;
import java.util.ArrayList;


/**
 * Put agents.
 */
public class PutAgentsAction extends TrafficAction {

    /**
     * Constructor.
     */
    public PutAgentsAction() {
        super("put agents");
    }

    /**
     * put agents.
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {
        log(">put agents");
        new Thread(new Runnable() {
                public void run() {
                    Point2D point = getPressedPoint();
                    try {
                        WorldManagerGUI wmgui = getWorldManagerGUI();
                        int number = inputInt(wmgui, "How many agent do you want to put?");
                        String message = "Input group type.(If empty, new group will not be created.)";
                        String groupName = inputString(wmgui, message);
                        List<TrafficAgent> agentBuf = new ArrayList<TrafficAgent>();
                        double radius = WorldManagerGUI.AGENT_RADIUS_DEFAULT;
                        double velocity = WorldManagerGUI.AGENT_VELOCITY_DEFAULT;
                        /*
                        for (int i = 0; i < number; i++) {
                            TrafficAgent agent = new TrafficAgent(wmgui.getWorldManager(), WorldManagerGUI.AGENT_RADIUS_DEFAULT, WorldManagerGUI.AGENT_VELOCITY_DEFAULT);
                            agent.setType(groupName);
                            agentBuf.add(agent);
                            agent.setLocation(point.getX(), point.getY(), 0);
                            wmgui.getWorldManager().appendWithoutCheck(agent);
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
            }, "put agent").start();
    }
}
