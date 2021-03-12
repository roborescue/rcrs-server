package rescuecore2.standard.entities;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

public class Hydrant extends Road {

  public Hydrant( EntityID id ) {
    super( id );
  }


  public Hydrant( Hydrant other ) {
    super( other );
  }


  /**
   * Create a Hydrant based on another Road.
   *
   * @param other
   *          The Road to copy.
   */
  public Hydrant( Road other ) {
    super( other );
  }


  @Override
  protected Entity copyImpl() {
    return new Hydrant( getID() );
  }


  @Override
  public StandardEntityURN getStandardURN() {
    return StandardEntityURN.HYDRANT;
  }


  @Override
  protected String getEntityName() {
    return "Hydrant";
  }
}
