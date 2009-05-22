package rescuecore2.messages;

import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.writeInt32;
import static rescuecore2.misc.EncodingTools.readBytes;
import static rescuecore2.misc.EncodingTools.INT_32_SIZE;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.EntityType;
import rescuecore2.worldmodel.EntityFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
   An Entity component to a message.
   @param <T> The subtype of EntityType that this component knows about.
   @param <E> The subtype of Entity that this component knows about.
 */
public class EntityComponent<T extends EntityType, E extends Entity> extends AbstractMessageComponent {
    private E entity;
    private EntityFactory<T, E> factory;

    /**
       Construct an EntityComponent with no content.
       @param name The name of the component.
       @param factory A factory for reading entities from an input stream.
     */
    public EntityComponent(String name, EntityFactory<T, E> factory) {
        super(name);
        this.factory = factory;
        entity = null;
    }

    /**
       Construct an EntityComponent with a specific entity value.
       @param name The name of the component.
       @param factory A factory for reading entities from an input stream.
       @param entity The value of this component.
     */
    public EntityComponent(String name, EntityFactory<T, E> factory, E entity) {
        this(name, factory);
        this.entity = entity;
    }

    /**
       Get the entity.
       @return The entity.
     */
    public E getEntity() {
        return entity;
    }

    /**
       Set the entity.
       @param e The new entity.
     */
    public void setEntity(E e) {
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
        T type = factory.makeEntityType(readInt32(in));
        int size = readInt32(in);
        byte[] data = readBytes(size, in);
        EntityID id = new EntityID(readInt32(data));
        entity = factory.makeEntity(type, id);
        entity.read(new ByteArrayInputStream(data, INT_32_SIZE, data.length - INT_32_SIZE));
    }

    @Override
    public String toString() {
        return getName() + " = " + entity.toString();
    }
}