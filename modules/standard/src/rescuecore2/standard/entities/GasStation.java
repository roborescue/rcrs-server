package rescuecore2.standard.entities;

import org.json.JSONObject;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

public class GasStation extends Building {

  public GasStation( Building other ) {
    super( other );
  }


  public GasStation( EntityID entityID ) {
    super( entityID );
  }


  @Override
  protected Entity copyImpl() {
    return new GasStation( getID() );
  }


  @Override
  public StandardEntityURN getStandardURN() {
    return StandardEntityURN.GAS_STATION;
  }


  @Override
  protected String getEntityName() {
    return "Gas Station";
  }


  @Override
  public JSONObject toJson() {
    return super.toJson();
  }
}
