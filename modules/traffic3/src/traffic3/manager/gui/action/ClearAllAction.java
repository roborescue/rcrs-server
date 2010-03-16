package traffic3.manager.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import static traffic3.log.Logger.log;


/**
 * Clear all.
 */
public class ClearAllAction extends TrafficAction {

    /**
     * Constructor.
     */
    public ClearAllAction() {
        super("Clear All");
        putValue("MnemonicKey", KeyEvent.VK_C);
        putValue("ShortDescription", "Clear world data.");
    }

    /**
     * remove all objects int the world manager.
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {
        log(">clear");
        getWorldManagerGUI().getWorldManager().clear();
    }
}
