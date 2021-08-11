package rescuecore2.standard.commands;

import static rescuecore2.Constants.COMMAND_URN_PREFIX;

/**
 * URNs for standard messages.
 */
public enum StandardCommandURN {

  /** Rest command. */
  AK_REST( COMMAND_URN_PREFIX + "rest" ),
  /** Move command. */
  AK_MOVE( COMMAND_URN_PREFIX + "move" ),
  /** Load command. */
  AK_LOAD( COMMAND_URN_PREFIX + "load" ),
  /** Unload command. */
  AK_UNLOAD( COMMAND_URN_PREFIX + "unload" ),
  /** Say command. */
  AK_SAY( COMMAND_URN_PREFIX + "say" ),
  /** Tell command. */
  AK_TELL( COMMAND_URN_PREFIX + "tell" ),
  /** Extinguish command. */
  AK_EXTINGUISH( COMMAND_URN_PREFIX + "extinguish" ),
  /** Rescue command. */
  AK_RESCUE( COMMAND_URN_PREFIX + "rescue" ),
  /** Clear command. */
  AK_CLEAR( COMMAND_URN_PREFIX + "clear" ),
  /** Clear-Area command. */
  AK_CLEAR_AREA( COMMAND_URN_PREFIX + "clear_area" ),
  /** Channel subscribe command. */
  AK_SUBSCRIBE( COMMAND_URN_PREFIX + "subscribe" ),
  /** Channel speak command. */
  AK_SPEAK( COMMAND_URN_PREFIX + "speak" );


  private final String urn;


  private StandardCommandURN( String urn ) {
    this.urn = urn;
  }


  @Override
  public String toString() {
    return urn;
  }


  /**
   * Convert a String to a StandardMessageURN.
   *
   * @param s
   *          The String to convert.
   * @return A StandardMessageURN.
   */
  public static StandardCommandURN fromString( String s ) {
    for ( StandardCommandURN next : StandardCommandURN.values() ) {
      if ( next.urn.equals( s ) ) {
        return next;
      }
    }
    throw new IllegalArgumentException( s );
  }
}