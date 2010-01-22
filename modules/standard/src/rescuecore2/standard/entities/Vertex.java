package rescuecore2.standard.entities;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.properties.IntProperty;
import rescuecore2.worldmodel.properties.EntityRefListProperty;

import rescuecore2.misc.Pair;

import java.util.List;

/**
   Vertex-type entities (e.g. nodes).
 */
public abstract class Vertex extends StandardEntity {
    private IntProperty x;
    private IntProperty y;
    private EntityRefListProperty edges;

    /**
       Construct a Vertex object with entirely undefined property values.
       @param id The ID of this entity.
       @param urn The urn of this entity.
     */
    protected Vertex(EntityID id, StandardEntityURN urn) {
        super(id, urn);
        x = new IntProperty(StandardPropertyURN.X);
        y = new IntProperty(StandardPropertyURN.Y);
        edges = new EntityRefListProperty(StandardPropertyURN.EDGES);
        registerProperties(x, y, edges);
    }

    /**
       Vertex copy constructor.
       @param other The Vertex to copy.
     */
    public Vertex(Vertex other) {
        super(other);
        x = new IntProperty(other.x);
        y = new IntProperty(other.y);
        edges = new EntityRefListProperty(other.edges);
        registerProperties(x, y, edges);
    }

    @Override
    public Property getProperty(String urn) {
        StandardPropertyURN type;
        try {
            type = StandardPropertyURN.valueOf(urn);
        }
        catch (IllegalArgumentException e) {
            return super.getProperty(urn);
        }
        switch (type) {
        case X:
            return x;
        case Y:
            return y;
        case EDGES:
            return edges;
        default:
            return super.getProperty(urn);
        }
    }

    @Override
    public Pair<Integer, Integer> getLocation(WorldModel<? extends StandardEntity> world) {
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