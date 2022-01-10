package rescuecore2.registry;

import rescuecore2.URN;
import rescuecore2.worldmodel.Property;

/**
 * An abstract property factory with helper methods for defining URNs with
 * enums.
 *
 * @param <T> An enum type that defines the URNs this factory knows about.
 */
public abstract class AbstractPropertyFactory<T extends Enum<T> & URN> extends AbstractFactory<T>
    implements PropertyFactory {

  protected AbstractPropertyFactory(Class<T> clazz) {
    super(clazz);
  }

  @Override
  public Property makeProperty(int urn) {
    return makeProperty(getURNEnum(urn));
  }

  /**
   * Create a new Property.
   *
   * @param urn The enum urn of the property to create.
   * @return A new Property of the correct type.
   * @throws IllegalArgumentException If the urn is not recognised.
   */
  protected abstract Property makeProperty(T urn);
}