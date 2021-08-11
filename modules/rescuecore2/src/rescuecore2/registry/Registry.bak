package rescuecore2.registry;

import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;
import java.io.IOException;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;
import rescuecore2.messages.Message;
import rescuecore2.log.Logger;

/**
 * A class for managing the different types of entities, properties, messages
 * and their associated factories.
 */
public final class Registry {
	/**
	 * The system-(or at least Classloader)-wide Registry.
	 */
	public static final Registry SYSTEM_REGISTRY = new Registry("System", null);

	private static final ThreadLocal<Registry> CURRENT_REGISTRY = new InheritableThreadLocal<Registry>() {
		@Override
		public Registry initialValue() {
			return SYSTEM_REGISTRY;
		}
	};

	static {
		// Register the ControlMessageFactory
		SYSTEM_REGISTRY
				.registerMessageFactory(rescuecore2.messages.control.ControlMessageFactory.INSTANCE);
	}

	private final Map<String, EntityFactory> entityFactories;
	private final Map<String, PropertyFactory> propertyFactories;
	private final Map<String, MessageFactory> messageFactories;

	private final Registry parent;
	private final String name;

	/**
	 * Create a new Registry that uses the system registry as a parent.
	 */
	public Registry() {
		this(null, SYSTEM_REGISTRY);
	}

	/**
	 * Create a new Registry with a particular name that uses the system
	 * registry as a parent.
	 * 
	 * @param name
	 *            The name of this Registry.
	 */
	public Registry(String name) {
		this(name, SYSTEM_REGISTRY);
	}

	/**
	 * Create a new Registry with a particular parent.
	 * 
	 * @param parent
	 *            The parent Registry.
	 */
	public Registry(Registry parent) {
		this(null, parent);
	}

	/**
	 * Create a new Registry with a particular name and parent.
	 * 
	 * @param name
	 *            The name of this Registry.
	 * @param parent
	 *            The parent Registry.
	 */
	public Registry(String name, Registry parent) {
		this.name = name;
		this.parent = parent;
		entityFactories = new HashMap<String, EntityFactory>();
		propertyFactories = new HashMap<String, PropertyFactory>();
		messageFactories = new HashMap<String, MessageFactory>();
	}

	/**
	 * Get the current Registry for this thread.
	 * 
	 * @return The current Registry for this thread.
	 */
	public static Registry getCurrentRegistry() {
		return CURRENT_REGISTRY.get();
	}

