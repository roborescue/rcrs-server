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
public abstract class AbstractMessageFactory<T extends Enum<T> & URN>
		implements MessageFactory {
	private Class<T> clazz;
	private Method fromString;

	/**
	 * Constructor for AbstractMessageFactory.
	 * 
	 * @param clazz The class of enum this factory uses.
	 */
	protected AbstractMessageFactory(Class<T> clazz) {
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
	public Message makeMessage(int urn, InputStream data) throws IOException {
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
		return makeMessage(t, data);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Message makeMessage(int urn, MessageProto data) {
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
		return makeMessage(t, data);
	}

	/**
	 * Get an EnumSet containing known message URNs. Default implementation
	 * returns EnumSet.allOf(T).
	 * 
	 * @return An EnumSet containing known message URNs.
	 */
	protected EnumSet<T> getKnownURNsEnum() {
		return EnumSet.allOf(clazz);
	}

	/**
	 * Create a message based on its urn and populate it with data from a
	 * stream. If the urn is not recognised then return null.
	 * 
	 * @param urn  The urn of the message type to create.
	 * @param data An InputStream to read message data from.
	 * @return A new Message object, or null if the urn is not recognised.
	 * @throws IOException If there is a problem reading the stream.
	 */
	protected abstract Message makeMessage(T urn, InputStream data)
			throws IOException;

	protected abstract Message makeMessage(T urn, MessageProto data);
	
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
