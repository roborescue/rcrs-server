package rescuecore2.worldmodel;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;

/**
   Abstract base class for Property implementations.
*/
public abstract class AbstractProperty implements Property {
    private boolean defined;
    private final int id;
    private final Set<PropertyListener> listeners;

    /**
       Construct a property with a given id and assume that the value of this property is initially undefined.
       @param id The ID of the property.
     */
    protected AbstractProperty(int id) {
        this(id, false);
    }

    /**
       Construct a property with a given id and whether the value of this property is initially defined or not.
       @param id The ID of the property.
       @param defined Whether the value is initially defined or not.
     */
    protected AbstractProperty(int id, boolean defined) {
        this.id = id;
        this.defined = defined;
        listeners = new HashSet<PropertyListener>();
    }

    /**
       Set the property status to defined.
     */
    protected void setDefined() {
        defined = true;
    }

    @Override
    public boolean isDefined() {
        return defined;
    }

    @Override
    public void undefine() {
        defined = false;
        firePropertyChanged();
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public void addPropertyListener(PropertyListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public void removePropertyListener(PropertyListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    /**
       Notify all listeners that this property has changed.
    */
    protected void firePropertyChanged() {
        Collection<PropertyListener> copy;
        synchronized (listeners) {
            copy = new HashSet<PropertyListener>(listeners);
        }
        for (PropertyListener next : copy) {
            next.propertyChanged(this);
        }
    }
}