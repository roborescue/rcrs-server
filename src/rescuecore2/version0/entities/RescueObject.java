package rescuecore2.version0.entities;

import static rescuecore2.misc.EncodingTools.writeInt32;
import static rescuecore2.misc.EncodingTools.readInt32;

import rescuecore2.worldmodel.AbstractEntity;
import rescuecore2.worldmodel.EntityType;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

/**
   Abstract base class for all version0 entities.
 */
public abstract class RescueObject extends AbstractEntity {
    /**
       Construct a RescueObject with entirely undefined property values.
       @param id The ID of this entity.
       @param type The type ID of this entity.
       @param props The set of properties this entity has.
     */
    protected RescueObject(EntityID id, EntityType type, Property... props) {
        super(id, type, props);
    }

    @Override
    public void write(OutputStream out) throws IOException {
        writeInt32(getID().getValue(), out);
        for (Property next : getProperties()) {
            ByteArrayOutputStream gather = new ByteArrayOutputStream();
            next.write(gather);
            byte[] bytes = gather.toByteArray();
            //   Type
            writeInt32(next.getID(), out);
            //   Size
            writeInt32(bytes.length, out);
            //   Data
            out.write(bytes);
        }
        // end-of-properties marker
        writeInt32(0, out);
    }

    @Override
    public void read(InputStream in) throws IOException {
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(getType().getName());
        result.append(" [");
        for (Iterator<Property> it = getProperties().iterator(); it.hasNext();) {
            result.append(it.next().toString());
            if (it.hasNext()) {
                result.append(", ");
            }
        }
        result.append("]");
        return result.toString();
    }
}