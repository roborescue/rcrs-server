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
    private BooleanProperty signal;
    private IntArrayProperty shortcut;
    private IntArrayProperty pocket;
    private IntArrayProperty timing;

    /**
       Construct a Node object with entirely undefined values.
       @param id The ID of this entity.
     */
    public Node(EntityID id) {
        super(id, EntityConstants.NODE);
        signal = new BooleanProperty(PropertyType.SIGNAL);
        shortcut = new IntArrayProperty(PropertyType.SHORTCUT_TO_TURN);
        pocket = new IntArrayProperty(PropertyType.POCKET_TO_TURN_ACROSS);
        timing = new IntArrayProperty(PropertyType.SIGNAL_TIMING);
        addProperties(signal, shortcut, pocket, timing);
    }

    @Override
    protected Entity copyImpl() {
        return new Node(getID());
    }
}