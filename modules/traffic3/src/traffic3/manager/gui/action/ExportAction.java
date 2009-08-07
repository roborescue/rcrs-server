package traffic3.manager.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

/**
 * Export to file.
 */
public class ExportAction extends TrafficAction {

    /**
     * Constructor.
     */
    public ExportAction() {
        super("Export");
        putValue("MnemonicKey", KeyEvent.VK_E);
        putValue("AcceleratorKey", KeyStroke.getKeyStroke(KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        putValue("ShortDescription", "<html>Export to file.<br/>World data will not be cleared. So if you want to open new file, then you should clear world at first.</html>");
    }

    /**
     * export to file.
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {
        getWorldManagerGUI().save();
    }
}