package firesimulator.simulator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import firesimulator.util.Configuration;
import firesimulator.util.Rnd;
import firesimulator.world.Building;
import firesimulator.world.FireBrigade;
import firesimulator.world.World;
import org.apache.log4j.Logger;

public class Simulator {

  private static final Logger LOG = Logger.getLogger( Simulator.class );

  private World            world;

  private WindShift        windShift;

  public static float      GAMMA                       = 0.5f;

  public static float      AIR_TO_AIR_COEFFICIENT      = 0.5f;

  public static float      AIR_TO_BUILDING_COEFFICIENT = 45f;

  public static float      WATER_COEFFICIENT           = 0.5f;

  public static float      ENERGY_LOSS                 = 0.9f;

  public static float      WIND_DIRECTION              = 0.9f;

  public static float      WIND_RANDOM                 = 0f;

  public static int        WIND_SPEED                  = 0;

  public static float      RADIATION_COEFFICENT        = 1.0f;

  public static float      TIME_STEP_LENGTH            = 1f;

  public static float      WEIGHT_GRID                 = 0.2f;

  public static float      AIR_CELL_HEAT_CAPACITY      = 1f;

  private Set              monitors;

  private static boolean   verbose;

  private static Simulator me;

  private EnergyHistory    energyHistory;


  public Simulator( World world ) {
    me = this;
    monitors = new HashSet();
    verbose = true;
    this.world = world;
    windShift = null;
  }


  public static Simulator getSimulator() {
    return me;
  }


  public void addMonitor( Monitor monitor ) {
    monitors.add( monitor );
  }


  public void removeMonitor( Monitor monitor ) {
    monitors.remove( monitor );
  }


  private void informStep() {
    for ( Iterator i = monitors.iterator(); i.hasNext(); ) {
      ((Monitor) i.next()).step( world );
    }
  }


  private void informDone() {
    for ( Iterator i = monitors.iterator(); i.hasNext(); ) {
      ((Monitor) i.next()).done( world );
    }
  }


  private void informReset() {
    for ( Iterator i = monitors.iterator(); i.hasNext(); ) {
      ((Monitor) i.next()).reset( world );
    }
  }


  public void step( int timestep ) {
    energyHistory = new EnergyHistory( world, timestep );
    refill();
    executeExtinguishRequests();
    burn();
    cool();
    updateGrid();
    exchangeBuilding();
    // FIXED
    cool();
    energyHistory.registerFinalEnergy( world );
    energyHistory.logSummary();
  }


  private void cool() {
    for ( Iterator i = world.getBuildings().iterator(); i.hasNext(); ) {
      Building b = (Building) i.next();
      waterCooling( b );
    }
  }


  private void refill() {
    for ( Iterator i = world.getFirebrigades().iterator(); i.hasNext(); ) {
      FireBrigade fb = ((FireBrigade) i.next());
      if ( fb.refill() ) {
        LOG.debug( "refilling fire brigade " + fb.getID() );
      }
    }
  }


  private void executeExtinguishRequests() {
    for ( Iterator i = world.getExtinguishIterator(); i.hasNext(); ) {
      ExtinguishRequest er = (ExtinguishRequest) i.next();
      er.execute();
    }
    world.clearExtinguishRequests();
  }


  private void burn() {
    for ( Building b : world.getBuildings() ) {
      if ( b.getTemperature() >= b.getIgnitionPoint() && b.fuel > 0
          && b.isInflameable() ) {
        float consumed = b.getConsum();
        if ( consumed > b.fuel ) {
          consumed = b.fuel;
        }
        double oldFuel = b.fuel;
        double oldEnergy = b.getEnergy();
        double oldTemp = b.getTemperature();
        b.setEnergy( b.getEnergy() + consumed );
        energyHistory.registerBurn( b, consumed );
        b.fuel -= consumed;
        b.setPrevBurned( consumed );
      } else {
        b.setPrevBurned( 0f );
      }
    }
  }


