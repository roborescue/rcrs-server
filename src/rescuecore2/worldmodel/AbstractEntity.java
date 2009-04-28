package rescuecore2.worldmodel;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

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
        for (Property next : props) {
            properties.put(next.getID(), next);
        }
    }

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
}