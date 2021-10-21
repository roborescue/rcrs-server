package rescuecore2.registry;

import java.util.EnumSet;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import rescuecore2.URN;
import rescuecore2.worldmodel.Property;

/**
 * An abstract property factory with helper methods for defining URNs with
 * enums.
 * 
 * @param <T> An enum type that defines the URNs this factory knows about.
 */
public abstract class AbstractPropertyFactory<T extends Enum<T> & URN>
		implements PropertyFactory {
	private Class<T> clazz;
	private Method fromString;

	/**
	 * Constructor for AbstractPropertyFactory.
	 * 
	 * @param clazz The class of enum this factory uses.
	 */
	protected AbstractPropertyFactory(Class<T> clazz) {
		this.clazz = clazz;
		try {
			fromString = clazz.getDeclaredMethod("fromString", String.class);
		} catch (NoSuchMethodException e) {
			try {
				fromString = clazz.getDeclaredMethod("fromInt", int.class);
			} catch (NoSuchMethodException e2) {
				fromString = null;
			}
		}
	}

	@Override
	public int[] getKnownURNs() {
		EnumSet<T> set = getKnownURNsEnum();
		int[] result = new int[set.size()];
		int i = 0;
		for (T next : set) {
			result[i++] = next.getUrn();
		}
		return result;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Property makeProperty(int urn) {
		T t = null;
		if (fromString != null) {
			try {
				t = (T) fromString.invoke(null, urn);
			} catch (IllegalAccessException e) {
				t = null;
			} catch (InvocationTargetException e) {
				t = null;
			}
		}
//        if (t == null) {
//            t = Enum.valueOf(clazz, urn);
//        }
		return makeProperty(t);
	}

	/**
	 * Get an EnumSet containing known property URNs. Default implementation
	 * returns EnumSet.allOf(T).
	 * 
	 * @return An EnumSet containing known property URNs.
	 */
	protected EnumSet<T> getKnownURNsEnum() {
		return EnumSet.allOf(clazz);
	}

	/**
	 * Create a new Property.
	 * 
	 * @param urn The enum urn of the property to create.
	 * @return A new Property of the correct type.
	 * @throws IllegalArgumentException If the urn is not recognised.
	 */
	protected abstract Property makeProperty(T urn);
	
	@Override
	public String getPrettyName(int urn) {
		if (fromString != null) {
			try {
				@SuppressWarnings("unchecked")
				T t = (T) fromString.invoke(null, urn);
				if(t!=null)
					return t.name();
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			}
		}
		return null;
	}
}
