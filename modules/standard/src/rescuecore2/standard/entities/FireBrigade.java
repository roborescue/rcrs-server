package rescuecore2.standard.entities;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.properties.IntProperty;

/**
   The FireBrigade object.
 */
public class FireBrigade extends Human {
    private IntProperty water;

    /**
       Construct a FireBrigade object with entirely undefined values.
       @param id The ID of this entity.
    */
    public FireBrigade(EntityID id) {
        super(id, StandardEntityType.FIRE_BRIGADE);
        water = new IntProperty(StandardPropertyType.WATER_QUANTITY);
        addProperties(water);
    }

    @Override
    protected Entity copyImpl() {
        return new FireBrigade(getID());
    }

    /**
       Get the water property.
       @return The water property.
     */
    public IntProperty getWaterProperty() {
        return water;
    }

    /**
       Get the amount of water this fire brigade is carrying.
       @return The water.
     */
    public int getWater() {
        return water.getValue();
    }

    /**
       Set the amount of water this fire brigade is carrying.
       @param water The new amount of water.
    */
    public void setWater(int water) {
        this.water.setValue(water);
    }

    /**
       Find out if the water property has been defined.
       @return True if the water property has been defined, false otherwise.
     */
    public boolean isWaterDefined() {
        return water.isDefined();
    }

    /**
       Undefine the water property.
    */
    public void undefineWater() {
        water.undefine();
    }
}