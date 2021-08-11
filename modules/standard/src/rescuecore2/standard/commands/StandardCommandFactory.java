package rescuecore2.standard.commands;

import rescuecore2.log.Logger;
import rescuecore2.commands.Command;
import rescuecore2.registry.AbstractCommandFactory;

/**
 * A factory for standard messages.
 */
public final class StandardCommandFactory
    extends AbstractCommandFactory<StandardCommandURN> {

  /** Singleton instance. */
  public static final StandardCommandFactory INSTANCE = new StandardCommandFactory();


  /**
   * Singleton class: private constructor.
   */
  private StandardCommandFactory() {
    super( StandardCommandURN.class );
  }


  @Override
  public Command makeCommand( StandardCommandURN urn ) {
    switch ( urn ) {
      case AK_REST:
        return new AKRest();
      case AK_MOVE:
        return new AKMove();
      case AK_LOAD:
        return new AKLoad();
      case AK_UNLOAD:
        return new AKUnload();
      case AK_SAY:
        return new AKSay();
      case AK_TELL:
        return new AKTell();
      case AK_EXTINGUISH:
        return new AKExtinguish();
      case AK_RESCUE:
        return new AKRescue();
      case AK_CLEAR:
        return new AKClear();
      case AK_CLEAR_AREA:
        return new AKClearArea();
      case AK_SUBSCRIBE:
        return new AKSubscribe();
      case AK_SPEAK:
        return new AKSpeak();
      default:
        Logger.warn( "Unrecognised message urn: " + urn );
        return null;
    }
  }
}