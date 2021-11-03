package firesimulator.world;

import java.awt.Polygon;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.uncommons.maths.number.NumberGenerator;
import org.uncommons.maths.random.GaussianGenerator;

import firesimulator.simulator.Simulator;
import firesimulator.util.Configuration;
import firesimulator.util.Geometry;

/**
 * @author tn
 *
 */
public class Building extends StationaryObject {
  private static final Logger LOG = Logger.getLogger(Building.class);

  private static final double STEFAN_BOLTZMANN_CONSTANT = 0.000000056704;

  public static int WATER_EXTINGUISH_PARAM;
  public static float woodSpeed;
  public static float steelSpeed;
  public static float concreteSpeed;
  public static float WATER_CAPCITY;
  public static boolean POLICE_INFALMEABLE = false;
  public static boolean AMBULANCE_INFALMEABLE = false;
  public static boolean FIRE_INFALMEABLE = false;
  public static boolean REFUGE_INFALMEABLE = false;
  public NumberGenerator<Double> burnRate;
  public boolean fierynessChanged;
  private int waterQuantity;
  private int floors = 1;
  private int attributes = 0;
  private int ignition = 0;
  protected int fieryness = 0;
  private int brokenness = 0;
  public int[][] cells;
  private int[] entrances;
  private int code = 0;
  private float buildingAreaGround = 0;
  private float buildingAreaTotal = 0;
  private int[] apexes;
  private Polygon polygon;
  public float fuel;
  private float initFuel;
  private float prevBurned;
  public float volume;
  public float capacity;
  private double energy;
  static final int FLOOR_HEIGHT = 3;
  public float cooling = 0;
  public Collection walls;
  public Hashtable connectedBuildings;
  public Building[] connectedBuilding;
  public float[] connectedValues;
  public double totalWallArea;
  private int lwater = 0;
  private int lwTime = -1;
  private boolean wasEverWatered = false;
  public boolean inflameable = true;

  public static float woodCapacity = 4;
  public static float steelCapacity = 4;
  public static float concreteCapacity = 4;
  public static float woodIgnition = 400;
  public static float steelIgnition = 400;
  public static float concreteIgnition = 400;
  public static float woodEnergie = 1;
  public static float steelEnergie = 1;
  public static float concreteEnergie = 1;
  public static float woodBurning = 800;
  public static float steelBurning = 800;
  public static float concreteBurning = 800;

  public static final int NORMAL = 0;
  public static final int HEATING = 1;
  public static final int BURNING = 2;
  public static final int COOLING_DOWN = 3;
  public static final int EXTINGUISHED = 5;
  public static final int BURNED_DOWN = 4;

  public Building(int id) {
    super(id);
    polygon = null;
    connectedBuildings = new Hashtable(30);
    initFuel = -1;
    prevBurned = 0;
    java.util.Random random = new java.util.Random(Long.valueOf(Configuration.getValue("random.seed")).longValue());
    burnRate = new GaussianGenerator(
        Double.valueOf(Configuration.getValue("resq-fire.burn-rate-average")).doubleValue(),
        Double.valueOf(Configuration.getValue("resq-fire.burn-rate-variance")).doubleValue(), random);
  }

  public float getBurningTemp() {
    switch (code) {
      case 0:
        return woodBurning;
      case 1:
        return steelBurning;
      default:
        return concreteBurning;
    }
  }

  public float getIgnitionPoint() {
    switch (code) {
      case 0:
        return woodIgnition;
      case 1:
        return steelIgnition;
      default:
        return concreteIgnition;
    }
  }

  public void ignite() {
    energy = getCapacity() * getIgnitionPoint() * 1.5;
  }

  public float getBuildingAreaGround() {
    return buildingAreaGround;
  }

  public int getFloors() {
    return floors;
  }

  public void initialize(World world) {
    initWalls(world);
    setFieryness(0);
    setWaterQuantity(0);
    fierynessChanged = false;
    if (polygon == null) {
      polygon = new Polygon();
      for (int n = 0; n < apexes.length; n++)
        polygon.addPoint(apexes[n], apexes[++n]);
    }
    volume = buildingAreaGround * floors * FLOOR_HEIGHT;
    fuel = getInitialFuel();
    setCapacity(volume * getThermoCapacity());
    energy = 0;
    initFuel = -1;
    prevBurned = 0;
    lwTime = -1;
    lwater = 0;
    wasEverWatered = false;
    LOG.debug("Initialised building " + id + ": ground area = " + buildingAreaGround + ", floors = " + floors
        + ", volume = " + volume + ", initial fuel = " + fuel + ", energy capacity = " + getCapacity());
  }

