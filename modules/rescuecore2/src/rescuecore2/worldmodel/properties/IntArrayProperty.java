package rescuecore2.worldmodel.properties;

import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.writeInt32;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import rescuecore2.worldmodel.Property;
import rescuecore2.URN;
import rescuecore2.messages.protobuf.RCRSProto.IntListProto;
import rescuecore2.messages.protobuf.RCRSProto.PropertyProto;
import rescuecore2.worldmodel.AbstractProperty;

/**
 * An integer-array property.
 */
public class IntArrayProperty extends AbstractProperty {
	/** Implement as a list to allow for growth. */
	private List<Integer> data;

	/**
	 * Construct an IntArrayProperty with no defined value.
	 * 
	 * @param urn The urn of this property.
	 */
	public IntArrayProperty(int urn) {
		super(urn);
		data = new ArrayList<Integer>();
	}

	/**
	 * Construct an IntArrayProperty with no defined value.
	 * 
	 * @param urn The urn of this property.
	 */
	public IntArrayProperty(URN urn) {
		super(urn);
		data = new ArrayList<Integer>();
	}

	/**
	 * Construct an IntArrayProperty with a defined value.
	 * 
	 * @param urn    The urn of this property.
	 * @param values The initial values of the property.
	 */
	public IntArrayProperty(int urn, int[] values) {
		super(urn, true);
		data = new ArrayList<Integer>(values.length);
		for (Integer next : values) {
			data.add(next);
		}
	}

	/**
	 * Construct an IntArrayProperty with a defined value.
	 * 
	 * @param urn    The urn of this property.
	 * @param values The initial values of the property.
	 */
	public IntArrayProperty(URN urn, int[] values) {
		super(urn, true);
		data = new ArrayList<Integer>(values.length);
		for (Integer next : values) {
			data.add(next);
		}
	}

	/**
	 * IntArrayProperty copy constructor.
	 * 
	 * @param other The IntArrayProperty to copy.
	 */
	public IntArrayProperty(IntArrayProperty other) {
		super(other);
		this.data = new ArrayList<Integer>(other.data);
	}

	@Override
	public int[] getValue() {
		if (!isDefined()) {
			return null;
		}
		Integer[] result = new Integer[data.size()];
		data.toArray(result);
		int[] out = new int[result.length];
		for (int i = 0; i < out.length; ++i) {
			out[i] = result[i].intValue();
		}
		return out;
	}

	/**
	 * Set the value of this property. Future calls to {@link #isDefined()} will
	 * return true.
	 * 
	 * @param values The new values.
	 */
	public void setValue(int[] values) {
		int[] old = getValue();
		this.data = new ArrayList<Integer>(values.length);
		for (Integer next : values) {
			data.add(next);
		}
		setDefined();
		fireChange(old, getValue());
	}

	/**
	 * Add a value to the array.
	 * 
	 * @param i The value to add.
	 */
	public void push(int i) {
		int[] old = getValue();
		setDefined();
		data.add(i);
		fireChange(old, getValue());
	}

	@Override
	public void takeValue(Property p) {
		if (p instanceof IntArrayProperty) {
			IntArrayProperty i = (IntArrayProperty) p;
			if (i.isDefined()) {
				setValue(i.getValue());
			} else {
				undefine();
			}
		} else {
			throw new IllegalArgumentException(
					this + " cannot take value from " + p);
		}
	}

	@Override
	public void write(OutputStream out) throws IOException {
		writeInt32(data.size(), out);
		for (Integer next : data) {
			writeInt32(next.intValue(), out);
		}
	}

	@Override
	public void read(InputStream in) throws IOException {
		int size = readInt32(in);
		int[] result = new int[size];
		for (int i = 0; i < size; ++i) {
			result[i] = readInt32(in);
		}
		setValue(result);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(getURN());
		if (isDefined()) {
			result.append(" = {");
			for (Iterator<Integer> it = data.iterator(); it.hasNext();) {
				result.append(it.next());
				if (it.hasNext()) {
					result.append(", ");
				}
			}
			result.append("}");
		} else {
			result.append(" (undefined)");
		}
		return result.toString();
	}

	@Override
	public IntArrayProperty copy() {
		return new IntArrayProperty(this);
	}

	
	@Override
	public PropertyProto toPropertyProto() {
		PropertyProto.Builder builder = basePropertyProto();
		if (isDefined()) {
			builder
			.setIntList(IntListProto.newBuilder().addAllValues(data));
		}
    	return builder.build();
	}

	@Override
	public void fromPropertyProto(PropertyProto proto) {
		if (!proto.getDefined())
			return;
		List<Integer> list = proto.getIntList().getValuesList();
		int[] result = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			result[i] = list.get(i);
		}
		setValue(result);
	}
}
