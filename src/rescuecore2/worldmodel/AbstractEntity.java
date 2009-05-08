package rescuecore2.worldmodel;

import static rescuecore2.misc.EncodingTools.writeInt32;
import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.readBytes;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
   Abstract base class for concrete Entity implementations.
 */
public abstract class AbstractEntity implements Entity {
    /** Map from id to Property. */
    private final Map<Integer, Property> properties;
    private final EntityID id;
    private final EntityType type;

    /**
       Construct an AbstractEntity with a set of properties.
       @param id The ID of this entity.
       @param type The type of this entity.
       @param props The properties of this entity.
    */
    protected AbstractEntity(EntityID id, EntityType type, Property... props) {
        this.id = id;
        this.type = type;
        properties = new HashMap<Integer, Property>();
        addProperties(props);
    }

    /**
       Add a set of properties to this entity. This should only be used by subclasses during construction.
       @param props The properties to add.
    */
    protected void addProperties(Property... props) {
        for (Property next : props) {
            properties.put(next.getID(), next);
        }
    }

    @Override
    public Entity copy() {
        Entity result = copyImpl();
        for (Map.Entry<Integer, Property> next : properties.entrySet()) {
            Property original = next.getValue();
            Property copy = result.getProperty(next.getKey());
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
    public Property getProperty(int propID) {
        return properties.get(propID);
    }

    @Override
    public EntityID getID() {
        return id;
    }

    @Override
    public EntityType getType() {
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
}