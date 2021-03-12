package rescuecore2.standard.entities;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

/**
 * The AmbulanceCentre object.
 */
public class AmbulanceCentre extends Building {

  /**
   * Construct a AmbulanceCentre object with entirely undefined values.
   *
   * @param id
   *          The ID of this entity.
   */
  public AmbulanceCentre( EntityID id ) {
    super( id );
  }


  /**
   * AmbulanceCentre copy constructor.
   *
   * @param other
   *          The AmbulanceCentre to copy.
   */
  public AmbulanceCentre( AmbulanceCentre other ) {
    super( other );
  }


  /**
   * Create an ambulance centre based on another Building.
   *
   * @param other
   *          The Building to copy.
   */
  public AmbulanceCentre( Building other ) {
    super( other );
  }


  @Override
  protected Entity copyImpl() {
    return new AmbulanceCentre( getID() );
  }


  @Override
  public StandardEntityURN getStandardURN() {
    return StandardEntityURN.AMBULANCE_CENTRE;
  }


  @Override
  protected String getEntityName() {
    return "Ambulance centre";
  }
}
