package traffic3.manager.gui.action;

import java.awt.event.ActionEvent;

import static traffic3.log.Logger.log;

/**
 * Show targets.
 */
public class ShowTargetsAsTextAction extends TrafficAction {

    /**
     * Constructor.
     */
    public ShowTargetsAsTextAction() {
        super("Show Targets as Text");
    }

    /**
     * show targets in the world manager.
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {
        log(">show targets as text");
        getWorldManagerGUI().showTargetsInformation();
    }
}