  private void waterCooling( Building b ) {
    double lWATER_COEFFICIENT = (b.getFieryness() > 0 && b.getFieryness() < 4
        ? WATER_COEFFICIENT
        : WATER_COEFFICIENT * GAMMA);
    boolean cond = false;
    if ( b.getWaterQuantity() > 0 ) {
      double oldEnergy = b.getEnergy();
      double oldTemp = b.getTemperature();
      double oldWater = b.getWaterQuantity();
      double dE = b.getTemperature() * b.getCapacity();
      if ( dE <= 0 ) {
        return;
      }
      double effect = b.getWaterQuantity() * lWATER_COEFFICIENT;
      int consumed = b.getWaterQuantity();
      if ( effect > dE ) {
        cond = true;
        double pc = 1 - ((effect - dE) / effect);
        effect *= pc;
        consumed *= pc;
      }
      b.setWaterQuantity( b.getWaterQuantity() - consumed );
      b.setEnergy( b.getEnergy() - effect );
      energyHistory.registerCool( b, effect );
      LOG.debug( "Building " + b.getID() + " water cooling" );
      LOG.debug( "Old energy: " + oldEnergy + ", old temperature: " + oldTemp
          + ", old water: " + oldWater );
      LOG.debug( "Consumed " + consumed + " water: effect = " + effect );
      LOG.debug( "New energy: " + b.getEnergy() + ", new temperature: "
          + b.getTemperature() + ", new water: " + b.getWaterQuantity() );
    }
  }


  private void exchangeBuilding() {
    for ( Iterator i = world.getBuildings().iterator(); i.hasNext(); ) {
      Building b = (Building) i.next();
      exchangeWithAir( b );
    }
    double sumdt = 0;
    Map<Building, Double> radiation = new HashMap<Building, Double>();
    for ( Iterator i = world.getBuildings().iterator(); i.hasNext(); ) {
      Building b = (Building) i.next();
      double radEn = b.getRadiationEnergy();
      radiation.put( b, radEn );
    }
    for ( Iterator i = world.getBuildings().iterator(); i.hasNext(); ) {
      Building b = (Building) i.next();
      double radEn = radiation.get( b );
      Building[] bs = b.connectedBuilding;
      float[] vs = b.connectedValues;

      for ( int c = 0; c < vs.length; c++ ) {
        double oldEnergy = bs[c].getEnergy();
        double connectionValue = vs[c];
        double a = radEn * connectionValue;
        double sum = oldEnergy + a;
        bs[c].setEnergy( sum );
        energyHistory.registerRadiationGain( bs[c], a );
      }
      b.setEnergy( b.getEnergy() - radEn );
      energyHistory.registerRadiationLoss( b, -radEn );
    }
  }


  private void exchangeWithAir( Building b ) {
    // Give/take heat to/from air cells
    double oldTemperature = b.getTemperature();
    double oldEnergy = b.getEnergy();
    double energyDelta = 0;

    for ( int[] nextCell : b.cells ) {
      int cellX = nextCell[0];
      int cellY = nextCell[1];
      double cellCover = nextCell[2] / 100.0;
      double cellTemp = world.getAirCellTemp( cellX, cellY );
      double dT = cellTemp - b.getTemperature();
      double energyTransferToBuilding = dT * AIR_TO_BUILDING_COEFFICIENT
          * TIME_STEP_LENGTH * cellCover * world.SAMPLE_SIZE;
      energyDelta += energyTransferToBuilding;
      double newCellTemp = cellTemp - energyTransferToBuilding
          / (AIR_CELL_HEAT_CAPACITY * world.SAMPLE_SIZE);
      world.setAirCellTemp( cellX, cellY, newCellTemp );
    }
    b.setEnergy( oldEnergy + energyDelta );
    energyHistory.registerAir( b, energyDelta );
  }


  private void updateGrid() {
    LOG.debug( "Updating air grid" );
    double[][] airtemp = world.getAirTemp();
    double[][] newairtemp = new double[airtemp.length][airtemp[0].length];
    for ( int x = 0; x < airtemp.length; x++ ) {
      for ( int y = 0; y < airtemp[0].length; y++ ) {
        double dt = (averageTemp( x, y ) - airtemp[x][y]);
        double change = (dt * AIR_TO_AIR_COEFFICIENT * TIME_STEP_LENGTH);
        newairtemp[x][y] = relTemp( airtemp[x][y] + change );
        if ( !(newairtemp[x][y] > -Double.MAX_VALUE
            && newairtemp[x][y] < Double.MAX_VALUE) ) {
          LOG.warn( "Value is not sensible: " + newairtemp[x][y] );
          newairtemp[x][y] = Double.MAX_VALUE * 0.75;
        }
        if ( newairtemp[x][y] == Double.NEGATIVE_INFINITY
            || newairtemp[x][y] == Double.POSITIVE_INFINITY ) {
          LOG.warn( "aha" );
        }
      }
    }
    world.setAirTemp( newairtemp );
    // Disable on October 21, 2018 because the wind direction and speed was not
    // correctly implemented.
    // world.setAirTemp( getWindShift().shift( world.getAirTemp(), this ) );
  }


