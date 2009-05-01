package rescuecore2.worldmodel.entities.legacy;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.properties.IntProperty;

/**
   Edge-type entities (e.g. roads).
 */
public abstract class Edge extends AbstractLegacyEntity {
    /**
       Construct an Edge object with entirely undefined values.
     */
    protected Edge(EntityType type, EntityID id, Property... props) {
	super(type,
	      id,
	      props
	      );
    }
}