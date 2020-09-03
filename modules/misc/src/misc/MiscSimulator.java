package misc;

import javax.swing.JComponent;

import rescuecore2.messages.Command;
import rescuecore2.messages.control.KSCommands;
import rescuecore2.messages.control.KSUpdate;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.standard.entities.*;
import rescuecore2.standard.messages.AKUnload;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.EntityListener;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.log.Logger;

import rescuecore2.standard.messages.AKRescue;
import rescuecore2.standard.components.StandardSimulator;

import rescuecore2.GUIComponent;

import java.util.*;

/**
 * Implementation of the legacy misc simulator.
 *
 * @author Maitreyi Nanjanath
 * @author Cameron Skinner
 *
 *         Implementation of Refuge Bed Capacity
 * @author Farshid Faraji
 */
public class MiscSimulator extends StandardSimulator implements GUIComponent {

  private Map<EntityID, HumanAttributes> humans;
  private Set<EntityID>                  newlyBrokenBuildings;
  private Map<EntityID, Integer>         oldBrokenBuildingsBuriedness = new HashMap<>();
  private MiscParameters                 parameters;
  private MiscSimulatorGUI               gui;
  private int                            GAS_STATION_EXPLOSION_RANG;
  private int                            GAS_STATION_Buriedness_Bound;
  private int                            GAS_STATION_Buriedness_MIN;
  private int                            GAS_STATION_Damage_Bound;
  private int                            GAS_STATION_Damage_MIN;

  private Set<EntityID>                  notExplosedGasStations;
  private Map<EntityID, Deque<EntityID>> waitingList;


  @Override
  public JComponent getGUIComponent() {
    if ( gui == null ) {
      gui = new MiscSimulatorGUI();
    }
    return gui;
  }


  @Override
  public String getGUIComponentName() {
    return "Misc simulator";
  }


  @Override
  protected void postConnect() {
    super.postConnect();
    notExplosedGasStations = new HashSet<>();
    waitingList = new HashMap<EntityID, Deque<EntityID>>();

    parameters = new MiscParameters( config );
    GAS_STATION_EXPLOSION_RANG = config
        .getIntValue( "ignition.gas_station.explosion.range", 0 );
    GAS_STATION_Buriedness_Bound = config
        .getIntValue( "misc.gas_station.Buriedness.bound", 30 );
    GAS_STATION_Buriedness_MIN = config
        .getIntValue( "misc.gas_station.Buriedness.min", 0 );
    GAS_STATION_Damage_Bound = config
        .getIntValue( "misc.gas_station.Damage.bound", 50 );
    GAS_STATION_Damage_MIN = config.getIntValue( "misc.gas_station.Damage.min",
        15 );

    humans = new HashMap<EntityID, HumanAttributes>();
    newlyBrokenBuildings = new HashSet<EntityID>();
    Logger.info( "MiscSimulator connected. World has "
        + model.getAllEntities().size() + " entities." );
    BuildingChangeListener buildingListener = new BuildingChangeListener();
    // HumanChangeListener humanListener = new HumanChangeListener();
    for ( Entity et : model.getAllEntities() ) {
      if ( et instanceof GasStation ) {
        notExplosedGasStations.add( et.getID() );
      }
      if ( et instanceof Refuge ) {
        Deque<EntityID> wlist = new LinkedList<EntityID>();
        waitingList.put( et.getID(), wlist );
      }
      if ( et instanceof Building ) {
        et.addEntityListener( buildingListener );
      } else if ( et instanceof Human ) {
        // et.addEntityListener(humanListener);
        Human human = (Human) et;
        HumanAttributes ha = new HumanAttributes( human, config );
        humans.put( ha.getID(), ha );
      }
    }
  }


