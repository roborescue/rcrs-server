package rescuecore2.worldmodel.properties;

/**
   A single integer property.
 */
public class IntProperty extends AbstractProperty {
    private int value;

    /**
       Construct an IntProperty with no defined value.
       @param name The name of the property.
     */
    public IntProperty(String name) {
	super(name);
    }

    /**
       Construct an IntProperty with a defined value.
       @param name The name of the property.
       @param value The initial value of the property.
     */
    public IntProperty(String name, int value) {
	super(name, true);
	this.value = value;
    }

    /**
       Get the value of this property. If {@link #isDefined()} returns false then the result will be undefined.
       @return The value of this property, or an undefined result if the value has not been set.
       @see #isDefined()
     */
    public int getValue() {
	return value;
    }

    /**
       Set the value of this property. Future calls to {@link #isDefined()} will return true.
       @param value The new value.
     */
    public void setValue(int value) {
	this.value = value;
	setDefined(true);
    }

    /**
       Clear the value of this property. Future calls to {@link #isDefined()} will return false.
     */
    public void clearValue() {
	value = 0;
	setDefined(false);
    }
}