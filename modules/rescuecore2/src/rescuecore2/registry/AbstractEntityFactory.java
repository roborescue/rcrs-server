package rescuecore2.registry;

import rescuecore2.URN;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

/**
 * An abstract entity factory with helper methods for defining URNs with enums.
 *
 * @param <T> An enum type that defines the URNs this factory knows about.
 */
public abstract class AbstractEntityFactory<T extends Enum<T> & URN> extends AbstractFactory<T>
    implements EntityFactory {

  /**
   * Constructor for AbstractEntityFactory.
   *
   * @param clazz The class of enum this factory uses.
   */
  protected AbstractEntityFactory(Class<T> clazz) {
    super(clazz);
  }

  @Override
  public Entity makeEntity(int urn, EntityID id) {
    return makeEntity(getURNEnum(urn), id);
  }

  /**
   * Create a new Entity.
   *
   * @param urn The enum urn of the entity to create.
   * @param id  The id of the new entity.
   * @return A new Entity of the correct type.
   * @throws IllegalArgumentException If the urn is not recognised.
   */
  protected abstract Entity makeEntity(T urn, EntityID id);
}