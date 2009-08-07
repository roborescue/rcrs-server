package traffic3.manager.gui.action;

import java.awt.event.ActionEvent;

import static traffic3.log.Logger.log;
import static traffic3.log.Logger.alert;

/**
 *
 */
public class ShowVersionAction extends TrafficAction {

    /**
     * Constructor.
     */
    public ShowVersionAction() {
        super("Version");
    }

    /**
     * Show version.
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {
        //        log(">show version");
        //        String message = "<html><div style='font-size:120%;'>" + traffic3.Launch.getVersion() + "</div></html>";
        //        alert(message, "infromation");
    }
}