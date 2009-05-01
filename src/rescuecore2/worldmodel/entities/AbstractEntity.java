package rescuecore2.worldmodel.entities;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;

/**
   Abstract base class for concrete Entity implementations.
 */
public abstract class AbstractEntity implements Entity {
    /** Map from name to Property */
    private final Map<String, Property> properties;
    private final EntityID id;

    /**
       Construct an AbstractEntity with a set of properties.
       @param props The properties of this entity.
     */
    protected AbstractEntity(EntityID id, Property... props) {
	this.id = id;
	properties = new HashMap<String, Property>();
	for (Property next : props) {
	    properties.put(next.getName(), next);
	}
    }

    @Override
    public Set<Property> getProperties() {
	return new HashSet<Property>(properties.values());
    }

    @Override
    public Property getProperty(String name) {
	return properties.get(name);
    }

    @Override
    public EntityID getID() {
	return id;
    }
}