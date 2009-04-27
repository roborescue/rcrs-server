package rescuecore2.version0.entities;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.EntityType;
import rescuecore2.worldmodel.Property;

/**
   Vertex-type entities (e.g. nodes).
 */
public abstract class Vertex extends RescueObject {
    /**
       Construct a Vertex object with entirely undefined values.
     */
    protected Vertex(EntityID id, EntityType type, Property... props) {
        super(id, type, props);
    }
}