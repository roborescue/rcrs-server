package rescuecore2.worldmodel.entities.legacy;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.properties.IntProperty;

/**
   Vertex-type entities (e.g. nodes).
 */
public abstract class Vertex extends AbstractLegacyEntity {
    /**
       Construct a Vertex object with entirely undefined values.
     */
    protected Vertex(EntityType type, EntityID id, Property... props) {
	super(type,
	      id,
	      props
	      );
    }
}