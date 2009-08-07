package traffic3.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import traffic3.manager.WorldManager;
import traffic3.manager.WorldManagerException;

/**
 * Base class for all objects in the traffic simulator.
 */
public abstract class TrafficObject {

    /**
     *
     */
    protected boolean checked = false;

    /**
     * ID.
     * cannot be null
     */
    private String objectID;

    /**
     * The world manager that this object belong to.
     * cannot be null
     */
    private WorldManager worldManager;

    /**
     *
     */
    private List<ChangeListener> changeListenerList = new ArrayList<ChangeListener>();

    /**
     * This constructor must not be called.
     */
    private TrafficObject() throws WorldManagerException {
        throw new WorldManagerException("TrafficObject's constructor must not be called.");
    }

    /**
     * ID will automattically be decided.
     * You have to add this object to world_manager manually.
     * This constructor use WorldManager.getUniqueID("_");
     * @param wm WorldManager which this object will belong to.
     */
    public TrafficObject(WorldManager wm) {
        this(wm, wm.getUniqueID("_"));
    }

    /**
     * You have to add this object to world_manager manually.
     * @param wm WorldManager which this object will belong to.
     * @param id id of this object
     */
    public TrafficObject(WorldManager wm, String id) {
        objectID = id;
        worldManager = wm;
    }

    /**
     * Whether parameters of this object is validated or not.
     * @return validated or not
     */
    public boolean isChecked() {
        return checked;
    }

    /**
     * Check parameters of this object.
     * For example, If this object has a link to other object then instance must be exists in WorldManager.
     * This method have to handle checked.
     * @throws Exception exception
     */
    public abstract void checkObject() throws WorldManagerException;

    /**
     * get ID.
     * not null
     * @return id of this object
     */
    public String getID() {
        return objectID;
    }

    /**
     * get manager.
     * @return world manager
     */
    public WorldManager getManager() {
        return worldManager;
    }

    /**
     * add changeListener.
     * @param listener listener
     */
    public void addChangeListener(ChangeListener listener) {
        changeListenerList.add(listener);
    }

    /**
     * remove changeListener.
     * @param listener listener
     * @return success
     */
    public boolean removeChangeListener(ChangeListener listener) {
        return changeListenerList.remove(listener);
    }

    /**
     *fire changed.
     */
    protected void fireChanged() {
        ChangeEvent e = new ChangeEvent(this);
        for (Iterator<ChangeListener> it = changeListenerList.iterator(); it.hasNext();) {
            it.next().stateChanged(e);
        }
    }

    /**
     * detailed description.
     * @return description
     */
    public String toLongString() {
        return toString();
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof TrafficObject)) {
            return false;
        }
        TrafficObject to = (TrafficObject)o;
        return (this.getID().equals(to.getID()));
    }

    public traffic3.manager.RTreeRectangle getBounds() {
        return null;
    }
}
