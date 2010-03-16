package traffic3.manager.gui.action;

import java.awt.event.ActionEvent;

import static traffic3.log.Logger.log;
import static traffic3.log.Logger.alert;

import traffic3.objects.TrafficAgent;
import traffic3.manager.WorldManagerException;

/**
 * clear agent action.
 * @return clear action
 */
public class ClearAllAgentsAction extends TrafficAction {

    /**
     * Constructor.
     */
    public ClearAllAgentsAction() {
        super("Clear All Agents");
        putValue("ShortDescription", "Clear all agents.");
    }

    /**
     * Clear all the objects in the world manager.
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {
        log(">clear agents");
        StringBuffer successlog = new StringBuffer();
        StringBuffer errorlog = new StringBuffer();
        boolean error = false;
        for (TrafficAgent agent : getWorldManagerGUI().getWorldManager().getAgentList()) {
            try {
                getWorldManagerGUI().getWorldManager().remove(agent);
                successlog.append(agent.toLongString()).append("\n");
            }
            catch (WorldManagerException exc) {
                exc.printStackTrace();
                errorlog.append(agent.toLongString()).append("\n");
                errorlog.append("-----------------------------\n");
                errorlog.append(exc.getMessage()).append("\n");
                errorlog.append(" == ===========================\n");
                error = true;
            }
        }
        if (error) {
            alert("success:\n" + successlog + "\n\nerror:\n" + errorlog, "error");
        }
    }
}
