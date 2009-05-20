package rescuecore2.version0.entities;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.EntityType;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.properties.IntProperty;
import rescuecore2.worldmodel.properties.EntityRefListProperty;

import rescuecore2.misc.Pair;

import java.util.List;

/**
   Vertex-type entities (e.g. nodes).
 */
public abstract class Vertex extends RescueObject {
    private IntProperty x;
    private IntProperty y;
    private EntityRefListProperty edges;

    /**
       Construct a Vertex object with entirely undefined property values.
       @param id The ID of this entity.
       @param type The type ID of this entity.
     */
    protected Vertex(EntityID id, RescueEntityType type) {
        super(id, type);
        x = new IntProperty(RescuePropertyType.X);
        y = new IntProperty(RescuePropertyType.Y);
        edges = new EntityRefListProperty(RescuePropertyType.EDGES);
        addProperties(x, y, edges);
    }

    @Override
    public Pair<Integer, Integer> getLocation(WorldModel<? extends RescueObject> world) {
        return new Pair<Integer, Integer>(x.getValue(), y.getValue());
    }

    /**
       Get the x property.
       @return The x property.
     */
    public IntProperty getXProperty() {
        return x;
    }

    /**
       Get the value of the x property.
       @return The value of the x property.
     */
    public int getX() {
        return x.getValue();
    }

    /**
       Set the x property.
       @param x The new x.
    */
    public void setX(int x) {
        this.x.setValue(x);
    }

    /**
       Find out if the x property has been defined.
       @return True if the x property has been defined, false otherwise.
     */
    public boolean isXDefined() {
        return x.isDefined();
    }

    /**
       Undefine the x property.
    */
    public void undefineX() {
        x.undefine();
    }

    /**
       Get the y property.
       @return The y property.
     */
    public IntProperty getYProperty() {
        return y;
    }

    /**
       Get the value of the y property.
       @return The value of the y property.
     */
    public int getY() {
        return y.getValue();
    }

    /**
       Set the y property.
       @param y The new y.
    */
    public void setY(int y) {
        this.y.setValue(y);
    }

    /**
       Find out if the y property has been defined.
       @return True if the y property has been defined, false otherwise.
     */
    public boolean isYDefined() {
        return y.isDefined();
    }

    /**
       Undefine the y property.
    */
    public void undefineY() {
        y.undefine();
    }

    /**
       Get the edges property.
       @return The edges property.
     */
    public EntityRefListProperty getEdgesProperty() {
        return edges;
    }

    /**
       Get the value of the edges property.
       @return The value of the edges property.
     */
    public List<EntityID> getEdges() {
        return edges.getValue();
    }

    /**
       Set the edges property.
       @param edges The new edges.
    */
    public void setEdges(List<EntityID> edges) {
        this.edges.setValue(edges);
    }

    /**
       Find out if the edges property has been defined.
       @return True if the edges property has been defined, false otherwise.
     */
    public boolean isEdgesDefined() {
        return edges.isDefined();
    }

    /**
       Undefine the edges property.
    */
    public void undefineEdges() {
        edges.undefine();
    }
}