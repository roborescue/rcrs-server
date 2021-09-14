package rescuecore2.standard.messages;

import rescuecore2.messages.protobuf.ControlMessageProto.MsgURN;

/**
 * URNs for standard messages.
 */
public enum StandardMessageURN {

//  /** Rest command. */
//  AK_REST( MESSAGE_URN_PREFIX + "rest" ),
//  /** Move command. */
//  AK_MOVE( MESSAGE_URN_PREFIX + "move" ),
//  /** Load command. */
//  AK_LOAD( MESSAGE_URN_PREFIX + "load" ),
//  /** Unload command. */
//  AK_UNLOAD( MESSAGE_URN_PREFIX + "unload" ),
//  /** Say command. */
//  AK_SAY( MESSAGE_URN_PREFIX + "say" ),
//  /** Tell command. */
//  AK_TELL( MESSAGE_URN_PREFIX + "tell" ),
//  /** Extinguish command. */
//  AK_EXTINGUISH( MESSAGE_URN_PREFIX + "extinguish" ),
//  /** Rescue command. */
//  AK_RESCUE( MESSAGE_URN_PREFIX + "rescue" ),
//  /** Clear command. */
//  AK_CLEAR( MESSAGE_URN_PREFIX + "clear" ),
//  /** Clear-Area command. */
//  AK_CLEAR_AREA( MESSAGE_URN_PREFIX + "clear_area" ),
//
//  /** Channel subscribe command. */
//  AK_SUBSCRIBE( MESSAGE_URN_PREFIX + "subscribe" ),
//
//  /** Channel speak command. */
//  AK_SPEAK( MESSAGE_URN_PREFIX + "speak" );

	AK_REST(MsgURN.AK_REST),
	AK_MOVE(MsgURN.AK_MOVE),
	AK_LOAD(MsgURN.AK_LOAD),
	AK_UNLOAD(MsgURN.AK_UNLOAD),
	AK_SAY(MsgURN.AK_SAY),
	AK_TELL(MsgURN.AK_TELL),
	AK_EXTINGUISH(MsgURN.AK_EXTINGUISH),
	AK_RESCUE(MsgURN.AK_RESCUE),
	AK_CLEAR(MsgURN.AK_CLEAR),
	AK_CLEAR_AREA(MsgURN.AK_CLEAR_AREA),
	AK_SUBSCRIBE(MsgURN.AK_SUBSCRIBE),
	AK_SPEAK(MsgURN.AK_SPEAK);

  private String urn;


  private StandardMessageURN( String urn ) {
    this.urn = urn;
  }


  StandardMessageURN(MsgURN urn) {
	  this.urn=urn.toString();
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
  public static StandardMessageURN fromString( String s ) {
    for ( StandardMessageURN next : StandardMessageURN.values() ) {
      if ( next.urn.equals( s ) ) {
        return next;
      }
    }
    throw new IllegalArgumentException( s );
  }
}
