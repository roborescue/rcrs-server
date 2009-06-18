package rescuecore2.worldmodel;

import static rescuecore2.misc.EncodingTools.writeInt32;
import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.readBytes;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Iterator;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
   Abstract base class for concrete Entity implementations.
   @param <T> A subclass of EntityType. This allows concrete implementations to guarantee that {@link #getType} will return this particular subclass.
 */
public abstract class AbstractEntity<T extends EntityType> implements Entity {
    /** Map from id to Property. */
    private final Map<Integer, Property> properties;
    private final EntityID id;
    private final T type;
    private final Set<EntityListener> listeners;

    /**
       Construct an AbstractEntity with a set of properties.
       @param id The ID of this entity.
       @param type The type of this entity.
    */
    protected AbstractEntity(EntityID id, T type) {
        this.id = id;
        this.type = type;
        properties = new HashMap<Integer, Property>();
        listeners = new HashSet<EntityListener>();
    }

    /**
       Add a set of properties to this entity. This should only be used by subclasses during construction.
       @param props The properties to add.
    */
    protected void addProperties(Property... props) {
        for (Property next : props) {
            properties.put(next.getType().getID(), next);
        }
    }

    @Override
    public void addEntityListener(EntityListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public void removeEntityListener(EntityListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    @Override
    public Entity copy() {
        Entity result = copyImpl();
        for (Property original : properties.values()) {
            Property copy = result.getProperty(original.getType());
            copy.takeValue(original);
        }
        return result;
    }

    /**
       Create a copy of this entity. Property values do not need to be copied.
       @return A new Entity of the same type as this and with the same ID.
    */
    protected abstract Entity copyImpl();

    @Override
    public Set<Property> getProperties() {
        return new HashSet<Property>(properties.values());
    }

    @Override
    public Property getProperty(PropertyType getType) {
        return properties.get(getType.getID());
    }

    @Override
    public Property getProperty(int getID) {
        return properties.get(getID);
    }

    @Override
    public EntityID getID() {
        return id;
    }

    @Override
    public T getType() {
        return type;
    }

    @Override
    public void write(OutputStream out) throws IOException {
        for (Property next : getProperties()) {
            ByteArrayOutputStream gather = new ByteArrayOutputStream();
            if (next.isDefined()) {
                next.write(gather);
                byte[] bytes = gather.toByteArray();
                //   Type
                writeInt32(next.getID(), out);
                //   Size
                writeInt32(bytes.length, out);
                //   Data
                out.write(bytes);
            }
        }
        // end-of-properties marker
        writeInt32(0, out);
    }

    @Override
    public void read(InputStream in) throws IOException {
        int propID;
        do {
            propID = readInt32(in);
            if (propID != 0) {
                //                System.out.println("Reading property " + propID);
                int size = readInt32(in);
                //                System.out.println("Size " + size);
                byte[] data = readBytes(size, in);
                Property prop = getProperty(propID);
                prop.read(new ByteArrayInputStream(data));
                //                System.out.println("Updated state: " + this);
            }
        } while (propID != 0);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(type.getName());
        result.append(" (");
        result.append(id);
        result.append(") [");
        for (Iterator<Property> it = getProperties().iterator(); it.hasNext();) {
            result.append(it.next().toString());
            if (it.hasNext()) {
                result.append(", ");
            }
        }
        result.append("]");
        return result.toString();
    }

    /**
       Notify all listeners that a property has changed.
       @param p The changed property.
    */
    protected void firePropertyChanged(Property p) {
        Collection<EntityListener> copy;
        synchronized (listeners) {
            copy = new HashSet<EntityListener>(listeners);
        }
        for (EntityListener next : copy) {
            next.propertyChanged(this, p);
        }
    }

    /**
       A class for forwarding property change events to entity listeners.
    */
    private class InternalPropertyListener implements PropertyListener {
        @Override
        public void propertyChanged(Property p) {
            firePropertyChanged(p);
        }
    }
}