  private double relTemp( double deltaT ) {
    return Math.max( 0, deltaT * ENERGY_LOSS * TIME_STEP_LENGTH );
  }


  private double averageTemp( int x, int y ) {
    double rv = neighbourCellAverage( x, y ) / weightSummCells( x, y );
    return rv;
  }


  private double neighbourCellAverage( int x, int y ) {
    double total = getTempAt( x + 1, y - 1 );
    total += getTempAt( x + 1, y );
    total += getTempAt( x + 1, y + 1 );
    total += getTempAt( x, y - 1 );
    total += getTempAt( x, y + 1 );
    total += getTempAt( x - 1, y - 1 );
    total += getTempAt( x - 1, y );
    total += getTempAt( x - 1, y + 1 );
    return total * WEIGHT_GRID;
  }


  private float weightSummCells( int x, int y ) {
    return 8 * WEIGHT_GRID;
  }


  protected double getTempAt( int x, int y ) {
    if ( x < 0 || y < 0 || x >= world.getAirTemp().length
        || y >= world.getAirTemp()[0].length )
      return 0;
    return world.getAirTemp()[x][y];
  }


  public void setWind( float direction, float speed ) {
    windShift = new WindShift( direction, speed, world.SAMPLE_SIZE );
  }


  public WindShift getWindShift() {
    if ( WIND_RANDOM > 0 && windShift != null ) {
      float nd = (float) (windShift.direction
          + windShift.direction * WIND_RANDOM * Rnd.get01());
      float ns = (float) (windShift.speed
          + windShift.speed * WIND_RANDOM * Rnd.get01());
      setWind( nd, ns );
    }
    if ( windShift == null || windShift.direction != WIND_DIRECTION
        || windShift.speed != WIND_SPEED )
      setWind( WIND_DIRECTION, WIND_SPEED );
    return windShift;
  }


  private void loadVars() {
    AIR_TO_BUILDING_COEFFICIENT = Float.parseFloat(
        Configuration.getValue( "resq-fire.air_to_building_flow" ) );
    AIR_TO_AIR_COEFFICIENT = Float
        .parseFloat( Configuration.getValue( "resq-fire.air_to_air_flow" ) );
    ENERGY_LOSS = Float
        .parseFloat( Configuration.getValue( "resq-fire.energy_loss" ) );
    WATER_COEFFICIENT = Float.parseFloat(
        Configuration.getValue( "resq-fire.water_thermal_capacity" ) );
    // Disable on October 21, 2018 because the wind direction and speed was not
    // correctly implemented.
    // WIND_SPEED = Integer
    // .parseInt( Configuration.getValue( "resq-fire.wind_speed" ) );
    // WIND_DIRECTION = Float
    // .parseFloat( Configuration.getValue( "resq-fire.wind_direction" ) );
    // WIND_RANDOM = Float
    // .parseFloat( Configuration.getValue( "resq-fire.wind_random" ) );
    RADIATION_COEFFICENT = Float.parseFloat(
        Configuration.getValue( "resq-fire.radiation_coefficient" ) );
    AIR_CELL_HEAT_CAPACITY = Float.parseFloat(
        Configuration.getValue( "resq-fire.air_cell_heat_capacity" ) );
    ExtinguishRequest.MAX_WATER_PER_CYCLE = Integer.parseInt(
        Configuration.getValue( "resq-fire.max_extinguish_power_sum" ) );
    ExtinguishRequest.MAX_DISTANCE = Integer
        .parseInt( Configuration.getValue( "resq-fire.water_distance" ) );
    GAMMA = Float.parseFloat( Configuration.getValue( "resq-fire.gamma" ) );
    Rnd.setSeed( Long.parseLong( Configuration.getValue( "random.seed" ) ) );

  }


  public void initialize() {
    try {
      loadVars();
    } catch ( Exception e ) {
      LOG.fatal( "invalid configuration, aborting", e );
      System.exit( -1 );
    }

    world.initialize();
  }


  public void reset() {
    loadVars();
    world.reset();
    informReset();
  }
}