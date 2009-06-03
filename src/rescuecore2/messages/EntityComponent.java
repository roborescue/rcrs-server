package rescuecore2.messages;

import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.writeInt32;
import static rescuecore2.misc.EncodingTools.readBytes;
import static rescuecore2.misc.EncodingTools.INT_32_SIZE;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.EntityRegistry;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
   An Entity component to a message.
 */
public class EntityComponent extends AbstractMessageComponent {
    private Entity entity;

    /**
       Construct an EntityComponent with no content.
       @param name The name of the component.
     */
    public EntityComponent(String name) {
        super(name);
        entity = null;
    }

    /**
       Construct an EntityComponent with a specific entity value.
       @param name The name of the component.
       @param entity The value of this component.
     */
    public EntityComponent(String name, Entity entity) {
        super(name);
        this.entity = entity;
    }

    /**
       Get the entity.
       @return The entity.
     */
    public Entity getEntity() {
        return entity;
    }

    /**
       Set the entity.
       @param e The new entity.
     */
    public void setEntity(Entity e) {
        entity = e;
    }

    @Override
    public void write(OutputStream out) throws IOException {
        ByteArrayOutputStream gather = new ByteArrayOutputStream();
        writeInt32(entity.getID().getValue(), gather);
        entity.write(gather);
        // Type
        writeInt32(entity.getType().getID(), out);
        // Size
        byte[] bytes = gather.toByteArray();
        writeInt32(bytes.length, out);
        out.write(bytes);
    }

    @Override
    public void read(InputStream in) throws IOException {
        int typeID = readInt32(in);
        int size = readInt32(in);
        byte[] data = readBytes(size, in);
        EntityID id = new EntityID(readInt32(data));
        entity = EntityRegistry.createEntity(typeID, id);
        entity.read(new ByteArrayInputStream(data, INT_32_SIZE, data.length - INT_32_SIZE));
    }

    @Override
    public String toString() {
        return getName() + " = " + entity.toString();
    }
}