  @Override
  protected void processCommands( KSCommands c, ChangeSet changes ) {
    long start = System.currentTimeMillis();
    int time = c.getTime();
    Logger.info( "Timestep " + time );

    for ( Command com : c.getCommands() ) {

      if ( checkValidity( com ) ) {
        if ( com instanceof AKRescue ) {
          Human human = (Human) ( model
              .getEntity( ( (AKRescue) com ).getTarget() ) );
          handleRescue( human, changes );
        }
        /*
         * For the implementation of Refuge Bed Capacity
         **/
        if ( com instanceof AKUnload ) {
          handleUnload( com, changes );
        }
      } else {
        Logger.debug( "Ignoring " + com );
      }
    }

    updateRefuges();

    processBrokenBuildings( changes );
    processBurningBuildings( changes );
    processExplodedGasStations( changes );
    updateDamage( changes );

    updateChangeSet( changes );

    // Clean up
    newlyBrokenBuildings.clear();
    writeDebugOutput( c.getTime() );
    if ( gui != null ) {
      gui.refresh( humans.values() );
    }
    long end = System.currentTimeMillis();
    Logger.info( "Timestep " + time + " took " + ( end - start ) + " ms" );
  }


  private void processExplodedGasStations( ChangeSet changes ) {
    Logger.info( "processExplodedGasStations for " + notExplosedGasStations );
    for ( Iterator<EntityID> iterator = notExplosedGasStations
        .iterator(); iterator.hasNext(); ) {
      GasStation gasStation = (GasStation) model.getEntity( iterator.next() );
      if ( gasStation.isFierynessDefined() && gasStation.getFieryness() == 1 ) {
        for ( HumanAttributes hA : humans.values() ) {

          Human human = hA.getHuman();
          if ( !human.isXDefined() || !human.isYDefined() ) continue;
          if ( GeometryTools2D.getDistance(
              new Point2D( human.getX(), human.getY() ),
              new Point2D( gasStation.getX(),
                  gasStation.getY() ) ) < GAS_STATION_EXPLOSION_RANG ) {
            Logger.info( human + " getting damage from explosion..." + human );
            int oldBuriedness = human.isBuriednessDefined()
                ? human.getBuriedness()
                : 0;
            human.setBuriedness( oldBuriedness
                + hA.getRandom().nextInt( GAS_STATION_Buriedness_Bound )
                + GAS_STATION_Buriedness_MIN );
            changes.addChange( human, human.getBuriednessProperty() );
            // Check for injury from being exploded
            int damage = hA.getRandom().nextInt( GAS_STATION_Damage_Bound )
                + GAS_STATION_Damage_MIN;
            if ( damage != 0 ) {
              hA.addCollapseDamage( damage );
            }
          }

        }

        iterator.remove();
      }
    }

  }


  private void processBrokenBuildings( ChangeSet changes ) {
    for ( HumanAttributes hA : humans.values() ) {
      Human human = hA.getHuman();
      EntityID positionID = human.getPosition();
      if ( !newlyBrokenBuildings.contains( positionID ) ) {
        continue;
      }
      // Human is in a newly collapsed building
      // Check for buriedness
      Logger.trace( "Checking if human should be buried in broken building" );
      Building b = (Building) human.getPosition( model );
      if ( parameters.shouldBuryAgent( b, hA ) ) {
        int buriedness = parameters.getBuriedness( b )
            - oldBrokenBuildingsBuriedness.get( b.getID() );

        if ( buriedness != 0 ) {
          int oldBuriedness = human.isBuriednessDefined()
              ? human.getBuriedness()
              : 0;
          human.setBuriedness( oldBuriedness + buriedness );
          changes.addChange( human, human.getBuriednessProperty() );
          // Check for injury from being buried
          int damage = parameters.getBuryDamage( b, hA );
          if ( damage != 0 ) {
            hA.addBuriednessDamage( damage );
          }
        }
      }
      // Now check for injury from the collapse
      int damage = parameters.getCollapseDamage( b, hA );
      if ( damage != 0 ) {
        hA.addCollapseDamage( damage );
      }
    }
  }