  public void reset(World w) {
    setFieryness(0);
    setWaterQuantity(0);
    initialize(w);
  }

  private void initWalls(World world) {
    if (walls != null)
      return;
    totalWallArea = 0;
    walls = new LinkedList();
    int fx = apexes[0];
    int fy = apexes[1];
    int lx = fx;
    int ly = fy;
    for (int n = 2; n < apexes.length; n++) {
      int tx = apexes[n];
      int ty = apexes[++n];
      Wall w = new Wall(lx, ly, tx, ty, this);
      if (w.validate()) {
        walls.add(w);
        totalWallArea += FLOOR_HEIGHT * 1000 * w.length;
      } else
        LOG.warn("Ignoring odd wall at building " + getID());
      lx = tx;
      ly = ty;
    }
    Wall w = new Wall(lx, ly, fx, fy, this);
    walls.add(w);
    world.allWalls.addAll(walls);
    totalWallArea = totalWallArea / 1000000d;
  }

  public int hashCode() {
    return id;
  }

  public void initWallValues(World world) {
    int totalHits = 0;
    int totalRays = 0;
    int selfHits = 0;
    int strange = 0;
    for (Iterator w = walls.iterator(); w.hasNext();) {
      Wall wall = (Wall) w.next();
      wall.findHits(world);
      totalHits += wall.hits;
      selfHits += wall.selfHits;
      totalRays += wall.rays;
      strange = wall.strange;
    }
    int c = 0;
    connectedBuilding = new Building[connectedBuildings.size()];
    connectedValues = new float[connectedBuildings.size()];
    float base = totalRays;
    for (Enumeration e = connectedBuildings.keys(); e.hasMoreElements(); c++) {
      Building b = (Building) e.nextElement();
      Integer value = (Integer) connectedBuildings.get(b);
      connectedBuilding[c] = b;
      connectedValues[c] = value.floatValue() / base;
    }
    LOG.debug("{" + (((float) totalHits) * 100 / ((float) totalRays)) + "," + totalRays + "," + totalHits + ","
        + selfHits + "," + strange + "}");
  }

  public float getInitialFuel() {
    if (initFuel < 0) {
      initFuel = (float) (getFuelDensity() * volume);
    }
    return initFuel;
  }

  private float getFuelDensity() {
    switch (code) {
      case 0:
        return woodEnergie;
      case 1:
        return steelEnergie;
      default:
        return concreteEnergie;
    }
  }

  private float getThermoCapacity() {
    switch (code) {
      case 0:
        return woodCapacity;
      case 1:
        return steelCapacity;
      default:
        return concreteCapacity;
    }
  }

  public Polygon getPolygon() {
    return polygon;
  }

  public String getType() {
    return "BUILDING";
  }

  public void setAttributes(int atrb) {
    this.attributes = atrb;
  }

  public int getCode() {
    return code;
  }

  public void setIgnition(int ignition) {
    this.ignition = ignition;
  }

  public int getIgnition() {
    return ignition;
  }

  public void setFieryness(int fieryness) {
    this.fieryness = fieryness;
  }

  public float getFuel() {
    return fuel;
  }

  public int getFieryness() {
    if (!isInflameable())
      return 0;
    if (getTemperature() >= getIgnitionPoint()) {
      if (fuel >= getInitialFuel() * 0.66)
        return 1; // burning, slightly damaged
      if (fuel >= getInitialFuel() * 0.33)
        return 2; // burning, more damaged
      if (fuel > 0)
        return 3; // burning, severly damaged
    }
    if (fuel == getInitialFuel())
      if (wasEverWatered)
        return 4; // not burnt, but watered-damaged
      else
        return 0; // not burnt, no water damage
    if (fuel >= getInitialFuel() * 0.66)
      return 5; // extinguished, slightly damaged
    if (fuel >= getInitialFuel() * 0.33)
      return 6; // extinguished, more damaged
    if (fuel > 0)
      return 7; // extinguished, severely damaged
    return 8; // completely burnt down
  }

