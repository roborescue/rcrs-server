package rescuecore2.version0.entities;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.EntityType;
import rescuecore2.worldmodel.Property;

/**
   Edge-type entities (e.g. roads).
 */
public abstract class Edge extends RescueObject {
    /**
       Construct an Edge object with entirely undefined values.
     */
    protected Edge(EntityID id, EntityType type, Property... props) {
        super(id, type, props);
    }
}