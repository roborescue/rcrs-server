package traffic3.manager.gui.action;

import java.io.File;
import java.io.FileNotFoundException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

import traffic3.manager.gui.IO;
import traffic3.manager.gui.UserCancelException;
import static traffic3.log.Logger.log;
import static traffic3.log.Logger.alert;

/**
 * Import action.
 */
public class ImportAction extends TrafficAction {

    /**
     * Constructor.
     */
    public ImportAction() {
        super("Import");
        putValue("MnemonicKey", KeyEvent.VK_I);
        putValue("AcceleratorKey", KeyStroke.getKeyStroke(KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        putValue("ShortDescription", "<html>Import from file.<br/>World data will not be cleared. So if you want to open new file, then you should clear world at first.</html>");
    }

    /**
     * import.
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {
        try {
            log(">import");
            File file = IO.getOpenFile(getWorldManagerGUI(), "GML file", "xml", "gml");
            alert("opening file: " + file.getAbsolutePath());
            getWorldManagerGUI().open(file);
            alert("finished!");

        }
        catch (UserCancelException exc) {
            log("cancelled by user.");
        }
        catch (FileNotFoundException exc) {
            alert(exc, "error");
        }
    }
}
