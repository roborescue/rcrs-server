package traffic3.manager.gui.action;

import java.awt.event.ActionEvent;
import javax.swing.KeyStroke;

import static traffic3.log.Logger.alert;
import traffic3.manager.gui.WorldManagerGUI;
import traffic3.manager.WorldManager;
import traffic3.manager.WorldManagerListener;
import traffic3.manager.WorldManagerEvent;


/**
 * Rec.
 */
public class RecAction2 extends TrafficAction {

    boolean isInitialized;

    /**
     * Constructor.
     */
    public RecAction2() {
        super("Rec2");
    }

    /**
     * switch rec.
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {
        alert(e.toString().replaceAll(",", ",\n"));
        if (!isInitialized) {
            initialize();
        }
    }

    public void initialize() {
        WorldManagerGUI wmgui = getWorldManagerGUI();
        WorldManager wm = wmgui.getWorldManager();
        wm.addWorldManagerListener(new WorldManagerListener() {
                public void changed(WorldManagerEvent e) {
                    alert(e);
                }
                public void added(WorldManagerEvent e) {
                    alert(e);
                }
                public void removed(WorldManagerEvent e) {
                    alert(e);                    
                }
                public void agentUpdated(WorldManagerEvent e) {
                    alert(e);                    
                }
                public void inputted(WorldManagerEvent e) {
                    alert(e);                    
                }
                public void mapUpdated(WorldManagerEvent e) {
                    alert(e);                    
                }
            });
        alert("switch");
    }
}