  public void setBrokenness(int brk) {
    this.brokenness = brk;
  }

  public void setEntrances(int[] ent) {
    this.entrances = ent;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public void setBuildingAreaGround(float area) {
    this.buildingAreaGround = area;
  }

  public void setBuildingAreaTotal(float area) {
    this.buildingAreaTotal = area;
  }

  public void setApexes(int[] apx) {
    this.apexes = apx;
  }

  public int[] getApexes() {
    return apexes;
  }

  public void setFloors(int floors) {
    this.floors = floors;
  }

  public void findCells(World w) {
    LinkedList tmp = new LinkedList();
    for (int x = 0; x < w.getAirTemp().length; x++)
      for (int y = 0; y < w.getAirTemp()[0].length; y++) {
        int xv = x * w.SAMPLE_SIZE + w.getMinX();
        int yv = y * w.SAMPLE_SIZE + w.getMinY();
        if (Geometry.boundingTest(polygon, xv, yv, w.SAMPLE_SIZE, w.SAMPLE_SIZE)) {
          int pc = Geometry.percent((float) xv, (float) yv, (float) w.SAMPLE_SIZE, (float) w.SAMPLE_SIZE, polygon);
          if (pc > 0) {
            tmp.add(Integer.valueOf(x));
            tmp.add(Integer.valueOf(y));
            tmp.add(Integer.valueOf(pc));
            Object[] o = new Object[] { this, Float.valueOf(pc) };
            w.gridToBuilding[x][y].add(o);
          }
        }
      }
    if (tmp.size() > 0) {
      cells = new int[tmp.size() / 3][3];
      Iterator i = tmp.iterator();
      for (int c = 0; c < cells.length; c++) {
        cells[c][0] = ((Integer) i.next()).intValue();
        cells[c][1] = ((Integer) i.next()).intValue();
        cells[c][2] = ((Integer) i.next()).intValue();
      }
    } else {
      LOG.warn(getID() + " has no cell");
      LOG.warn("Sample size: " + w.SAMPLE_SIZE);
      LOG.warn("World min X, Y: " + w.getMinX() + ", " + w.getMinY());
      LOG.warn("Air grid size: " + w.getAirTemp().length + " x " + w.getAirTemp()[0].length);
      LOG.warn("Building polygon: ");
      for (int i = 0; i < apexes.length; i += 2) {
        LOG.warn(apexes[i] + ", " + apexes[i + 1]);
      }
      int expectedCellX = (apexes[0] - w.getMinX()) / w.SAMPLE_SIZE;
      int expectedCellY = (apexes[1] - w.getMinY()) / w.SAMPLE_SIZE;
      LOG.warn("Building should be in cell " + expectedCellX + ", " + expectedCellY);
      for (int x = 0; x < w.getAirTemp().length; x++) {
        for (int y = 0; y < w.getAirTemp()[0].length; y++) {
          int xv = x * w.SAMPLE_SIZE + w.getMinX();
          int yv = y * w.SAMPLE_SIZE + w.getMinY();
          if (Geometry.boundingTest(polygon, xv, yv, w.SAMPLE_SIZE, w.SAMPLE_SIZE)) {
            LOG.warn("Cell " + x + ", " + y);
            LOG.warn("boundingTest(polygon, " + xv + ", " + yv + ", " + w.SAMPLE_SIZE + ", " + w.SAMPLE_SIZE + ") = "
                + Geometry.boundingTest(polygon, xv, yv, w.SAMPLE_SIZE, w.SAMPLE_SIZE));
            LOG.warn("pc = "
                + Geometry.percent((float) xv, (float) yv, (float) w.SAMPLE_SIZE, (float) w.SAMPLE_SIZE, polygon));
            int counter = 0;
            double dx = w.SAMPLE_SIZE / 100;
            double dy = w.SAMPLE_SIZE / 100;
            for (int i = 0; i < 100; i++) {
              for (int j = 0; j < 100; j++) {
                double testX = dx * i + xv;
                double testY = dy * j + yv;
                if (polygon.contains(dx * i + xv, dy * j + yv)) {
                  counter++;
                  LOG.warn("Point " + testX + ", " + testY + " is inside");
                }
              }
            }
            LOG.warn("Counted " + counter + " interior points");
          }
        }
      }
    }
  }

  public double getTemperature() {
    double rv = energy / getCapacity();
    if (Double.isNaN(rv)) {
      LOG.warn("Building " + id + " getTemperature returned NaN");
      new RuntimeException().printStackTrace();
      LOG.warn("Energy: " + energy);
      LOG.warn("Capacity: " + getCapacity());
      LOG.warn("Volume: " + volume);
      LOG.warn("Thermal capacity: " + getThermoCapacity());
      LOG.warn("Ground area: " + buildingAreaGround);
      LOG.warn("Floors: " + floors);
    }
    if (rv == Double.NaN || rv == Double.POSITIVE_INFINITY || rv == Double.NEGATIVE_INFINITY)
      rv = Double.MAX_VALUE * 0.75;
    return rv;
  }

  public String codeToString() {
    switch (code) {
      case 0:
        return "wooden";
      case 1:
        return "steelframe";
      default:
        return "concret";
    }
  }

  public int getLastWater() {
    return lwater;
  }

  public boolean getLastWatered() {
    return lwTime == World.getWorld().getTime();
  }

  public boolean wasEverWatered() {
    return wasEverWatered;
  }

  public int getWaterQuantity() {
    return waterQuantity;
  }

  public void setWaterQuantity(int i) {
    if (i > waterQuantity) {
      lwTime = World.getWorld().getTime();
      lwater = i - waterQuantity;
      wasEverWatered = true;
    }
    waterQuantity = i;
  }

  public float getCapacity() {
    return capacity;
  }

  public void setCapacity(float f) {
    capacity = f;
  }

  public String toString() {
    String rv = "building " + getID() + "\n";
    for (Iterator i = walls.iterator(); i.hasNext(); rv += i.next() + "\n")
      ;
    return rv;
  }

  public double getRadiationEnergy() {
    double t = getTemperature() + 293; // Assume ambient temperature is 293 Kelvin.
    double radEn = (t * t * t * t) * totalWallArea * Simulator.RADIATION_COEFFICENT * STEFAN_BOLTZMANN_CONSTANT;
    if (id == 23545) {
      LOG.debug("Getting radiation energy for building " + id);
      LOG.debug("t = " + t);
      LOG.debug("t^4 = " + (t * t * t * t));
      LOG.debug("Total wall area: " + totalWallArea);
      LOG.debug("Radiation coefficient: " + Simulator.RADIATION_COEFFICENT);
      LOG.debug("Stefan-Boltzmann constant: " + STEFAN_BOLTZMANN_CONSTANT);
      LOG.debug("Radiation energy: " + radEn);
      LOG.debug("Building energy: " + getEnergy());
    }
    if (radEn == Double.NaN || radEn == Double.POSITIVE_INFINITY || radEn == Double.NEGATIVE_INFINITY)
      radEn = Double.MAX_VALUE * 0.75;
    if (radEn > getEnergy()) {
      radEn = getEnergy();
    }
    return radEn;
  }

  public boolean isBuilding(int x, int y) {
    return getX() == x && getY() == y;
  }

  public double getEnergy() {
    if (energy == Double.NaN || energy == Double.POSITIVE_INFINITY || energy == Double.NEGATIVE_INFINITY)
      energy = Double.MAX_VALUE * 0.75d;
    return energy;
  }

  public void setEnergy(double energy) {
    if (energy == Double.NaN || energy == Double.POSITIVE_INFINITY || energy == Double.NEGATIVE_INFINITY) {
      energy = Double.MAX_VALUE * 0.75d;
    }
    this.energy = energy;
  }

  public float getConsum() {
    if (fuel == 0) {
      return 0;
    }
    float tf = (float) (getTemperature() / 1000f);
    float lf = getFuel() / getInitialFuel();
    float f = (float) (tf * lf * burnRate.nextValue());
    if (f < 0.005f)
      f = 0.005f;
    return getInitialFuel() * f;
  }

  public float getPrevBurned() {
    return prevBurned;
  }

  public void setPrevBurned(float prevBurned) {
    this.prevBurned = prevBurned;
  }

  public void setInflameable(boolean inflameable) {
    this.inflameable = inflameable;
  }

  public boolean isInflameable() {
    return inflameable;
  }
}