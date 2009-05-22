package rescuecore2.messages;

import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.writeInt32;
import static rescuecore2.misc.EncodingTools.readBytes;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.EntityType;
import rescuecore2.worldmodel.EntityFactory;

/**
   An EntityList component to a message.
 */
public class EntityListComponent<T extends EntityType, E extends Entity> extends AbstractMessageComponent {
    private List<E> entities;
    private EntityFactory<T, E> factory;

    /**
       Construct an EntityListComponent with no content.
       @param name The name of the component.
       @param factory A factory for reading entities from an input stream.
    */
    public EntityListComponent(String name, EntityFactory<T, E> factory) {
        super(name);
        this.factory = factory;
        entities = new ArrayList<E>();
    }

    /**
       Construct an EntityListComponent with a specific list of entities.
       @param name The name of the component.
       @param factory A factory for reading entities from an input stream.
       @param entities The entities in this message component.
    */
    public EntityListComponent(String name, EntityFactory<T, E> factory, Collection<E> entities) {
        this(name, factory);
        this.entities = new ArrayList<E>(entities);
    }

    /**
       Get the entities that make up this message component.
       @return The entities in this component.
    */
    public List<E> getEntities() {
        return entities;
    }

    /**
       Set the entities that make up this message component.
       @param entities The entities in this component.
    */
    public void setEntities(Collection<E> entities) {
        this.entities = new ArrayList<E>(entities);
    }

    @Override
    public void write(OutputStream out) throws IOException {
        for (E next : entities) {
            ByteArrayOutputStream gather = new ByteArrayOutputStream();
            writeInt32(next.getID().getValue(), gather);
            next.write(gather);
            // Type
            writeInt32(next.getType().getID(), out);
            // Size
            byte[] bytes = gather.toByteArray();
            writeInt32(bytes.length, out);
            out.write(bytes);
        }
        // End-of-list marker
        writeInt32(0, out);
    }

    @Override
    public void read(InputStream in) throws IOException {
        //        System.out.println("Reading entity list");
        entities.clear();
        int typeID;
        do {
            typeID = readInt32(in);
            if (typeID != 0) {
                T type = factory.makeEntityType(typeID);
                int size = readInt32(in);
                byte[] data = readBytes(size, in);
                ByteArrayInputStream eIn = new ByteArrayInputStream(data);
                EntityID id = new EntityID(readInt32(eIn));
                E e = factory.makeEntity(type, id);
                //                System.out.println("Reading " + e);
                //                System.out.println("Size: " + size);
                e.read(eIn);
                entities.add(e);
            }
        } while (typeID != 0);
    }

    @Override
    public String toString() {
        return getName() + " = " + entities.size() + " entities";
    }
}