package rescuecore2.worldmodel.entities.legacy;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.properties.IntProperty;
import rescuecore2.worldmodel.properties.IntArrayProperty;
import rescuecore2.worldmodel.properties.BooleanProperty;

/**
   The Node object.
 */
public class Node extends Vertex {
    /**
       Construct a Node object with entirely undefined values.
     */
    public Node(EntityID id) {
	super(EntityType.NODE,
	      id,
	      // Node properties
	      new BooleanProperty(PropertyType.SIGNAL.getName()),
	      new IntArrayProperty(PropertyType.SHORTCUT_TO_TURN.getName()),
	      new IntArrayProperty(PropertyType.POCKET_TO_TURN_ACROSS.getName()),
	      new IntArrayProperty(PropertyType.SIGNAL_TIMING.getName()),
	      // Vertex properties
	      new IntProperty(PropertyType.X.getName()),
	      new IntProperty(PropertyType.Y.getName()),
	      new IntArrayProperty(PropertyType.EDGES.getName())
	      );
    }
}