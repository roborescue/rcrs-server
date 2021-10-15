package rescuecore2.worldmodel.properties;

import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.writeInt32;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rescuecore2.URN;
import rescuecore2.messages.protobuf.RCRSProto.IntListProto;
import rescuecore2.messages.protobuf.RCRSProto.PropertyProto;
import rescuecore2.worldmodel.AbstractProperty;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;

/**
 * A property that refers to a list of entity IDs.
 */
public class EntityRefListProperty extends AbstractProperty {

	private List<EntityID> ids;

	/**
	 * Construct an EntityRefListProperty with no defined value.
	 *
	 * @param urn The urn of this property.
	 */
	public EntityRefListProperty(int urn) {
		super(urn);
		ids = new ArrayList<EntityID>();
	}

	/**
	 * Construct an EntityRefListProperty with no defined value.
	 *
	 * @param urn The urn of this property.
	 */
	public EntityRefListProperty(URN urn) {
		super(urn);
		ids = new ArrayList<EntityID>();
	}

	/**
	 * Construct an EntityRefListProperty with a defined value.
	 *
	 * @param urn The urn of this property.
	 * @param ids The initial value of the property.
	 */
	public EntityRefListProperty(int urn, List<EntityID> ids) {
		super(urn, true);
		this.ids = new ArrayList<EntityID>(ids);
	}

	/**
	 * Construct an EntityRefListProperty with a defined value.
	 *
	 * @param urn The urn of this property.
	 * @param ids The initial value of the property.
	 */
	public EntityRefListProperty(URN urn, List<EntityID> ids) {
		super(urn, true);
		this.ids = new ArrayList<EntityID>(ids);
	}

	/**
	 * EntityRefListProperty copy constructor.
	 *
	 * @param other The EntityRefListProperty to copy.
	 */
	public EntityRefListProperty(EntityRefListProperty other) {
		super(other);
		this.ids = new ArrayList<EntityID>(other.ids);
	}

	@Override
	public List<EntityID> getValue() {
		if (!isDefined()) {
			return null;
		}
		return Collections.unmodifiableList(ids);
	}

	/**
	 * Set the list of ids. Future calls to {@link #isDefined()} will return
	 * true.
	 *
	 * @param newIDs The new id list.
	 */
	public void setValue(List<EntityID> newIDs) {
		List<EntityID> old = new ArrayList<EntityID>(ids);
		ids.clear();
		ids.addAll(newIDs);
		setDefined();
		fireChange(old, Collections.unmodifiableList(ids));
	}

	/**
	 * Add a value to the list.
	 *
	 * @param id The id to add.
	 */
	public void addValue(EntityID id) {
		List<EntityID> old = new ArrayList<EntityID>(ids);
		ids.add(id);
		setDefined();
		fireChange(old, Collections.unmodifiableList(ids));
	}

	/**
	 * Remove a value from the list.
	 *
	 * @param id The id to remove.
	 */
	public void removeValue(EntityID id) {
		List<EntityID> old = new ArrayList<EntityID>(ids);
		ids.remove(id);

		if (ids.isEmpty())
			undefine();

		fireChange(old, Collections.unmodifiableList(ids));
	}

	/**
	 * Remove all entries from this list but keep it defined.
	 */
	public void clearValues() {
		List<EntityID> old = new ArrayList<EntityID>(ids);
		ids.clear();
		fireChange(old, Collections.unmodifiableList(ids));
	}

	@Override
	public void takeValue(Property p) {
		if (p instanceof EntityRefListProperty) {
			EntityRefListProperty e = (EntityRefListProperty) p;
			if (e.isDefined()) {
				setValue(e.getValue());
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
		writeInt32(ids.size(), out);
		for (EntityID next : ids) {
			writeInt32(next.getValue(), out);
		}
	}

	@Override
	public void read(InputStream in) throws IOException {
		int count = readInt32(in);
		List<EntityID> newIDs = new ArrayList<EntityID>(count);
		for (int i = 0; i < count; ++i) {
			newIDs.add(new EntityID(readInt32(in)));
		}
		setValue(newIDs);
	}

	/*
	 * @Override public String toString() { StringBuilder result = new
	 * StringBuilder(); result.append(getURN()); if (isDefined()) {
	 * result.append(" = {"); for (Iterator<EntityID> it = ids.iterator();
	 * it.hasNext();) { result.append(it.next()); if (it.hasNext()) {
	 * result.append(", "); } } result.append("}"); } else {
	 * result.append(" (undefined)"); } return result.toString(); }
	 */

	@Override
	public EntityRefListProperty copy() {
		return new EntityRefListProperty(this);
	}


	@Override
	public PropertyProto toPropertyProto() {
		PropertyProto.Builder builder = basePropertyProto();
		if (isDefined()) {
			IntListProto.Builder intListBuilder = IntListProto.newBuilder();
			for (EntityID next : ids) {
				intListBuilder.addValues(next.getValue());
			}
			builder.setIntList(intListBuilder);
		}
    	return builder.build();
	}
	@Override
	public void fromPropertyProto(PropertyProto proto) {
		if (!proto.getDefined())
			return;
		List<Integer> values = proto.getIntList().getValuesList();
		List<EntityID> newIDs = new ArrayList<EntityID>(values.size());
		for (Integer val : values) {
			newIDs.add(new EntityID(val));
		}
		setValue(newIDs);
	}
}
