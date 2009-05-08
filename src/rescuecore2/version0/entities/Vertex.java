package rescuecore2.version0.entities;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.EntityType;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.misc.Pair;

import rescuecore2.version0.entities.properties.IntProperty;
import rescuecore2.version0.entities.properties.IntArrayProperty;
import rescuecore2.version0.entities.properties.PropertyType;

/**
   Vertex-type entities (e.g. nodes).
 */
public abstract class Vertex extends RescueObject {
    private IntProperty x;
    private IntProperty y;
    private IntArrayProperty edges;

    /**
       Construct a Vertex object with entirely undefined property values.
       @param id The ID of this entity.
       @param type The type ID of this entity.
       @param props The set of properties this entity has.
     */
    protected Vertex(EntityID id, EntityType type, Property... props) {
        super(id, type, props);
        x = new IntProperty(PropertyType.X);
        y = new IntProperty(PropertyType.Y);
        edges = new IntArrayProperty(PropertyType.EDGES);
        addProperties(x, y, edges);
    }

    @Override
    public Pair<Integer, Integer> getLocation(WorldModel<? extends RescueObject> world) {
        return new Pair<Integer, Integer>(x.getValue(), y.getValue());
    }
}