  private void processBurningBuildings( ChangeSet changes ) {
    for ( HumanAttributes hA : humans.values() ) {
      Human human = hA.getHuman();
      EntityID positionID = human.getPosition();
      Entity position = human.getPosition( model );
      if ( position instanceof Building
          && ( (Building) position ).isOnFire() ) {
        // Human is in a burning building
        int damage = parameters.getFireDamage( (Building) position, hA );
        if ( damage != 0 ) {
          hA.addFireDamage( damage );
        }
      }
    }
  }


  private void writeDebugOutput( int time ) {
    StringBuilder builder = new StringBuilder();
    Formatter format = new Formatter( builder );
    format.format( "Agents damaged or buried at timestep %1d%n", time );
    format.format(
        "    ID    |   HP   | Damage |   Bury   | Collapse |   Fire   | Buriedness%n" );
    for ( HumanAttributes ha : humans.values() ) {
      Human h = ha.getHuman();
      int hp = h.isHPDefined() ? h.getHP() : 0;
      int damage = ha.getTotalDamage();
      int buriedness = h.isBuriednessDefined() ? h.getBuriedness() : 0;
      boolean isAlive = hp > 0;
      boolean hasDamage = damage > 0;
      boolean isBuried = buriedness > 0;
      if ( ( hasDamage || isBuried ) && isAlive ) {
        format.format(
            "%1$9d | %2$6d | %3$6d | %4$8.3f | %5$8.3f | %6$8.3f | %7$6d%n",
            ha.getID().getValue(), hp, damage, ha.getBuriednessDamage(),
            ha.getCollapseDamage(), ha.getFireDamage(), buriedness );
      }
    }
    Logger.debug( builder.toString() );
  }


  private void updateDamage( ChangeSet changes ) {
    for ( HumanAttributes ha : humans.values() ) {
      Human h = ha.getHuman();
      int oldDamage = ha.getTotalDamage();
      if ( h.isPositionDefined()
          && !( h.getPosition( model ) instanceof Refuge ) ) {
        updateDamage( ha );
        int hp = h.isHPDefined() ? h.getHP() : 0;
        int damage = ha.getTotalDamage();

        h.setDamage( damage );
        changes.addChange( ha.getHuman(), ha.getHuman().getDamageProperty() );

        // Update HP
        boolean isAlive = hp > 0;
        boolean hasDamage = damage > 0;

        if ( isAlive && hasDamage ) {
          int newHP = Math.max( 0, hp - damage );
          h.setHP( newHP );
          changes.addChange( ha.getHuman(), ha.getHuman().getHPProperty() );
        }
      }
      /*
       * For the implementation of Refuge Bed Capacity Damage increases and HP
       * decreases while victim is in waiting list in Refuge While victim is on
       * the bed, Damage is reducing but HP is fix human will not die while on
       * the bed but it takes time to get damage to 0
       */

      else if ( h.isPositionDefined()
          && ( h.getPosition( model ) instanceof Refuge ) && h.isHPDefined()
          && h.getHP() > 0 ) {
        if ( waitingList.get( h.getPosition() ).size() > 0
            && waitingList.get( h.getPosition() ).contains( h.getID() ) ) {
          updateDamage( ha );
          int hp = h.isHPDefined() ? h.getHP() : 0;
          int damage = ha.getTotalDamage();

          h.setDamage( damage );
          changes.addChange( ha.getHuman(), ha.getHuman().getDamageProperty() );

          // Update HP
          boolean isAlive = hp > 0;
          boolean hasDamage = damage > 0;

          if ( isAlive && hasDamage ) {
            int newHP = Math.max( 0, hp - damage );
            h.setHP( newHP );
            changes.addChange( ha.getHuman(), ha.getHuman().getHPProperty() );
          }
        } else {
          updateDamageInRefuge( ha );
          h.setDamage( ha.getTotalDamage() );
          changes.addChange( ha.getHuman(), ha.getHuman().getDamageProperty() );

          if ( oldDamage > 0 && h.getDamage() <= 0 ) {
            if ( waitingList.get( h.getPosition() ).size() > 0 ) {
              waitingList.get( h.getPosition() ).remove();
            } else
              ( (Refuge) h.getPosition( model ) ).decreaseOccupiedBeds();
          }
        }
      }
    }
  }


