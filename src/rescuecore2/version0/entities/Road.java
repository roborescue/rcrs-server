package rescuecore2.version0.entities;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.version0.entities.properties.IntProperty;
import rescuecore2.version0.entities.properties.BooleanProperty;
import rescuecore2.version0.entities.properties.PropertyType;

/**
   The Road object.
 */
public class Road extends Edge {
    private IntProperty kind;
    private IntProperty carsToHead;
    private IntProperty carsToTail;
    private IntProperty humansToHead;
    private IntProperty humansToTail;
    private IntProperty width;
    private IntProperty block;
    private IntProperty cost;
    private BooleanProperty hasMedian;
    private IntProperty linesToHead;
    private IntProperty linesToTail;
    private IntProperty widthForWalkers;

    /**
       Construct a Road object with entirely undefined property values.
       @param id The ID of this entity.
     */
    public Road(EntityID id) {
        super(id, EntityConstants.ROAD);
        kind = new IntProperty(PropertyType.ROAD_KIND);
        carsToHead = new IntProperty(PropertyType.CARS_PASS_TO_HEAD);
        carsToTail = new IntProperty(PropertyType.CARS_PASS_TO_TAIL);
        humansToHead = new IntProperty(PropertyType.HUMANS_PASS_TO_HEAD);
        humansToTail = new IntProperty(PropertyType.HUMANS_PASS_TO_TAIL);
        width = new IntProperty(PropertyType.WIDTH);
        block = new IntProperty(PropertyType.BLOCK);
        cost = new IntProperty(PropertyType.REPAIR_COST);
        hasMedian = new BooleanProperty(PropertyType.MEDIAN_STRIP);
        linesToHead = new IntProperty(PropertyType.LINES_TO_HEAD);
        linesToTail = new IntProperty(PropertyType.LINES_TO_TAIL);
        widthForWalkers = new IntProperty(PropertyType.WIDTH_FOR_WALKERS);
        addProperties(kind, carsToHead, carsToTail, humansToHead, humansToTail, width, block, cost, hasMedian, linesToHead, linesToTail, widthForWalkers);
    }

    @Override
    protected Entity copyImpl() {
        return new Road(getID());
    }
}