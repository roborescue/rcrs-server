package traffic3.manager.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

import static traffic3.log.Logger.log;

/**
 * Fit View.
 */
public class FitViewAction extends TrafficAction {

    /**
     * Fit view.
     */
    public FitViewAction() {
        super("Fit View");
        putValue("MnemonicKey", KeyEvent.VK_V);
        putValue("ShortDescription", "Set view that all the object just can be seen .");
        putValue("AcceleratorKey", KeyStroke.getKeyStroke('f'));
    }

    /**
     * fit view.
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {
        log(">fit view");
        getWorldManagerGUI().fitView();
    }
}