  private void updateDamage( HumanAttributes ha ) {
    Human h = ha.getHuman();
    if ( h.getHP() <= 0 ) {
      return; // Agent is already dead.
    }
    ha.progressDamage();
  }


  /*
   * For the implementation of Refuge Bed Capacity
   **/
  private void updateDamageInRefuge( HumanAttributes ha ) {
    Human h = ha.getHuman();
    if ( h.getHP() <= 0 ) {
      return; // Agent is already dead.
    }
    ha.progressDamageInRefuge();
  }


  private boolean checkValidity( Command command ) {
    Entity e = model.getEntity( command.getAgentID() );
    if ( e == null ) {
      Logger.warn( "Received a " + command.getURN()
          + " command from an unknown agent: " + command.getAgentID() );
      return false;
    }
    if ( command instanceof AKRescue ) {
      return checkRescue( (AKRescue) command, e );
    }
    if ( command instanceof AKUnload )
      return checkUnload( (AKUnload) command, e );

    return false;
  }


  private boolean checkRescue( AKRescue rescue, Entity agent ) {
    EntityID targetID = rescue.getTarget();
    Entity target = model.getEntity( targetID );
    if ( !( agent instanceof FireBrigade ) ) {
      Logger.warn( "Rejecting rescue command from agent " + agent.getID()
          + " who is of type " + agent.getURN() );
      return false;
    }
    if ( target == null ) {
      Logger.warn( "Rejecting rescue command from agent " + agent.getID()
          + " for a non-existant target " + targetID );
      return false;
    }
    if ( !( target instanceof Human ) ) {
      Logger.warn( "Rejecting rescue command from agent " + agent.getID()
          + " for a non-human target: " + targetID + " is of type "
          + target.getURN() );
      return false;
    }
    Human h = (Human) target;
    FireBrigade fb = (FireBrigade) agent;
    if ( fb.isHPDefined() && fb.getHP() <= 0 ) {
      Logger.warn( "Rejecting rescue command from agent " + agent.getID()
          + ": agent is dead" );
      return false;
    }
    if ( fb.isBuriednessDefined() && fb.getBuriedness() > 0 ) {
      Logger.warn( "Rejecting rescue command from agent " + agent.getID()
          + ": agent is buried" );
      return false;
    }
    if ( !h.isBuriednessDefined() || h.getBuriedness() == 0 ) {
      Logger.warn( "Rejecting rescue command from agent " + agent.getID()
          + " for a non-buried target " + targetID );
      return false;
    }
    if ( !h.isPositionDefined() || !fb.isPositionDefined()
        || !h.getPosition().equals( fb.getPosition() ) ) {
      Logger.warn( "Rejecting rescue command from agent " + agent.getID()
          + " for a non-adjacent target " + targetID );
      return false;
    }
    if ( h.getID().equals( fb.getID() ) ) {
      Logger.warn( "Rejecting rescue command from agent " + agent.getID()
          + ": tried to rescue self" );
      return false;
    }
    return true;
  }


  private boolean checkUnload( AKUnload unload, Entity agent ) {
    if ( !( agent instanceof AmbulanceTeam ) ) {
      Logger.warn( "Rejecting unload command from agent " + agent.getID()
          + " who is of type " + agent.getURN() );
      return false;
    }

    AmbulanceTeam at = (AmbulanceTeam) agent;
    if ( at.isHPDefined() && at.getHP() <= 0 ) {
      Logger.warn( "Rejecting Unload command from agent " + agent.getID()
          + ": agent is dead" );
      return false;
    }

    return true;
  }


  private void handleRescue( Human target, ChangeSet changes ) {
    target.setBuriedness( Math.max( 0, target.getBuriedness() - 1 ) );
    changes.addChange( target, target.getBuriednessProperty() );
  }