	/**
	 * Set the current Registry for this thread.
	 * 
	 * @param r
	 *            The current Registry for this thread.
	 */
	public static void setCurrentRegistry(Registry r) {
		CURRENT_REGISTRY.set(r);
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Get the name of this registry.
	 * 
	 * @return The name of this registry.
	 */
	public String getName() {
		if (name == null) {
			return super.toString();
		}
		return name;
	}

	/**
	 * Register an entity factory. This will register all entity URNs that the
	 * factory knows about.
	 * 
	 * @param factory
	 *            The entity factory to register.
	 */
	public void registerEntityFactory(EntityFactory factory) {
		for (String urn : factory.getKnownEntityURNs()) {
			registerEntityFactory(urn, factory);
		}
	}

	/**
	 * Register an entity URN and assign an EntityFactory for constructing
	 * instances of this type.
	 * 
	 * @param urn
	 *            The urn to register.
	 * @param factory
	 *            The factory that is responsible for constructing entity
	 *            instances of this type.
	 */
	public void registerEntityFactory(String urn, EntityFactory factory) {
		synchronized (entityFactories) {
			EntityFactory old = entityFactories.get(urn);
			if (old != null && old != factory) {
				Logger.warn(getName() + ": entity " + urn
						+ " is being clobbered by " + factory
						+ ". Old factory: " + old);
			}
			entityFactories.put(urn, factory);
		}
	}

	/**
	 * Register a property factory. This will register all property URNs that
	 * the factory knows about.
	 * 
	 * @param factory
	 *            The property factory to register.
	 */
	public void registerPropertyFactory(PropertyFactory factory) {
		for (String urn : factory.getKnownPropertyURNs()) {
			registerPropertyFactory(urn, factory);
		}
	}

	/**
	 * Register a property URN and assign a PropertyFactory for constructing
	 * instances of this type.
	 * 
	 * @param urn
	 *            The urn to register.
	 * @param factory
	 *            The factory that is responsible for constructing property
	 *            instances of this type.
	 */
	public void registerPropertyFactory(String urn, PropertyFactory factory) {
		synchronized (propertyFactories) {
			PropertyFactory old = propertyFactories.get(urn);
			if (old != null && old != factory) {
				Logger.warn(getName() + ": property " + urn
						+ " is being clobbered by " + factory
						+ ". Old factory: " + old);
			}
			propertyFactories.put(urn, factory);
		}
	}

	/**
	 * Register a message factory. This will register all message URNs that the
	 * factory knows about.
	 * 
	 * @param factory
	 *            The message factory to register.
	 */
	public void registerMessageFactory(MessageFactory factory) {
		for (String urn : factory.getKnownMessageURNs()) {
			registerMessageFactory(urn, factory);
		}
	}

	/**
	 * Register a message URN and assign a MessageFactory for constructing
	 * instances of this type.
	 * 
	 * @param urn
	 *            The urn to register.
	 * @param factory
	 *            The factory that is responsible for constructing message
	 *            instances of this type.
	 */
	public void registerMessageFactory(String urn, MessageFactory factory) {
		synchronized (messageFactories) {
			MessageFactory old = messageFactories.get(urn);
			if (old != null && old != factory) {
				Logger.warn(getName() + ": message " + urn
						+ " is being clobbered by " + factory
						+ ". Old factory: " + old);
			}
			messageFactories.put(urn, factory);
		}
	}

	/**
	 * Create an entity from a urn. If the urn is not recognised then return
	 * null. This method will delegate to the
	 * {@link #registerEntityFactory(EntityFactory) previously registered}
	 * EntityFactory.
	 * 
	 * @param urn
	 *            The urn of the entity type to create.
	 * @param id
	 *            The EntityID of the Entity that will be created.
	 * @return A new Entity object, or null if the urn is not recognised.
	 */
	public Entity createEntity(String urn, EntityID id) {
		EntityFactory factory = getEntityFactory(urn);
		if (factory == null) {
			Logger.warn(getName() + ": Entity " + urn + " not recognised.");
			return null;
		}
		return factory.makeEntity(urn, id);
	}

	/**
	 * Create a property from a urn. If the urn is not recognised then return
	 * null. This method will delegate to the
	 * {@link #registerPropertyFactory(PropertyFactory) previously registered}
	 * PropertyFactory.
	 * 
	 * @param urn
	 *            The urn of the property type to create.
	 * @return A new Property object, or null if the urn is not recognised.
	 */
	public Property createProperty(String urn) {
		PropertyFactory factory = getPropertyFactory(urn);
		if (factory == null) {
			Logger.warn(getName() + ": Property " + urn + " not recognised.");
			return null;
		}
		return factory.makeProperty(urn);
	}

	/**
	 * Create a message from a urn. If the urn is not recognised then return
	 * null. This method will delegate to the
	 * {@link #registerMessageFactory(MessageFactory) previously registered}
	 * MessageFactory.
	 * 
	 * @param urn
	 *            The urn of the message type to create.
	 * @param data
	 *            An InputStream to read message data from.
	 * @return A new Message object, or null if the urn is not recognised.
	 * @throws IOException
	 *             If there is a problem decoding the message.
	 */
	public Message createMessage(String urn, InputStream data)
			throws IOException {
		MessageFactory factory = getMessageFactory(urn);
		if (factory == null) {
			Logger.warn(getName() + ": Message " + urn + " not recognised.");
			return null;
		}
		return factory.makeMessage(urn, data);
	}

	/**
	 * Get the entity factory for a URN, delegating to the parent if required.
	 * 
	 * @param urn
	 *            The URN to look up.
	 * @return An EntityFactory, or null if the URN is not recognised.
	 */
	protected EntityFactory getEntityFactory(String urn) {
		EntityFactory result = null;
		synchronized (entityFactories) {
			result = entityFactories.get(urn);
		}
		if (result == null && parent != null) {
			result = parent.getEntityFactory(urn);
		}
		return result;
	}

	/**
	 * Get the property factory for a URN, delegating to the parent if required.
	 * 
	 * @param urn
	 *            The URN to look up.
	 * @return A PropertyFactory, or null if the URN is not recognised.
	 */
	protected PropertyFactory getPropertyFactory(String urn) {
		PropertyFactory result = null;
		synchronized (propertyFactories) {
			result = propertyFactories.get(urn);
		}
		if (result == null && parent != null) {
			result = parent.getPropertyFactory(urn);
		}
		return result;
	}

	/**
	 * Get the message factory for a URN, delegating to the parent if required.
	 * 
	 * @param urn
	 *            The URN to look up.
	 * @return A MessageFactory, or null if the URN is not recognised.
	 */
	protected MessageFactory getMessageFactory(String urn) {
		MessageFactory result = null;
		synchronized (messageFactories) {
			result = messageFactories.get(urn);
		}
		if (result == null && parent != null) {
			result = parent.getMessageFactory(urn);
		}
		return result;
	}
}
