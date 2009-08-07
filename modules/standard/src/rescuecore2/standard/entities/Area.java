package rescuecore2.standard.entities;

import java.util.*;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.properties.IntProperty;
import rescuecore2.worldmodel.properties.BooleanProperty;
import rescuecore2.worldmodel.properties.IntArrayProperty;
import rescuecore2.worldmodel.properties.EntityRefListProperty;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.misc.Pair;
import rescuecore2.worldmodel.Property;

/**
   The Area object.
 */
public class Area extends StandardEntity {

    private IntProperty x;
    private IntProperty y;
    private IntProperty groundArea;
    private IntArrayProperty apexes;
    private EntityRefListProperty entrances;
    private IntProperty area_type;
    private EntityRefListProperty next_area;
    private EntityRefListProperty blockade_list;
    //private IntProperty repair_cost;

    /**
       Construct a Area object with entirely undefined property values.
       @param id The ID of this entity.
     */
    public Area(EntityID id) {
	this(id, StandardEntityURN.AREA);
    }
    

    public Area(EntityID id, StandardEntityURN urn) {
        super(id, urn);
	x = new IntProperty(StandardPropertyURN.X);
	y = new IntProperty(StandardPropertyURN.Y);
        groundArea = new IntProperty(StandardPropertyURN.BUILDING_AREA_GROUND);
        apexes = new IntArrayProperty(StandardPropertyURN.BUILDING_APEXES);
        entrances = new EntityRefListProperty(StandardPropertyURN.ENTRANCES);
        area_type = new IntProperty(StandardPropertyURN.AREA_TYPE);
	next_area = new EntityRefListProperty(StandardPropertyURN.NEXT_AREA);
	blockade_list = new EntityRefListProperty(StandardPropertyURN.BLOCKADE_LIST);
    }

    public Area(Area other) {
        super(other);
	x = new IntProperty(other.x);
	y = new IntProperty(other.y);
        groundArea = new IntProperty(other.groundArea);
        apexes = new IntArrayProperty(other.apexes);
        entrances = new EntityRefListProperty(other.entrances);
        area_type = new IntProperty(other.area_type);
	next_area = new EntityRefListProperty(other.next_area);
	blockade_list = new EntityRefListProperty(other.blockade_list);
    }
    
    public Pair<Integer, Integer> getLocation(WorldModel<? extends StandardEntity> world) {
	return new Pair<Integer, Integer>(x.getValue(), y.getValue());
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
            return x;
        case Y:
            return y;
        case BUILDING_APEXES:
            return apexes;
        case ENTRANCES:
            return entrances;
        case AREA_TYPE:
            return area_type;
        case NEXT_AREA:
            return next_area;
        case BLOCKADE_LIST:
            return blockade_list;
        default:
            return super.getProperty(urn);
        }
    }

    @Override
    public Set<Property> getProperties() {
        Set<Property> result = super.getProperties();
        result.add(x);
        result.add(y);
        result.add(apexes);
        result.add(entrances);
        result.add(area_type);
        result.add(next_area);
        result.add(blockade_list);
        return result;
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
       Get the value of the x kind property.
       @return The x kind.
     */
    public int getCenterX() {
        return x.getValue();
    }

    public void setCenter(int x, int y) {
	setCenterX(x);
	setCenterY(y);
    }

    /**
       Set the value of the x kind property.
       @param newKind The new x kind.
    */
    public void setCenterX(int newKind) {
        this.x.setValue(newKind);
    }

    public int getCenterY() {
        return y.getValue();
    }
    public void setCenterY(int newKind) {
        this.y.setValue(newKind);
    }

    public List<EntityID> getNeighbors() {
	ArrayList<EntityID> list = new ArrayList<EntityID>();
	for (EntityID id : getNextArea()) {
	    if (id != null && id.getValue() != -1) {
		list.add(id);
            }
        }
        return list;
    }

    /*
    public int[] getAbstractDistanceToNeightbors() {
        return abstract_distance_to_neighbors.getValue();
    }
    public void setAbstractDistanceToNeighbors(int[] newKind) {
        this.abstract_distance_to_neighbors.setValue(newKind);
    }
    */

    public boolean isBlockadeListDefined() {
        return blockade_list.isDefined();
    }
    public void undefineBlockadeList() {
        blockade_list.undefine();
    }
    public List<EntityID> getBlockadeList() {
	return blockade_list.getValue();
    }
    public void setBlockadeList(List<EntityID> new_list) {
	blockade_list.setValue(new_list);
    }
    public List<EntityID> getNearBlockadeList(WorldModel<? extends StandardEntity> world) {
	ArrayList<EntityID> blockade_list = new ArrayList<EntityID>();
	blockade_list.addAll(getBlockadeList());
	for(EntityID neighbor_id : getNeighbors()) {
	    Entity entity = world.getEntity(neighbor_id);
	    assert (entity instanceof Area) : "neighbor is not Area!";
	    Area area = (Area)entity;
	    for(EntityID blockade_id : area.getBlockadeList())
		blockade_list.add(blockade_id);
	}
	return blockade_list;
    }
    public EntityID getNearlestBlockade(int x, int y, WorldModel<? extends StandardEntity> world) {

	double min = 0;
	EntityID min_id = null;
	for(EntityID blockade_id : getNearBlockadeList(world)) {
	    Entity entity = world.getEntity(blockade_id);
	    //assert (entity instanceof Area) : "entity is not Area!";
	    Pair<Integer, Integer> location = null;
	    if(entity instanceof Building)
		location = ((Building)entity).getLocation(world);
	    else if(entity instanceof Area)
		location = ((Area)entity).getLocation(world);
	    else if(entity instanceof Blockade)
		location = ((Blockade)entity).getLocation(world);
	    
	    assert (location!=null) : "blockade location is null ["+blockade_id+"]";
	    if(location==null || location.first()==null){
		System.err.println("!"+entity+":"+location);

	    }

	    double dx = (location.first()-x);
	    double dy = (location.second()-y);
	    double distance = Math.sqrt(dx*dx + dy*dy);
	    if(min_id==null || min > distance) {
		min = distance;
		min_id = entity.getID();
	    }
	}

	return min_id;
    }

    public int[] getApexes() {
        return apexes.getValue();
    }
    public List<EntityID> getNextArea() {
        return next_area.getValue();
    }
    public void setApexes(int[] newShape, List<EntityID> newNextArea) {
        this.apexes.setValue(newShape);
        this.next_area.setValue(newNextArea);
    }

}