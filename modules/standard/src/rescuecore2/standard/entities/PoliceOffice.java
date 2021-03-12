package rescuecore2.standard.entities;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

/**
 * The PoliceOffice object.
 */
public class PoliceOffice extends Building {

  /**
   * Construct a PoliceOffice object with entirely undefined property values.
   *
   * @param id
   *          The ID of this entity.
   */
  public PoliceOffice( EntityID id ) {
    super( id );
  }


  /**
   * PoliceOffice copy constructor.
   *
   * @param other
   *          The PoliceOffice to copy.
   */
  public PoliceOffice( PoliceOffice other ) {
    super( other );
  }


  /**
   * Create a police office based on another Building.
   *
   * @param other
   *          The Building to copy.
   */
  public PoliceOffice( Building other ) {
    super( other );
  }


  @Override
  protected Entity copyImpl() {
    return new PoliceOffice( getID() );
  }


  @Override
  public StandardEntityURN getStandardURN() {
    return StandardEntityURN.POLICE_OFFICE;
  }


  @Override
  protected String getEntityName() {
    return "Police office";
  }
}
