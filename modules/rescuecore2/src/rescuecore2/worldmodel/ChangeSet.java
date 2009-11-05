package rescuecore2.worldmodel;

import static rescuecore2.misc.EncodingTools.writeInt32;
import static rescuecore2.misc.EncodingTools.writeProperty;
import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.readProperty;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Collection;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;

import rescuecore2.misc.collections.LazyMap;

/**
   This class is used for accumulating changes to entities.
 */
public class ChangeSet {
    private Map<EntityID, Set<Property>> changes;

    /**
       Create an empty ChangeSet.
     */
    public ChangeSet() {
        changes = new LazyMap<EntityID, Set<Property>>() {
            @Override
            public Set<Property> createValue() {
                return new HashSet<Property>();
            }
        };
    }

    /**
       Copy constructor.
       @param other The ChangeSet to copy.
    */
    public ChangeSet(ChangeSet other) {
        this();
        merge(other);
    }

    /**
       Add a change.
       @param e The entity that has changed.
       @param p The property that has changed.
     */
    public void addChange(Entity e, Property p) {
        addChange(e.getID(), p);
    }

    /**
       Add a change.
       @param e The ID of the entity that has changed.
       @param p The property that has changed.
     */
    public void addChange(EntityID e, Property p) {
        changes.get(e).add(p.copy());
    }

    /**
       Get the properties that have changed for an entity.
       @param e The entity ID to look up.
       @return The set of changed properties. This may be empty but will never be null.
    */
    public Set<Property> getChangedProperties(EntityID e) {
        return changes.get(e);
    }

    /**
       Look up a property change for an entity by property URN.
       @param e The entity ID to look up.
       @param urn The property URN to look up.
       @return The changed property with the right URN, or null if the property is not found or has not changed.
    */
    public Property getChangedProperty(EntityID e, String urn) {
        Set<Property> props = changes.get(e);
        if (props != null) {
            for (Property next : props) {
                if (next.getURN().equals(urn)) {
                    return next;
                }
            }
        }
        return null;
    }

    /**
       Get the IDs of all changed entities.
       @return A set of IDs of changed entities.
     */
    public Set<EntityID> getChangedEntities() {
        return new HashSet<EntityID>(changes.keySet());
    }

    /**
       Merge another ChangeSet into this one.
       @param other The other ChangeSet.
     */
    public void merge(ChangeSet other) {
        for (Map.Entry<EntityID, Set<Property>> next : other.changes.entrySet()) {
            EntityID e = next.getKey();
            for (Property p : next.getValue()) {
                addChange(e, p);
            }
        }
    }

    /**
       Add all defined properties from a collection.
       @param c The collection to copy changes from.
    */
    public void addAll(Collection<? extends Entity> c) {
        for (Entity entity : c) {
            for (Property property : entity.getProperties()) {
                if (property.isDefined()) {
                    addChange(entity, property);
                }
            }
        }
    }

    /**
       Write this ChangeSet to a stream.
       @param out The stream to write to.
       @throws IOException If there is a problem.
    */
    public void write(OutputStream out) throws IOException {
        // Number of entity IDs
        writeInt32(changes.size(), out);
        for (Map.Entry<EntityID, Set<Property>> next : changes.entrySet()) {
            Set<Property> props = next.getValue();
            // EntityID, number of properties
            writeInt32(next.getKey().getValue(), out);
            writeInt32(props.size(), out);
            for (Property prop : props) {
                writeProperty(prop, out);
            }
        }
    }

    /**
       Read this ChangeSet from a stream.
       @param in The stream to read from.
       @throws IOException If there is a problem.
    */
    public void read(InputStream in) throws IOException {
        changes.clear();
        int entityCount = readInt32(in);
        for (int i = 0; i < entityCount; ++i) {
            EntityID id = new EntityID(readInt32(in));
            int propCount = readInt32(in);
            for (int j = 0; j < propCount; ++j) {
                Property p = readProperty(in);
                if (p != null) {
                    addChange(id, p);
                }
            }
        }
    }
}