package traffic3.manager.gui.action;

import java.awt.event.ActionEvent;

/**
 * Exit.
 */
public class ExitAction extends TrafficAction {

    /**
     * Constructor.
     */
    public ExitAction() {
        super("Exit");
    }

    /**
     * exit.
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {
        System.exit(0);
    }
}
