package rescuecore2.registry;

import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.io.InputStream;
import java.io.IOException;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;
import rescuecore2.messages.Message;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;
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

	private static final ThreadLocal<Registry> CURRENT_REGISTRY=new InheritableThreadLocal<Registry>(){@Override public Registry initialValue(){return SYSTEM_REGISTRY;}};

	static {
		// Register the ControlMessageFactory
		SYSTEM_REGISTRY.registerMessageFactory(
				rescuecore2.messages.control.ControlMessageFactory.INSTANCE);
	}

	private final Map<Integer, EntityFactory> entityFactories;
	private final Map<Integer, PropertyFactory> propertyFactories;
	private final Map<Integer, MessageFactory> messageFactories;
	private final Map<String, Integer> v1_v2_map;
	private final Map<Integer, String> v2_v1_map;
	private final Map<Integer, String> urn_prettyName;

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
	 * @param name The name of this Registry.
	 */
	public Registry(String name) {
		this(name, SYSTEM_REGISTRY);
	}

	/**
	 * Create a new Registry with a particular parent.
	 * 
	 * @param parent The parent Registry.
	 */
	public Registry(Registry parent) {
		this(null, parent);
	}

	/**
	 * Create a new Registry with a particular name and parent.
	 * 
	 * @param name   The name of this Registry.
	 * @param parent The parent Registry.
	 */
	public Registry(String name, Registry parent) {
		this.name = name;
		this.parent = parent;
		entityFactories = new HashMap<Integer, EntityFactory>();
		propertyFactories = new HashMap<Integer, PropertyFactory>();
		messageFactories = new HashMap<Integer, MessageFactory>();
		v1_v2_map = new HashMap<String,Integer>();
		v2_v1_map = new HashMap<Integer,String>();
		urn_prettyName=new HashMap<Integer, String>();
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
	 * @param r The current Registry for this thread.
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
	 * @param factory The entity factory to register.
	 */
	public void registerEntityFactory(EntityFactory factory) {
		for (Integer urn : factory.getKnownEntityURNs()) {
			registerEntityFactory(urn, factory);
		}
	}

	/**
	 * Register an entity URN and assign an EntityFactory for constructing
	 * instances of this type.
	 * 
	 * @param urn     The urn to register.
	 * @param factory The factory that is responsible for constructing entity
	 *                instances of this type.
	 */
	public void registerEntityFactory(Integer urn, EntityFactory factory) {
		synchronized (entityFactories) {
			EntityFactory old = entityFactories.get(urn);
			if (old != null && old != factory) {
				Logger.warn(getName() + ": entity " + urn
						+ " is being clobbered by " + factory
						+ ". Old factory: " + old);
			}
			entityFactories.put(urn, factory);
			urn_prettyName.put(urn, factory.getPrettyName(urn));
			String v1urn = factory.getV1Equiv(urn);
			v1_v2_map.put(v1urn, urn);
			v2_v1_map.put(urn, v1urn);
		}
	}

	/**
	 * Register a property factory. This will register all property URNs that
	 * the factory knows about.
	 * 
	 * @param factory The property factory to register.
	 */
	public void registerPropertyFactory(PropertyFactory factory) {
		for (Integer urn : factory.getKnownPropertyURNs()) {
			registerPropertyFactory(urn, factory);
		}
	}

	/**
	 * Register a property URN and assign a PropertyFactory for constructing
	 * instances of this type.
	 * 
	 * @param urn     The urn to register.
	 * @param factory The factory that is responsible for constructing property
	 *                instances of this type.
	 */
	public void registerPropertyFactory(Integer urn, PropertyFactory factory) {
		synchronized (propertyFactories) {
			PropertyFactory old = propertyFactories.get(urn);
			if (old != null && old != factory) {
				Logger.warn(getName() + ": property " + urn
						+ " is being clobbered by " + factory
						+ ". Old factory: " + old);
			}
			propertyFactories.put(urn, factory);
			urn_prettyName.put(urn, factory.getPrettyName(urn));
			
			String v1urn = factory.getV1Equiv(urn);
			v1_v2_map.put(v1urn, urn);
			v2_v1_map.put(urn, v1urn);
		}
	}

	/**
	 * Register a message factory. This will register all message URNs that the
	 * factory knows about.
	 * 
	 * @param factory The message factory to register.
	 */
	public void registerMessageFactory(MessageFactory factory) {
		for (Integer urn : factory.getKnownMessageURNs()) {
			registerMessageFactory(urn, factory);
		}
	}

	/**
	 * Register a message URN and assign a MessageFactory for constructing
	 * instances of this type.
	 * 
	 * @param urn     The urn to register.
	 * @param factory The factory that is responsible for constructing message
	 *                instances of this type.
	 */
	public void registerMessageFactory(Integer urn, MessageFactory factory) {
		synchronized (messageFactories) {
			MessageFactory old = messageFactories.get(urn);
			if (old != null && old != factory) {
				Logger.warn(getName() + ": message " + urn
						+ " is being clobbered by " + factory
						+ ". Old factory: " + old);
			}
			messageFactories.put(urn, factory);
			urn_prettyName.put(urn, factory.getPrettyName(urn));
			String v1urn = factory.getV1Equiv(urn);
			v1_v2_map.put(v1urn, urn);
			v2_v1_map.put(urn, v1urn);
		}
	}

	/**
	 * Create an entity from a urn. If the urn is not recognised then return
	 * null. This method will delegate to the
	 * {@link #registerEntityFactory(EntityFactory) previously registered}
	 * EntityFactory.
	 * 
	 * @param urn The urn of the entity type to create.
	 * @param id  The EntityID of the Entity that will be created.
	 * @return A new Entity object, or null if the urn is not recognised.
	 */
	public Entity createEntity(int urn, EntityID id) {
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
	 * @param urn The urn of the property type to create.
	 * @return A new Property object, or null if the urn is not recognised.
	 */
	public Property createProperty(Integer urn) {
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
	 * @param urn  The urn of the message type to create.
	 * @param data An InputStream to read message data from.
	 * @return A new Message object, or null if the urn is not recognised.
	 * @throws IOException If there is a problem decoding the message.
	 */
	public Message createMessage(Integer urn, InputStream data)
			throws IOException {
		MessageFactory factory = getMessageFactory(urn);
		if (factory == null) {
			Logger.warn(getName() + ": Message " + urn + " not recognised.");
			return null;
		}
		return factory.makeMessage(urn, data);
	}

	public Message createMessage(Integer urn, MessageProto data) {
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
	 * @param urn The URN to look up.
	 * @return An EntityFactory, or null if the URN is not recognised.
	 */
	protected EntityFactory getEntityFactory(int urn) {
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
	 * @param urn The URN to look up.
	 * @return A PropertyFactory, or null if the URN is not recognised.
	 */
	protected PropertyFactory getPropertyFactory(Integer urn) {
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
	 * @param urn The URN to look up.
	 * @return A MessageFactory, or null if the URN is not recognised.
	 */
	protected MessageFactory getMessageFactory(Integer urn) {
		MessageFactory result = null;
		synchronized (messageFactories) {
			result = messageFactories.get(urn);
		}
		if (result == null && parent != null) {
			result = parent.getMessageFactory(urn);
		}
		return result;
	}


	public String toURN_V1(int urn) {
		return v2_v1_map.get(urn);
	}

	public int toURN_V2(String urn) {
		return v1_v2_map.get(urn);
	}

	public String toPrettyName(int urn) {
		return urn_prettyName.get(urn);
	}
	
	public JSONObject toJSON() {
		JSONObject json=new JSONObject();
		JSONObject messages=new JSONObject();
		JSONObject entities=new JSONObject();
		JSONObject properties=new JSONObject();
		JSONObject inverse=new JSONObject();
		for (Entry<Integer, MessageFactory> entry : messageFactories.entrySet()) {
			messages.put(entry.getValue().getPrettyName(entry.getKey()), entry.getKey());
			inverse.put(entry.getKey()+"",entry.getValue().getPrettyName(entry.getKey()));
		}
		for (Entry<Integer, EntityFactory> entry : entityFactories.entrySet()) {
			entities.put(entry.getValue().getPrettyName(entry.getKey()), entry.getKey());
			inverse.put(entry.getKey()+"",entry.getValue().getPrettyName(entry.getKey()));
		}
		
		for (Entry<Integer, PropertyFactory> entry : propertyFactories.entrySet()) { 
			properties.put(entry.getValue().getPrettyName(entry.getKey()), entry.getKey());
			inverse.put(entry.getKey()+"",entry.getValue().getPrettyName(entry.getKey()));
		}
		
		json.put("Messages", messages);
		json.put("Entities", entities);
		json.put("Properties", properties);
		return json;
	}
	
	public String toPython() {
		String out="";
		out+="\n#### Messages ####\n";
		ArrayList<Integer> msgkeys = new ArrayList<Integer>(messageFactories.keySet());
		Collections.sort(msgkeys);
		ArrayList<Integer> entitykeys = new ArrayList<Integer>(entityFactories.keySet());
		Collections.sort(entitykeys);
		ArrayList<Integer> propkeys = new ArrayList<Integer>(propertyFactories.keySet());
		Collections.sort(propkeys );
		
		for (Integer urn : msgkeys) {
			String prettyName = urn_prettyName.get(urn);
			out+=prettyName+"="+urn+"\n";
		}
		out+="\n#### Entities ####\n";
		
		for (Integer urn : entitykeys) {
			String prettyName = urn_prettyName.get(urn);
			out+=prettyName+"="+urn+"\n";
		}
		out+="\n#### Properties ####\n";
		
		for (Integer urn : propkeys ) {
			String prettyName = urn_prettyName.get(urn);
			out+=prettyName+"="+urn+"\n";
		}
		
		out+="\n#### PrettyName ####\n";
		out+="MAP={\n";
		out+="\n#### Messages ####\n";
		for (Integer urn : msgkeys) {
			String prettyName = urn_prettyName.get(urn);
			out+="\t"+urn+":'"+prettyName+"',\n";
		}
		out+="\n#### Entities ####\n";
		for (Integer urn : entitykeys) {
			String prettyName = urn_prettyName.get(urn);
			out+="\t"+urn+":'"+prettyName+"',\n";
		}
		out+="\n#### Properties ####\n";
		for (Integer urn : propkeys ) {
			String prettyName = urn_prettyName.get(urn);
			out+="\t"+urn+":'"+prettyName+"',\n";
		}
		out+="}";
		return out;
	}
	
}
