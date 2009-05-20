package rescuecore2.version0.entities;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.properties.IntProperty;
import rescuecore2.worldmodel.properties.BooleanProperty;

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
        super(id, RescueEntityType.ROAD);
        kind = new IntProperty(RescuePropertyType.ROAD_KIND);
        carsToHead = new IntProperty(RescuePropertyType.CARS_PASS_TO_HEAD);
        carsToTail = new IntProperty(RescuePropertyType.CARS_PASS_TO_TAIL);
        humansToHead = new IntProperty(RescuePropertyType.HUMANS_PASS_TO_HEAD);
        humansToTail = new IntProperty(RescuePropertyType.HUMANS_PASS_TO_TAIL);
        width = new IntProperty(RescuePropertyType.WIDTH);
        block = new IntProperty(RescuePropertyType.BLOCK);
        cost = new IntProperty(RescuePropertyType.REPAIR_COST);
        hasMedian = new BooleanProperty(RescuePropertyType.MEDIAN_STRIP);
        linesToHead = new IntProperty(RescuePropertyType.LINES_TO_HEAD);
        linesToTail = new IntProperty(RescuePropertyType.LINES_TO_TAIL);
        widthForWalkers = new IntProperty(RescuePropertyType.WIDTH_FOR_WALKERS);
        addProperties(kind, carsToHead, carsToTail, humansToHead, humansToTail, width, block, cost, hasMedian, linesToHead, linesToTail, widthForWalkers);
    }

    @Override
    protected Entity copyImpl() {
        return new Road(getID());
    }

    /**
       Get the road kind property.
       @return The road kind property.
     */
    public IntProperty getRoadKindProperty() {
        return kind;
    }

    /**
       Get the value of the road kind property.
       @return The road kind.
     */
    public int getRoadKind() {
        return kind.getValue();
    }

    /**
       Set the value of the road kind property.
       @param newKind The new road kind.
    */
    public void setRoadKind(int newKind) {
        this.kind.setValue(newKind);
    }

    /**
       Find out if the road kind property has been defined.
       @return True if the road kind property has been defined, false otherwise.
     */
    public boolean isRoadKindDefined() {
        return kind.isDefined();
    }

    /**
       Undefine the road kind property.
    */
    public void undefineRoadKind() {
        kind.undefine();
    }

    /**
       Get the carsToHead property.
       @return The carsToHead property.
     */
    public IntProperty getCarsToHeadProperty() {
        return carsToHead;
    }

    /**
       Get the value of the carsToHead property.
       @return The value of the carsToHead property.
     */
    public int getCarsToHead() {
        return carsToHead.getValue();
    }

    /**
       Set the value of the carsToHead property.
       @param carsToHead The new carsToHead property value.
    */
    public void setCarsToHead(int carsToHead) {
        this.carsToHead.setValue(carsToHead);
    }

    /**
       Find out if the carsToHead property has been defined.
       @return True if the carsToHead property has been defined, false otherwise.
     */
    public boolean isCarsToHeadDefined() {
        return carsToHead.isDefined();
    }

    /**
       Undefine the carsToHead property.
    */
    public void undefineCarsToHead() {
        carsToHead.undefine();
    }

    /**
       Get the carsToTail property.
       @return The carsToTail property.
     */
    public IntProperty getCarsToTailProperty() {
        return carsToTail;
    }

    /**
       Get the value of the carsToTail property.
       @return The value of the carsToTail property.
     */
    public int getCarsToTail() {
        return carsToTail.getValue();
    }

    /**
       Set the carsToTail property.
       @param carsToTail The new carsToTail.
    */
    public void setCarsToTail(int carsToTail) {
        this.carsToTail.setValue(carsToTail);
    }

    /**
       Find out if the carsToTail property has been defined.
       @return True if the carsToTail property has been defined, false otherwise.
     */
    public boolean isCarsToTailDefined() {
        return carsToTail.isDefined();
    }

    /**
       Undefine the carsToTail property.
    */
    public void undefineCarsToTail() {
        carsToTail.undefine();
    }

    /**
       Get the humansToHead property.
       @return The humansToHead property.
     */
    public IntProperty getHumansToHeadProperty() {
        return humansToHead;
    }

    /**
       Get the value of the humansToHead property.
       @return The value of the humansToHead property.
     */
    public int getHumansToHead() {
        return humansToHead.getValue();
    }

    /**
       Set the humansToHead property.
       @param humansToHead The new humansToHead.
    */
    public void setHumansToHead(int humansToHead) {
        this.humansToHead.setValue(humansToHead);
    }

    /**
       Find out if the humansToHead property has been defined.
       @return True if the humansToHead property has been defined, false otherwise.
     */
    public boolean isHumansToHeadDefined() {
        return humansToHead.isDefined();
    }

    /**
       Undefine the humansToHead property.
    */
    public void undefineHumansToHead() {
        humansToHead.undefine();
    }

    /**
       Get the humansToTail property.
       @return The humansToTail property.
     */
    public IntProperty getHumansToTailProperty() {
        return humansToTail;
    }

    /**
       Get the value of the humansToTail property.
       @return The value of the humansToTail property.
     */
    public int getHumansToTail() {
        return humansToTail.getValue();
    }

    /**
       Set the humansToTail property.
       @param humansToTail The new humansToTail.
    */
    public void setHumansToTail(int humansToTail) {
        this.humansToTail.setValue(humansToTail);
    }

    /**
       Find out if the humansToTail property has been defined.
       @return True if the humansToTail property has been defined, false otherwise.
     */
    public boolean isHumansToTailDefined() {
        return humansToTail.isDefined();
    }

    /**
       Undefine the humansToTail property.
    */
    public void undefineHumansToTail() {
        humansToTail.undefine();
    }

    /**
       Get the linesToHead property.
       @return The linesToHead property.
     */
    public IntProperty getLinesToHeadProperty() {
        return linesToHead;
    }

    /**
       Get the value of the linesToHead property.
       @return The value of the linesToHead property.
     */
    public int getLinesToHead() {
        return linesToHead.getValue();
    }

    /**
       Set the linesToHead property.
       @param linesToHead The new linesToHead.
    */
    public void setLinesToHead(int linesToHead) {
        this.linesToHead.setValue(linesToHead);
    }

    /**
       Find out if the linesToHead property has been defined.
       @return True if the linesToHead property has been defined, false otherwise.
     */
    public boolean isLinesToHeadDefined() {
        return linesToHead.isDefined();
    }

    /**
       Undefine the linesToHead property.
    */
    public void undefineLinesToHead() {
        linesToHead.undefine();
    }

    /**
       Get the linesToTail property.
       @return The linesToTail property.
     */
    public IntProperty getLinesToTailProperty() {
        return linesToTail;
    }

    /**
       Get the value of the linesToTail property.
       @return The value of the linesToTail property.
     */
    public int getLinesToTail() {
        return linesToTail.getValue();
    }

    /**
       Set the linesToTail property.
       @param linesToTail The new linesToTail.
    */
    public void setLinesToTail(int linesToTail) {
        this.linesToTail.setValue(linesToTail);
    }

    /**
       Find out if the linesToTail property has been defined.
       @return True if the linesToTail property has been defined, false otherwise.
     */
    public boolean isLinesToTailDefined() {
        return linesToTail.isDefined();
    }

    /**
       Undefine the linesToTail property.
    */
    public void undefineLinesToTail() {
        linesToTail.undefine();
    }

    /**
       Get the width property.
       @return The width property.
     */
    public IntProperty getWidthProperty() {
        return width;
    }

    /**
       Get the value of the width property.
       @return The value of the width property.
     */
    public int getWidth() {
        return width.getValue();
    }

    /**
       Set the width property.
       @param width The new width.
    */
    public void setWidth(int width) {
        this.width.setValue(width);
    }

    /**
       Find out if the width property has been defined.
       @return True if the width property has been defined, false otherwise.
     */
    public boolean isWidthDefined() {
        return width.isDefined();
    }

    /**
       Undefine the width property.
    */
    public void undefineWidth() {
        width.undefine();
    }

    /**
       Get the block property.
       @return The block property.
     */
    public IntProperty getBlockProperty() {
        return block;
    }

    /**
       Get the value of the block property.
       @return The value of the block property.
     */
    public int getBlock() {
        return block.getValue();
    }

    /**
       Set the block property.
       @param block The new block.
    */
    public void setBlock(int block) {
        this.block.setValue(block);
    }

    /**
       Find out if the block property has been defined.
       @return True if the block property has been defined, false otherwise.
     */
    public boolean isBlockDefined() {
        return block.isDefined();
    }

    /**
       Undefine the block property.
    */
    public void undefineBlock() {
        block.undefine();
    }

    /**
       Get the repair cost property.
       @return The repair cost property.
     */
    public IntProperty getRepairCostProperty() {
        return cost;
    }

    /**
       Get the value of the repair cost property.
       @return The value of the repair cost property.
     */
    public int getRepairCost() {
        return cost.getValue();
    }

    /**
       Set the repair cost property.
       @param newCost The new cost.
    */
    public void setRepairCost(int newCost) {
        this.cost.setValue(newCost);
    }

    /**
       Find out if the repair cost property has been defined.
       @return True if the repair cost property has been defined, false otherwise.
     */
    public boolean isRepairCostDefined() {
        return cost.isDefined();
    }

    /**
       Undefine the repair cost property.
    */
    public void undefineRepairCost() {
        cost.undefine();
    }

    /**
       Get the median strip property.
       @return The median strip property.
     */
    public BooleanProperty getMedianStripProperty() {
        return hasMedian;
    }

    /**
       Get the value of the median strip property.
       @return The value of the median strip property.
     */
    public boolean getMedianStrip() {
        return hasMedian.getValue();
    }

    /**
       Set the median strip property.
       @param newHasMedian The new hasMedian.
    */
    public void setMedianStrip(boolean newHasMedian) {
        this.hasMedian.setValue(newHasMedian);
    }

    /**
       Find out if the median strip property has been defined.
       @return True if the median strip property has been defined, false otherwise.
     */
    public boolean isMedianStripDefined() {
        return hasMedian.isDefined();
    }

    /**
       Undefine the median strip property.
    */
    public void undefineMedianStrip() {
        hasMedian.undefine();
    }

    /**
       Get the widthForWalkers property.
       @return The widthForWalkers property.
     */
    public IntProperty getWidthForWalkersProperty() {
        return widthForWalkers;
    }

    /**
       Get the value of the widthForWalkers property.
       @return The value of the widthForWalkers property.
     */
    public int getWidthForWalkers() {
        return widthForWalkers.getValue();
    }

    /**
       Set the widthForWalkers property.
       @param widthForWalkers The new widthForWalkers.
    */
    public void setWidthForWalkers(int widthForWalkers) {
        this.widthForWalkers.setValue(widthForWalkers);
    }

    /**
       Find out if the widthForWalkers property has been defined.
       @return True if the widthForWalkers property has been defined, false otherwise.
     */
    public boolean isWidthForWalkersDefined() {
        return widthForWalkers.isDefined();
    }

    /**
       Undefine the widthForWalkers property.
    */
    public void undefineWidthForWalkers() {
        widthForWalkers.undefine();
    }
}