package sample;

import java.util.Collection;
import java.util.EnumSet;
import org.apache.log4j.Logger;
import rescuecore2.commands.Command;
import rescuecore2.registry.Registry;
import rescuecore2.registry.EntityFactory;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntityFactory;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

/**
 * An agent for testing custom registry objects.
 */
public class CustomRegistryTestAgent extends AbstractSampleAgent<Human> {

  private static final Logger LOG = Logger
      .getLogger( CustomRegistryTestAgent.class );


  @Override
  public String toString() {
    return "Custom registry test agent";
  }


  @Override
  public Registry getPreferredRegistry( Registry parent ) {
    Registry result = new Registry( "Custom registry", parent );
    result.registerEntityFactory( new CustomEntityFactory() );
    return result;
  }


  @Override
  protected void postConnect() {
    super.postConnect();
    LOG.info( "Custom registry test agent " + getID() + " connected" );
    LOG.info( "Current registry: " + Registry.getCurrentRegistry() );
  }


  @Override
  protected void think( int time, ChangeSet changed,
      Collection<Command> heard ) {
    sendRest( time );
  }


  @Override
  protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
    return EnumSet.of( StandardEntityURN.FIRE_BRIGADE,
        StandardEntityURN.POLICE_FORCE, StandardEntityURN.AMBULANCE_TEAM );
  }


  private static class CustomEntityFactory implements EntityFactory {

    private StandardEntityFactory downstream = StandardEntityFactory.INSTANCE;


    @Override
    public Entity makeEntity( String urn, EntityID id ) {
      // Logger.debug("CustomEntityFactory.makeEntity(" + urn + ", " + id +
      // ")");
      return downstream.makeEntity( urn, id );
    }


    @Override
    public String[] getKnownEntityURNs() {
      return downstream.getKnownEntityURNs();
    }


    @Override
    public String toString() {
      return "Custom entity factory";
    }
  }
}
