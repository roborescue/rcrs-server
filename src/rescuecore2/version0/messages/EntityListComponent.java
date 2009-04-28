package rescuecore2.version0.messages;

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

import rescuecore2.messages.AbstractMessageComponent;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.EntityType;

import rescuecore2.version0.entities.RescueEntityFactory;

/**
   An EntityList component to a message.
 */
public class EntityListComponent extends AbstractMessageComponent {
    private List<Entity> entities;

    /**
       Construct an EntityListComponent with no content.
       @param name The name of the component.
    */
    public EntityListComponent(String name) {
        super(name);
        entities = new ArrayList<Entity>();
    }

    /**
       Construct an EntityListComponent with a specific list of entities.
       @param name The name of the component.
       @param entities The entities in this message component.
    */
    public EntityListComponent(String name, Collection<Entity> entities) {
        super(name);
        this.entities = new ArrayList<Entity>(entities);
    }

    /**
       Get the entities that make up this message component.
       @return The entities in this component.
    */
    public List<Entity> getEntities() {
        return entities;
    }

    /**
       Set the entities that make up this message component.
       @param entities The entities in this component.
    */
    public void setEntities(Collection<Entity> entities) {
        this.entities = new ArrayList<Entity>(entities);
    }

    @Override
    public void write(OutputStream out) throws IOException {
        for (Entity next : entities) {
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
        System.out.println("Reading entity list");
        entities.clear();
        int typeID;
        do {
            typeID = readInt32(in);
            if (typeID != 0) {
                EntityType type = RescueEntityFactory.INSTANCE.makeEntityType(typeID);
                int size = readInt32(in);
                byte[] data = readBytes(size, in);
                ByteArrayInputStream eIn = new ByteArrayInputStream(data);
                EntityID id = new EntityID(readInt32(eIn));
                Entity e = RescueEntityFactory.INSTANCE.makeEntity(type, id);
                e.read(eIn);
                entities.add(e);
            }
        } while (typeID != 0);
    }

    @Override
    public String toString() {
        return entities.size() + " entities";
    }
}