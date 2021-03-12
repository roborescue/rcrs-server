package rescuecore2.standard.entities;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

/**
 * The AmbulanceTeam object.
 */
public class AmbulanceTeam extends Human {

  /**
   * Construct a AmbulanceTeam object with entirely undefined values.
   *
   * @param id
   *          The ID of this entity.
   */
  public AmbulanceTeam( EntityID id ) {
    super( id );
  }


  /**
   * AmbulanceTeam copy constructor.
   *
   * @param other
   *          The AmbulanceTeam to copy.
   */
  public AmbulanceTeam( AmbulanceTeam other ) {
    super( other );
  }


  @Override
  protected Entity copyImpl() {
    return new AmbulanceTeam( getID() );
  }


  @Override
  public StandardEntityURN getStandardURN() {
    return StandardEntityURN.AMBULANCE_TEAM;
  }


  @Override
  protected String getEntityName() {
    return "Ambulance team";
  }

}