  /*
   * For the implementation of Refuge Bed Capacity
   **/
  private void handleUnload( Command command, ChangeSet changes ) {
    EntityID agentID = command.getAgentID();
    Entity agent = model.getEntity( agentID );
    Civilian target = null;
    for ( Entity e : model.getEntitiesOfType( StandardEntityURN.CIVILIAN ) ) {
      Civilian civ = (Civilian) e;
      if ( civ.isPositionDefined() && agentID.equals( civ.getPosition() ) ) {
        target = civ;
        break;
      }
    }
    if ( target != null ) {
      Entity AgentPosition = ( (Human) agent ).getPosition( model );
      if ( AgentPosition != null && AgentPosition instanceof Refuge ) {
        addVictimToWaitingList( AgentPosition, target );
      }
    }
  }


  private class BuildingChangeListener implements EntityListener {

    @Override
    public void propertyChanged( Entity e, Property p, Object oldValue,
        Object newValue ) {
      if ( !( e instanceof Building ) ) {
        return; // we want to only look at buildings
      }

      if ( p.getURN().equals( StandardPropertyURN.BROKENNESS.toString() ) )
        checkBrokenness( e, oldValue, newValue );

    }


    private void checkBrokenness( Entity e, Object oldValue, Object newValue ) {
      double old = oldValue == null ? 0 : (Integer) oldValue;
      double next = newValue == null ? 0 : (Integer) newValue;
      if ( next > old ) {
        newlyBrokenBuildings.add( e.getID() );

      }
    }

  }


  @Override
  protected void handleUpdate( KSUpdate u ) {
    for ( StandardEntity entity : model ) {
      if ( entity instanceof Building ) {
        oldBrokenBuildingsBuriedness.put( entity.getID(),
            parameters.getBuriedness( (Building) entity ) );
      }
    }
    super.handleUpdate( u );
  }


  private void addVictimToWaitingList( Entity refuge, Civilian victim ) {
    if ( victim.getDamage() > 0 )
      waitingList.get( refuge.getID() ).add( victim.getID() );
  }


  /*
   * For the implementation of Refuge Bed Capacity
   **/
  private void updateRefuges() {
    for ( Map.Entry e : waitingList.entrySet() ) {
      ArrayList<EntityID> tempList = new ArrayList<EntityID>();
      for ( EntityID civ : (Deque<EntityID>) e.getValue() ) {
        if ( model.getEntity( civ ) instanceof Human ) {
          if ( ( (Human) model.getEntity( civ ) ).getDamage() <= 0 ) {
            tempList.add( civ );
          }
          if ( ( (Human) model.getEntity( civ ) ).getHP() <= 0 ) {
            tempList.add( civ );
          }
        }
      }
      if ( tempList.size() > 0 ) {
        ( (Deque<EntityID>) e.getValue() ).removeAll( tempList );
      }
    }
    for ( StandardEntity e : model
        .getEntitiesOfType( StandardEntityURN.REFUGE ) ) {
      if ( e instanceof Refuge ) {
        while ( ( (Refuge) e ).getOccupiedBeds() < ( (Refuge) e )
            .getBedCapacity() ) {
          if ( waitingList.get( e.getID() ).size() > 0 ) {
            EntityID removedid = waitingList.get( e.getID() ).remove();
            ( (Refuge) e ).increaseOccupiedBeds();
          } else
            break;
        }
      }
    }
  }


  /*
   * For the implementation of Refuge Bed Capacity
   **/
  private void updateChangeSet( ChangeSet changes ) {
    for ( StandardEntity e : model
        .getEntitiesOfType( StandardEntityURN.REFUGE ) )
      if ( e instanceof Refuge ) {
        int size = waitingList.get( e.getID() ).size();
        ( (Refuge) e ).setWaitingListSize( size );
        changes.addChange( (Refuge) e,
            ( (Refuge) e ).getOccupiedBedsProperty() );
        changes.addChange( (Refuge) e,
            ( (Refuge) e ).getWaitingListSizeProperty() );
      }
  }


  private void removeWaitingList( EntityID ref ) {
    if ( waitingList.get( ref ).size() > 0 ) {
      waitingList.get( ref ).remove();
      Logger.warn( "removing from waiting list..." );
    }
  }
}