package rescuecore2.registry;

import rescuecore2.URN;

/**
 * An abstract message factory with helper methods for defining URNs with enums.
 *
 * @param <T> An enum type that defines the URNs this factory knows about.
 */
public abstract class AbstractMessageComponentFactory<T extends Enum<T> & URN> extends AbstractFactory<T>
    implements MessageComponentFactory {

  /**
   * Constructor for AbstractMessageFactory.
   *
   * @param clazz The class of enum this factory uses.
   */
  protected AbstractMessageComponentFactory(Class<T> clazz) {
    super(clazz);
  }
}