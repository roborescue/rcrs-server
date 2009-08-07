package rescuecore2.standard.entities;

import java.util.List;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.properties.IntProperty;
import rescuecore2.worldmodel.properties.BooleanProperty;
import rescuecore2.worldmodel.properties.IntArrayProperty;
import rescuecore2.worldmodel.properties.EntityRefListProperty;
import rescuecore2.worldmodel.properties.EntityRefProperty;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.misc.Pair;
import java.util.Set;


/**
   The Area object.
 */
public class Blockade extends StandardEntity {

    private IntProperty center_x;
    private IntProperty center_y;
    private EntityRefProperty area;
    private IntArrayProperty shape;
    private IntProperty repair_cost;

    /**
       Construct a Area object with entirely undefined property values.
       @param id The ID of this entity.
     */
    public Blockade(EntityID id) {
	this(id, StandardEntityURN.BLOCKADE);
    }
    
    public Blockade(EntityID id, StandardEntityURN urn) {
        super(id, urn);
	center_x = new IntProperty(StandardPropertyURN.X);
	center_y = new IntProperty(StandardPropertyURN.Y);
	area = new EntityRefProperty(StandardPropertyURN.AREA);
	shape = new IntArrayProperty(StandardPropertyURN.AREA_APEXES);
      	repair_cost = new IntProperty(StandardPropertyURN.REPAIR_COST);
    }
    
    public Pair<Integer, Integer> getLocation(WorldModel<? extends StandardEntity> world) {
	return new Pair<Integer, Integer>(center_x.getValue(), center_y.getValue());
    }
    
    @Override
    protected Entity copyImpl() {
        return new Area(getID());
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
            return center_x;
        case Y:
            return center_y;
        case AREA:
            return area;
        case AREA_APEXES:
            return shape;
        case REPAIR_COST:
            return repair_cost;
        default:
            return super.getProperty(urn);
        }
    }

    @Override
    public Set<Property> getProperties() {
        Set<Property> result = super.getProperties();
        result.add(center_x);
        result.add(center_y);
        result.add(area);
        result.add(shape);
        result.add(repair_cost);
        return result;
    }


    /**
       Get the value of the center_x kind property.
       @return The center_x kind.
     */
    public int getCenterX() {
        return center_x.getValue();
    }

    public void setCenter(int x, int y) {
	setCenterX(x);
	setCenterY(y);
    }

    /**
       Set the value of the center_x kind property.
       @param newKind The new center_x kind.
    */
    public void setCenterX(int newKind) {
        this.center_x.setValue(newKind);
    }

    public int getCenterY() {
        return center_y.getValue();
    }
    public void setCenterY(int newKind) {
        this.center_y.setValue(newKind);
    }

    public EntityID getArea() {
	return area.getValue();
    }
    public void setArea(EntityID id) {
	area.setValue(id);
    }

    public int[] getShape() {
        return shape.getValue();
    }
    public void setShape(int[] newShape) {
        this.shape.setValue(newShape);
    }

}