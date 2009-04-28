package rescuecore2.version0.entities;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.EntityType;
import rescuecore2.worldmodel.Property;

/**
   Vertex-type entities (e.g. nodes).
 */
public abstract class Vertex extends RescueObject {
    /**
       Construct a Vertex object with entirely undefined property values.
       @param id The ID of this entity.
       @param type The type ID of this entity.
       @param props The set of properties this entity has.
     */
    protected Vertex(EntityID id, EntityType type, Property... props) {
        super(id, type, props);
    }
}