package rescuecore2.version0.entities;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.version0.entities.properties.IntProperty;
import rescuecore2.version0.entities.properties.IntArrayProperty;
import rescuecore2.version0.entities.properties.BooleanProperty;
import rescuecore2.version0.entities.properties.PropertyType;

/**
   The Node object.
 */
public class Node extends Vertex {
    /**
       Construct a Node object with entirely undefined values.
       @param id The ID of this entity.
     */
    public Node(EntityID id) {
        super(id,
              EntityConstants.NODE,
              // Node properties
              new BooleanProperty(PropertyType.SIGNAL),
              new IntArrayProperty(PropertyType.SHORTCUT_TO_TURN),
              new IntArrayProperty(PropertyType.POCKET_TO_TURN_ACROSS),
              new IntArrayProperty(PropertyType.SIGNAL_TIMING),
              // Vertex properties
              new IntProperty(PropertyType.X),
              new IntProperty(PropertyType.Y),
              new IntArrayProperty(PropertyType.EDGES)
              );
    }

    @Override
    protected Entity copyImpl() {
        return new Node(getID());
    }
}