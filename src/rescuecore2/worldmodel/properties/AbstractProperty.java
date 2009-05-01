package rescuecore2.worldmodel.properties;

import rescuecore2.worldmodel.Property;

/**
   Abstract base class for Property implementations.
*/
public abstract class AbstractProperty implements Property {
    private boolean defined;
    private String name;

    /**
       Construct a property with a given name and assume that the value of this property is initially undefined.
       @param name The name of the property.
     */
    protected AbstractProperty(String name) {
        this(name, false);
    }

    /**
       Construct a property with a given name and whether the value of this property is initially defined or not.
       @param name The name of the property.
       @param defined Whether the value is initially defined or not.
     */
    protected AbstractProperty(String name, boolean defined) {
	this.name = name;
        this.defined = defined;
    }

    /**
       Set whether the value is defined or not.
       @param b The new defined status.
     */
    protected void setDefined(boolean b) {
        defined = b;
    }

    @Override
    public boolean isDefined() {
        return defined;
    }

    @Override
    public String getName() {
	return name;
    }
}