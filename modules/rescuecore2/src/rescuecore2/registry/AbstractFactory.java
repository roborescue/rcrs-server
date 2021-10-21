package rescuecore2.registry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumSet;

import rescuecore2.URN;

public class AbstractFactory<T extends Enum<T> & URN> implements Factory {
	private Class<T> clazz;
	private Method fromString;
	private Method fromInt;

	/**
	 * Constructor for AbstractPropertyFactory.
	 *
	 * @param clazz The class of enum this factory uses.
	 */
	protected AbstractFactory(Class<T> clazz) {
		this.clazz = clazz;
		try {
			fromString = clazz.getDeclaredMethod("fromString", String.class);
		} catch (NoSuchMethodException e) {
		}
		try {
			fromInt = clazz.getDeclaredMethod("fromInt", int.class);
		} catch (NoSuchMethodException e2) {
		}
	}

	@SuppressWarnings("unchecked")
	public T getUrnEnum(int urn) {
		if (fromInt != null) {
			try {
				return (T) fromInt.invoke(null, urn);
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public T getUrnEnum(String urn) {
		if (fromString != null) {
			try {
				return (T) fromString.invoke(null, urn);
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			}
		}
		return null;
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

	/**
	 * Get an EnumSet containing known property URNs. Default implementation
	 * returns EnumSet.allOf(T).
	 *
	 * @return An EnumSet containing known property URNs.
	 */
	protected EnumSet<T> getKnownURNsEnum() {
		return EnumSet.allOf(clazz);
	}

	@Override
	public String getV1Equiv(int urnId) {
		T urnEnum = getUrnEnum(urnId);
		return urnEnum == null ? null : urnEnum.getUrnString();
	}

	@Override
	public String getPrettyName(int urnId) {
		T urnEnum = getUrnEnum(urnId);
		if (urnEnum != null)
			return urnEnum.name();
		return null;
	}

}
