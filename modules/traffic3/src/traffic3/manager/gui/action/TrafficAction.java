package traffic3.manager.gui.action;

import java.awt.geom.Point2D;
import java.awt.event.ActionEvent;
import traffic3.manager.gui.WorldManagerGUI;
import javax.swing.AbstractAction;

/**
 * this object define action of both menu and popup.
 */
public abstract class TrafficAction extends AbstractAction {

    private WorldManagerGUI worldManagerGUI;
    private Point2D pressedPoint;

    /**
     * Constructor.
     * @param title title of this action
     */
    public TrafficAction(String title) {
        super(title);
    }

    /**
     * This method will be called by WorldManagerGUI before this action will be called.
     * @param wmgui WorldManager
     */
    public void setWorldManagerGUI(WorldManagerGUI wmgui) {
        worldManagerGUI = wmgui;
    }

    /**
     * this is the only way to get instance of WorldManagerGUI.
     * and using WorldManagerGUI.getWorldManager() to get instance of WorldManger.
     * @return WorldManagerGUI
     */
    public WorldManagerGUI getWorldManagerGUI() {
        return worldManagerGUI;
    }

    /**
     * this method will be called before popup window will apear.
     * and the point is the location([mm], [mm]) of clicking.
     * @param p pressed point
     */
    public void setPressedPoint(Point2D p) {
        pressedPoint = p;
    }

    /**
     * point.
     * @return point
     */
    public Point2D getPressedPoint() {
        return pressedPoint;
    }
}
