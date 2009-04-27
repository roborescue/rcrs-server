package rescuecore2.version0.entities.properties;

import rescuecore2.worldmodel.AbstractProperty;

/**
   Abstract base class for rescue properties.
*/
public abstract class RescueProperty extends AbstractProperty {
    private PropertyType type;

    /**
       Construct a property with a given type and assume that the value of this property is initially undefined.
       @param type The property type.
     */
    protected RescueProperty(PropertyType type) {
        this(type, false);
    }

    /**
       Construct a property with a given type and whether the value of this property is initially defined or not.
       @param type The property type.
       @param defined Whether the value is initially defined or not.
     */
    protected RescueProperty(PropertyType type, boolean defined) {
        super(type.getID(), defined);
	this.type = type;
    }

    /**
       Get the name of this property. The name is a unique identifier.
       @return The name of this property.
     */
    public String getName() {
	return type.getName();
    }
}