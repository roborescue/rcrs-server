package rescuecore2.worldmodel.properties;

/**
   A boolean property.
 */
public class BooleanProperty extends AbstractProperty {
    private boolean value;

    /**
       Construct a BooleanProperty with no defined value.
       @param name The name of the property.
     */
    public BooleanProperty(String name) {
	super(name);
    }

    /**
       Construct a BooleanProperty with a defined value.
       @param name The name of the property.
       @param value The initial value of the property.
     */
    public BooleanProperty(String name, boolean value) {
	super(name, true);
	this.value = value;
    }

    /**
       Get the value of this property. If {@link #isDefined()} returns false then the result will be undefined.
       @return The value of this property, or an undefined result if the value has not been set.
       @see #isDefined()
     */
    public boolean getValue() {
	return value;
    }

    /**
       Set the value of this property. Future calls to {@link #isDefined()} will return true.
       @param value The new value.
     */
    public void setValue(boolean value) {
	this.value = value;
	setDefined(true);
    }

    /**
       Clear the value of this property. Future calls to {@link #isDefined()} will return false.
     */
    public void clearValue() {
	setDefined(false);
    }
}