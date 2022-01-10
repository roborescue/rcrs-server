package rescuecore2.registry;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import rescuecore2.log.Logger;
import rescuecore2.messages.Message;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;

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
    SYSTEM_REGISTRY.registerFactory(rescuecore2.messages.control.ControlMessageFactory.INSTANCE);
    SYSTEM_REGISTRY.registerFactory(rescuecore2.messages.control.ControlMessageComponentFactory.INSTANCE);
  }

  private final Map<Integer, EntityFactory> entityFactories;
  private final Map<Integer, PropertyFactory> propertyFactories;
  private final Map<Integer, MessageFactory> messageFactories;
  private final Map<Integer, MessageComponentFactory> messageComponentFactories;
  private final Map<String, Integer> urn_map_str_id;
  private final Map<Integer, String> urn_map_id_str;
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
   * Create a new Registry with a particular name that uses the system registry as
   * a parent.
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
    messageComponentFactories = new HashMap<Integer, MessageComponentFactory>();
    urn_map_str_id = new HashMap<String, Integer>();
    urn_map_id_str = new HashMap<Integer, String>();
    urn_prettyName = new HashMap<Integer, String>();
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
   * Register a factory. This will register all message URNs that the factory
   * knows about.
   *
   * @param factory The factory to register.
   */
  public void registerFactory(Factory factory) {
    for (int urn : factory.getKnownURNs()) {
      registerFactory(urn, factory);
    }
  }

  /**
   * Register a URN and assign a Factory for constructing instances of this type.
   *
   * @param urn     The urn to register.
   * @param factory The factory that is responsible for constructing message
   *                instances of this type.
   */
  public void registerFactory(int urn, Factory factory) {
    if (factory instanceof MessageComponentFactory)
      registerFactoryInternal(urn, (MessageComponentFactory) factory, messageComponentFactories);
    else if (factory instanceof MessageFactory)
      registerFactoryInternal(urn, (MessageFactory) factory, messageFactories);
    // registerMessageFactory(urn,(MessageFactory) factory);
    else if (factory instanceof PropertyFactory)
      registerFactoryInternal(urn, (PropertyFactory) factory, propertyFactories);
    // registerPropertyFactory(urn,(PropertyFactory) factory);
    else if (factory instanceof EntityFactory)
      registerFactoryInternal(urn, (EntityFactory) factory, entityFactories);
    // registerEntityFactory(urn,(EntityFactory) factory);
    else
      Logger.error(getName() + ":unknown factory! " + factory.getClass().getName());

  }

  private <T extends Factory> void registerFactoryInternal(int urnId, T factory, Map<Integer, T> target) {
    synchronized (target) {
      T old = target.get(urnId);
      if (old != null && old != factory) {
        Logger.warn(getName() + ": " + urnId + " (" + old.getPrettyName(urnId) + ":" + old.getURNStr(urnId) + ")"
            + " is being clobbered by " + factory + ". Old factory: " + old);
      }
      target.put(urnId, factory);

      this.urn_prettyName.put(urnId, factory.getPrettyName(urnId));

      String urn_str = factory.getURNStr(urnId);
      this.urn_map_str_id.put(urn_str, urnId);
      this.urn_map_id_str.put(urnId, urn_str);
    }

  }

  /**
   * Create an entity from a urn. If the urn is not recognised then return null.
   * This method will delegate to the {@link #registerEntityFactory(EntityFactory)
   * previously registered} EntityFactory.
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
   * Create a property from a urn. If the urn is not recognised then return null.
   * This method will delegate to the
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
   * Create a message from a urn. If the urn is not recognised then return null.
   * This method will delegate to the
   * {@link #registerMessageFactory(MessageFactory) previously registered}
   * MessageFactory.
   *
   * @param urn  The urn of the message type to create.
   * @param data An InputStream to read message data from.
   * @return A new Message object, or null if the urn is not recognised.
   * @throws IOException If there is a problem decoding the message.
   */
  public Message createMessage(Integer urn, InputStream data) throws IOException {
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

  public String toURN_Str(int urnId) {
    return this.urn_map_id_str.get(urnId);
  }

  public int toURN_Id(String urnStr) {
    return this.urn_map_str_id.get(urnStr);
  }

  public String toPrettyName(int urn) {
    return urn_prettyName.get(urn);
  }
}