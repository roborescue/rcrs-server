package rescuecore2.standard.entities;


import rescuecore2.messages.protobuf.ControlMessageProto.EntityURN;

/**
 * URNs for standard entities.
 */
public enum StandardEntityURN {

//  WORLD( ENTITY_URN_PREFIX + "world" ),
//  ROAD( ENTITY_URN_PREFIX + "road" ),
//  BLOCKADE( ENTITY_URN_PREFIX + "blockade" ),
//  BUILDING( ENTITY_URN_PREFIX + "building" ),
//  REFUGE( ENTITY_URN_PREFIX + "refuge" ),
//  HYDRANT( ENTITY_URN_PREFIX + "hydrant" ),
//  GAS_STATION( ENTITY_URN_PREFIX + "gasstation" ),
//  FIRE_STATION( ENTITY_URN_PREFIX + "firestation" ),
//  AMBULANCE_CENTRE( ENTITY_URN_PREFIX + "ambulancecentre" ),
//  POLICE_OFFICE( ENTITY_URN_PREFIX + "policeoffice" ),
//  CIVILIAN( ENTITY_URN_PREFIX + "civilian" ),
//  FIRE_BRIGADE( ENTITY_URN_PREFIX + "firebrigade" ),
//  AMBULANCE_TEAM( ENTITY_URN_PREFIX + "ambulanceteam" ),
//  POLICE_FORCE( ENTITY_URN_PREFIX + "policeforce" );

	WORLD(EntityURN.WORLD),
	ROAD(EntityURN.ROAD),
	BLOCKADE(EntityURN.BLOCKADE),
	BUILDING(EntityURN.BUILDING),
	REFUGE(EntityURN.REFUGE),
	HYDRANT(EntityURN.HYDRANT),
	GAS_STATION(EntityURN.GAS_STATION),
	FIRE_STATION(EntityURN.FIRE_STATION),
	AMBULANCE_CENTRE(EntityURN.AMBULANCE_CENTRE),
	POLICE_OFFICE(EntityURN.POLICE_OFFICE),
	CIVILIAN(EntityURN.CIVILIAN),
	FIRE_BRIGADE(EntityURN.FIRE_BRIGADE),
	AMBULANCE_TEAM(EntityURN.AMBULANCE_TEAM),
	POLICE_FORCE(EntityURN.POLICE_FORCE);

  private String urn;


  private StandardEntityURN( String urn ) {
    this.urn = urn;
  }
  private StandardEntityURN( EntityURN urn ) {
	    this.urn = urn.toString();
  }

  @Override
  public String toString() {
    return urn;
  }


  /**
   * Convert a String to a StandardEntityURN.
   *
   * @param s
   *          The String to convert.
   * @return A StandardEntityURN.
   */
  public static StandardEntityURN fromString( String s ) {
    for ( StandardEntityURN next : StandardEntityURN.values() ) {
      if ( next.urn.equals( s ) ) {
        return next;
      }
    }
    throw new IllegalArgumentException( s );
  }
}
