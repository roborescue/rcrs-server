package traffic3.manager.gui.action;

import java.awt.event.ActionEvent;
import javax.swing.KeyStroke;

/**
 * Rec.
 */
public class RecAction extends TrafficAction {

    /**
     * Constructor.
     */
    public RecAction() {
        super("Rec");
        putValue("AcceleratorKey", KeyStroke.getKeyStroke('r'));
    }

    /**
     * switch rec.
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {
        getWorldManagerGUI().switchRec();
    }
}
