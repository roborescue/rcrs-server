package rescuecore2.registry;

import java.util.EnumSet;
import java.io.InputStream;
import java.io.IOException;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import rescuecore2.URN;
import rescuecore2.messages.Message;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;

/**
 * An abstract message factory with helper methods for defining URNs with enums.
 * 
 * @param <T> An enum type that defines the URNs this factory knows about.
 */
public abstract class AbstractMessageComponentFactory<T extends Enum<T> & URN>
		implements MessageComponentFactory {
	private Class<T> clazz;
	private Method fromString;

	/**
	 * Constructor for AbstractMessageFactory.
	 * 
	 * @param clazz The class of enum this factory uses.
	 */
	protected AbstractMessageComponentFactory(Class<T> clazz) {
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
	public int[] getKnownMessageURNs() {
		EnumSet<T> set = getKnownMessageURNsEnum();
		int[] result = new int[set.size()];
		int i = 0;
		for (T next : set) {
			result[i++] = next.getUrn();
		}
		return result;
	}

	/**
	 * Get an EnumSet containing known message URNs. Default implementation
	 * returns EnumSet.allOf(T).
	 * 
	 * @return An EnumSet containing known message URNs.
	 */
	protected EnumSet<T> getKnownMessageURNsEnum() {
		return EnumSet.allOf(clazz);
	}

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
