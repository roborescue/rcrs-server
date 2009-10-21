package rescuecore2.worldmodel;

import static rescuecore2.misc.EncodingTools.writeInt32;
import static rescuecore2.misc.EncodingTools.writeString;
import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.readString;
import static rescuecore2.misc.EncodingTools.readBytes;

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
 */
public abstract class AbstractEntity implements Entity {
    private static final String END_OF_PROPERTIES = "";

    /** Map from id to Property. */
    private final EntityID id;
    private final String urn;
    private final Set<EntityListener> listeners;

    /**
       Construct an AbstractEntity with a set of properties.
       @param id The ID of this entity.
       @param urn The urn of this entity.
    */
    protected AbstractEntity(EntityID id, String urn) {
        this.id = id;
        this.urn = urn;
        listeners = new HashSet<EntityListener>();
    }

    /**
       Construct an AbstractEntity with a set of properties.
       @param id The ID of this entity.
       @param urn The urn of this entity.
    */
    protected AbstractEntity(EntityID id, Enum<?> urn) {
        this(id, urn.name());
    }

    /**
       AbstractEntity copy constructor.
       @param other The AbstractEntity to copy.
     */
    protected AbstractEntity(AbstractEntity other) {
        this(other.getID(), other.getURN());
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
        for (Property original : getProperties()) {
            Property copy = result.getProperty(original.getURN());
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
        return new HashSet<Property>();
    }

    @Override
    public Property getProperty(String propertyURN) {
        return null;
    }

    @Override
    public EntityID getID() {
        return id;
    }

    @Override
    public String getURN() {
        return urn;
    }

    @Override
    public void write(OutputStream out) throws IOException {
        for (Property next : getProperties()) {
            ByteArrayOutputStream gather = new ByteArrayOutputStream();
            if (next.isDefined()) {
                next.write(gather);
                byte[] bytes = gather.toByteArray();
                //   Type
                writeString(next.getURN(), out);
                //   Size
                writeInt32(bytes.length, out);
                //   Data
                out.write(bytes);
            }
        }
        // end-of-properties marker
        writeString(END_OF_PROPERTIES, out);
    }

    @Override
    public void read(InputStream in) throws IOException {
        String propertyURN;
        while (true) {
            propertyURN = readString(in);
            if (END_OF_PROPERTIES.equals(propertyURN)) {
                return;
            }
            //                System.out.println("Reading property " + propID);
            int size = readInt32(in);
            //                System.out.println("Size " + size);
            byte[] data = readBytes(size, in);
            Property prop = getProperty(propertyURN);
            prop.read(new ByteArrayInputStream(data));
            //                System.out.println("Updated state: " + this);
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(urn);
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
}