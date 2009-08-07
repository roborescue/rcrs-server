package traffic3.manager.gui.action;

import java.awt.event.ActionEvent;

import static traffic3.log.Logger.log;
import static org.util.Handy.inputString;
import org.util.CannotStopEDTException;

import traffic3.objects.TrafficObject;

import static traffic3.log.Logger.alert;


/**
 *
 */
public class SelectByIdAction extends TrafficAction {

    /**
     * Constructor.
     */
    public SelectByIdAction() {
        super("Select by ID");
    }

    /**
     * add object selected by id into selected group of the world manager.
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {

        log(">select by id");
        new Thread(new Runnable() { public void run() {
            try {
                String id = inputString(getWorldManagerGUI(), "id");

                TrafficObject o = getWorldManagerGUI().getWorldManager().getTrafficObject(id);
                if (o != null) {
                    getWorldManagerGUI().getTargetList().put(o.getID(), o);
                }
                else {
                    alert("cannot find id [" + id + "]", "error");
                }
                getWorldManagerGUI().createImageInOtherThread();
            }
            catch (CannotStopEDTException exc) {
                alert(exc, "error");
            }
        } }).start();
    }
}