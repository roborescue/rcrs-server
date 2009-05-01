package rescuecore2.worldmodel.properties;

import java.util.List;
import java.util.ArrayList;

/**
   An integer-array property.
 */
public class IntArrayProperty extends AbstractProperty {
    /** Implement as a list to allow for growth */
    private List<Integer> data;

    /**
       Construct an IntArrayProperty with no defined value.
       @param name The name of the property.
     */
    public IntArrayProperty(String name) {
	super(name);
	data = null;
    }

    /**
       Construct an IntArrayProperty with a defined value.
       @param name The name of the property.
       @param values The initial values of the property.
     */
    public IntArrayProperty(String name, int[] values) {
	super(name, true);
	data = new ArrayList<Integer>(values.length);
	for (Integer next : values) {
	    data.add(next);
	}
    }

    /**
       Get the values of this property. If {@link #isDefined()} returns false then the result will be undefined.
       @return The values of this property, or an undefined result if the values have not been set.
       @see #isDefined()
     */
    public int[] getValues() {
	// null data means undefined
	if (data == null) {
	    return null;
	}
	Integer[] result = new Integer[data.size()];
	data.toArray(result);
	int[] out = new int[result.length];
	for (int i=0; i < out.length; ++i) {
	    out[i] = result[i].intValue();
	}
	return out;
    }

    /**
       Set the value of this property. Future calls to {@link #isDefined()} will return true.
       @param value The new value.
     */
    public void setValues(int[] values) {
	this.data = new ArrayList<Integer>(values.length);
	for (Integer next : values) {
	    data.add(next);
	}
	setDefined(true);
    }

    /**
       Add a value to the array.
       @param i The value to add.
     */
    public void push(int i) {
	if (data == null) {
	    data = new ArrayList<Integer>();
	    setDefined(true);
	}
	data.add(i);
    }

    /**
       Clear the value of this property. Future calls to {@link #isDefined()} will return false.
     */
    public void clearValue() {
	data = null;
	setDefined(false);
    }
}