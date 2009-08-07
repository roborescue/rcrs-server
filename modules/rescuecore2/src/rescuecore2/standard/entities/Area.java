package rescuecore2.standard.entities;

import java.util.List;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.properties.IntProperty;
import rescuecore2.worldmodel.properties.BooleanProperty;
import rescuecore2.worldmodel.properties.IntArrayProperty;
import rescuecore2.worldmodel.properties.EntityRefListProperty;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.misc.Pair;


/**
   The Area object.
 */
public class Area extends StandardEntity {

    private IntProperty center_x;
    private IntProperty center_y;
    private EntityRefListProperty neighbors;
    private IntProperty area_type;
    private IntArrayProperty shape;
    private EntityRefListProperty next_area;
    //private IntProperty block;
    //private IntProperty repair_cost;

    /**
       Construct a Area object with entirely undefined property values.
       @param id The ID of this entity.
     */
    public Area(EntityID id) {
	this(id, StandardEntityType.AREA);
    }
    public Area(EntityID id, StandardEntityType type) {
        super(id, type);
	center_x = new IntProperty(StandardPropertyType.X);
	center_y = new IntProperty(StandardPropertyType.Y);
	neighbors = new EntityRefListProperty(StandardPropertyType.NEIGHBORS);
        area_type = new IntProperty(StandardPropertyType.AREA_TYPE);
	shape = new IntArrayProperty(StandardPropertyType.AREA_APEXES);
	next_area = new EntityRefListProperty(StandardPropertyType.NEXT_AREA);
        //block = new IntProperty(StandardPropertyType.BLOCK);
	//repair_cost = new IntProperty(StandardPropertyType.REPAIR_COST);
        addProperties(center_x, center_y, neighbors, area_type, shape, next_area);
    }

    public Pair<Integer, Integer> getLocation(WorldModel<? extends StandardEntity> world) {
	return new Pair<Integer, Integer>(center_x.getValue(), center_y.getValue());
    }

    @Override
    protected Entity copyImpl() {
        return new Area(getID());
    }


    /**
       Get the area kind property.
       @return The area kind property.
     */
    public IntProperty getAreaTypeProperty() {
        return area_type;
    }

    /**
       Get the value of the area kind property.
       @return The area kind.
     */
    public int getAreaType() {
        return area_type.getValue();
    }

    /**
       Set the value of the area kind property.
       @param newKind The new area kind.
    */
    public void setAreaType(int newType) {
        this.area_type.setValue(newType);
    }

    /**
       Find out if the area kind property has been defined.
       @return True if the area kind property has been defined, false otherwise.
     */
    public boolean isAreaTypeDefined() {
        return area_type.isDefined();
    }

    /**
       Undefine the area kind property.
    */
    public void undefineAreaType() {
        area_type.undefine();
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


    public List<EntityID> getNeighbors() {
        return neighbors.getValue();
    }
    public void setNeighbors(List<EntityID> newKind) {
        this.neighbors.setValue(newKind);
    }

    /*
    public int[] getAbstractDistanceToNeightbors() {
        return abstract_distance_to_neighbors.getValue();
    }
    public void setAbstractDistanceToNeighbors(int[] newKind) {
        this.abstract_distance_to_neighbors.setValue(newKind);
    }
    */


    public int[] getShape() {
        return shape.getValue();
    }
    public List<EntityID> getNextArea() {
        return next_area.getValue();
    }
    public void setShape(int[] newShape, List<EntityID> newNextArea) {
        this.shape.setValue(newShape);
        this.next_area.setValue(newNextArea);
    }

}