package rescuecore2.messages.control;

import rescuecore2.registry.AbstractMessageComponentFactory;

/**
 * A factory for control messages.
 */
public final class ControlMessageComponentFactory extends AbstractMessageComponentFactory<ControlMessageComponentURN> {
  /** Singleton instance. */
  public static final ControlMessageComponentFactory INSTANCE = new ControlMessageComponentFactory();

  private ControlMessageComponentFactory() {
    super(ControlMessageComponentURN.class);
  }
}