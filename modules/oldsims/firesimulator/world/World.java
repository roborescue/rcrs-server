package firesimulator.world;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import firesimulator.util.Configuration;

/**
 * @author tn
 *
 */
public class World implements WorldConstants {
  private static final Logger LOG = Logger.getLogger(World.class);

  private Hashtable pool;
  private Collection extinguishRequests;
  private int time;
  private Collection updatelist;
  private Collection<Building> buildings;
  private Collection firebrigades;
  private int maxX;
  private int maxY;
  private int minX;
  private int minY;
  private double[][] airTemp;
  public ArrayList[][] gridToBuilding;
  public int SAMPLE_SIZE = 5000;
  public float AIR_CAPACITY = 0.2f;
  public float AIR_HEIGHT = 30;
  public int CAPACITY;
  public float maxDist;
  private boolean isInitialized;
  public Collection allWalls;
  private Long hashValue;
  private static World me;

  public World() {
    me = this;
    hashValue = null;
    pool = new Hashtable();
    allWalls = new LinkedList();
    extinguishRequests = new LinkedList();
    updatelist = new LinkedList();
    firebrigades = new HashSet();
    buildings = new HashSet<Building>();
    maxX = Integer.MIN_VALUE;
    maxY = Integer.MIN_VALUE;
    minX = Integer.MAX_VALUE;
    minY = Integer.MAX_VALUE;
    isInitialized = false;
  }

  public static World getWorld() {
    return me;
  }

  public int getMaxX() {
    return maxX;
  }

  public Iterator getExtinguishIterator() {
    return extinguishRequests.iterator();
  }

  public void addExtinguishRequest(Object request) {
    extinguishRequests.add(request);
  }

  public void clearExtinguishRequests() {
    extinguishRequests.clear();
    for (Iterator i = firebrigades.iterator(); i.hasNext();) {
      FireBrigade fb = (FireBrigade) i.next();
      fb.nextCycle();
    }
  }

  public boolean isIntialized() {
    return isInitialized;
  }

  public int getMaxY() {
    return maxY;
  }

  public int getMinX() {
    return minX;
  }

  public int getMinY() {
    return minY;
  }

  private void loadVars() {
    SAMPLE_SIZE = Integer.valueOf(Configuration.getValue("resq-fire.cell_size")).intValue();
    Building.concreteBurning = Float.valueOf(Configuration.getValue("resq-fire.concrete_burning")).floatValue();
    Building.concreteCapacity = Float.valueOf(Configuration.getValue("resq-fire.concrete_capacity")).floatValue();
    Building.concreteEnergie = Float.valueOf(Configuration.getValue("resq-fire.concrete_energy")).floatValue();
    Building.concreteIgnition = Float.valueOf(Configuration.getValue("resq-fire.concrete_ignition")).floatValue();
    Building.concreteSpeed = Float.valueOf(Configuration.getValue("resq-fire.concrete_speed")).floatValue();
    Building.steelBurning = Float.valueOf(Configuration.getValue("resq-fire.steel_burning")).floatValue();
    Building.steelCapacity = Float.valueOf(Configuration.getValue("resq-fire.steel_capacity")).floatValue();
    Building.steelEnergie = Float.valueOf(Configuration.getValue("resq-fire.steel_energy")).floatValue();
    Building.steelIgnition = Float.valueOf(Configuration.getValue("resq-fire.steel_ignition")).floatValue();
    Building.steelSpeed = Float.valueOf(Configuration.getValue("resq-fire.steel_speed")).floatValue();
    Building.woodBurning = Float.valueOf(Configuration.getValue("resq-fire.wooden_burning")).floatValue();
    Building.woodCapacity = Float.valueOf(Configuration.getValue("resq-fire.wooden_capacity")).floatValue();
    Building.woodEnergie = Float.valueOf(Configuration.getValue("resq-fire.wooden_energy")).floatValue();
    Building.woodIgnition = Float.valueOf(Configuration.getValue("resq-fire.wooden_ignition")).floatValue();
    Building.woodSpeed = Float.valueOf(Configuration.getValue("resq-fire.wooden_speed")).floatValue();
    Building.FIRE_INFALMEABLE = Boolean.valueOf(Configuration.getValue("resq-fire.fire_station_inflammable"))
        .booleanValue();
    Building.AMBULANCE_INFALMEABLE = Boolean.valueOf(Configuration.getValue("resq-fire.ambulance_center_inflammable"))
        .booleanValue();
    Building.POLICE_INFALMEABLE = Boolean.valueOf(Configuration.getValue("resq-fire.police_office_inflammable"))
        .booleanValue();
    Building.REFUGE_INFALMEABLE = Boolean.valueOf(Configuration.getValue("resq-fire.refuge_inflammable"))
        .booleanValue();
    Wall.RAY_RATE = Float.valueOf(Configuration.getValue("resq-fire.ray_rate")).floatValue();
    Wall.MAX_SAMPLE_DISTANCE = Integer.valueOf(Configuration.getValue("resq-fire.max_ray_distance")).intValue();
    FireBrigade.REFILL_QUANTITY = Integer.valueOf(Configuration.getValue("resq-fire.water_refill_rate")).intValue();
    FireBrigade.REFILL_HYDRANT_QUANTITY = Integer.valueOf(Configuration.getValue("resq-fire.water_hydrant_refill_rate"))
        .intValue();
    FireBrigade.MAX_WATER_QUANTITY = Integer.valueOf(Configuration.getValue("resq-fire.water_capacity")).intValue();
  }

  public void initialize() {
    LOG.info("World initialising");
    loadVars();
    allWalls.clear();
    clearExtinguishRequests();
    initializeBuildings();
    // initializeRoads();
    initializeAir();
    igniteGISFires();
    isInitialized = true;
    LOG.info("World initialised");
  }

  private void initializeBuildings() {
    for (Building b : buildings) {
      int[] ap = b.getApexes();
      for (int n = 0; n < ap.length; n++) {
        if (ap[n] > maxX)
          maxX = ap[n];
        if (ap[n] < minX)
          minX = ap[n];
        n++;
        if (ap[n] > maxY)
          maxY = ap[n];
        if (ap[n] < minY)
          minY = ap[n];
      }
      b.initialize(this);
    }
    maxDist = (float) Math.sqrt(((maxX - minX) * (maxX - minX)) + ((maxY - minY) * (maxY - minY)));
    initRayValues();
  }

  private void initRayValues() {
    long hash = hash();
    boolean loaded = false;
    String fname = Configuration.getValue("resq-fire.rays.dir") + "/" + hash + ".rays";
    try {
      File f = new File(fname);
      BufferedReader br = new BufferedReader(new FileReader(f));
      float rayDens = Float.parseFloat(br.readLine());
      String nl;
      while (null != (nl = br.readLine())) {
        int x = Integer.parseInt(nl);
        int y = Integer.parseInt(br.readLine());
        int quantity = Integer.parseInt(br.readLine());
        Building[] bl = new Building[quantity];
        float[] wght = new float[quantity];
        for (int c = 0; c < quantity; c++) {
          int ox = Integer.parseInt(br.readLine());
          int oy = Integer.parseInt(br.readLine());
          bl[c] = (Building) getBuilding(ox, oy);
          wght[c] = Float.parseFloat(br.readLine());
        }
        Building b = getBuilding(x, y);
        b.connectedBuilding = bl;
        b.connectedValues = wght;
      }
      loaded = true;
      LOG.info("loaded radiation sample file \"" + fname + "\"");
    } catch (Exception e) {
      LOG.warn("unable to load radiation sample file \"" + fname + "\", sampling:");
      int n = 0;
      long t1 = System.currentTimeMillis();
      for (Building b : buildings) {
        LOG.info("building " + b.getID() + " (" + (n++) + " of " + buildings.size() + ") ");
        b.initWallValues(this);
        long dt = System.currentTimeMillis() - t1;
        dt = dt / n;
        dt = dt * (buildings.size() - n);
        long sec = dt / (1000);
        long min = (sec / 60) % 60;
        long hour = sec / (60 * 60);
        sec = sec % 60;
        LOG.info(" time left: ca. " + hour + ":" + min + ":" + sec);
      }
    }
    try {
      if (!loaded) {
        File f = new File(fname);
        f.createNewFile();
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        bw.write(Wall.RAY_RATE + "\n");
        for (Building b : buildings) {
          bw.write(b.getX() + "\n");
          bw.write(b.getY() + "\n");
          bw.write(b.connectedBuilding.length + "\n");
          for (int c = 0; c < b.connectedBuilding.length; c++) {
            bw.write(b.connectedBuilding[c].getX() + "\n");
            bw.write(b.connectedBuilding[c].getY() + "\n");
            bw.write(b.connectedValues[c] + "\n");
          }
        }
        bw.close();
        LOG.info("wrote radiation sample file \"" + fname + "\"");
      }
    } catch (Exception e) {
      LOG.error("error while writting radiation sample file \"" + fname + "\"", e);
    }
  }

  private Building getBuilding(int x, int y) {
    for (Building b : buildings) {
      if (b.isBuilding(x, y))
        return b;
    }
    LOG.error("parser error");
    throw new NullPointerException();
  }

  public float getMaxDistance() {
    return maxDist;
  }

  private void initializeAir() {
    LOG.info("World width: " + (maxX - minX) + "mm");
    LOG.info("World height: " + (maxY - minY) + "mm");
    int xSamples = 1 + (maxX - minX) / SAMPLE_SIZE;
    int ySamples = 1 + (maxY - minY) / SAMPLE_SIZE;
    LOG.info("grid cell size=" + SAMPLE_SIZE + "mm, x*y=" + xSamples + "*" + ySamples + " = " + (xSamples * ySamples));
    airTemp = new double[xSamples][ySamples];
    for (int x = 0; x < airTemp.length; x++)
      for (int y = 0; y < airTemp[x].length; y++)
        airTemp[x][y] = 0;
    CAPACITY = (int) (SAMPLE_SIZE * SAMPLE_SIZE * AIR_HEIGHT * AIR_CAPACITY) / 1000000;
    // assign buildings
    gridToBuilding = new ArrayList[xSamples][ySamples];
    for (int x = 0; x < gridToBuilding.length; x++)
      for (int y = 0; y < gridToBuilding[0].length; y++)
        gridToBuilding[x][y] = new ArrayList();
    for (Building b : buildings) {
      b.findCells(this);
    }
  }

  public double[][] getAirTemp() {
    return airTemp;
  }

  public void setAirTemp(double[][] a) {
    airTemp = a;
  }

  public void setAirCellTemp(int x, int y, double temp) {
    airTemp[x][y] = temp;
  }

  public double getAirCellTemp(int x, int y) {
    return airTemp[x][y];
  }

  public Collection<Building> getBuildings() {
    return buildings;
  }

  public void addUpdate(RescueObject obj) {
    updatelist.add(obj);
  }

  public void clearUpdates() {
    updatelist.clear();
  }

  public Collection getUpdates() {
    return updatelist;
  }

  public int countObjects() {
    return pool.size();
  }

  public int getTime() {
    return time;
  }

  public RescueObject getObject(int ID) {
    return (RescueObject) pool.get(Integer.valueOf(ID));
  }

  public void putObject(RescueObject obj) {
    pool.put(Integer.valueOf(obj.getID()), obj);
    if (obj instanceof FireBrigade) {
      firebrigades.add(obj);
    }
    if (obj instanceof Building) {
      buildings.add((Building) obj);
    }
    // Moving objects need the world to get their position
    if (obj instanceof MovingObject) {
      ((MovingObject) obj).setWorld(this);
    }
  }

  public void setTime(int time) {
    this.time = time;
  }

  public void reset() {
    loadVars();
    setTime(0);
    resetAir();
    for (Iterator i = buildings.iterator(); i.hasNext(); ((Building) i.next()).reset(this))
      ;
    for (Iterator i = firebrigades.iterator(); i.hasNext(); ((FireBrigade) i.next()).reset())
      ;
    igniteGISFires();
  }

  private void resetAir() {
    for (int x = 0; x < airTemp.length; x++)
      for (int y = 0; y < airTemp[x].length; y++)
        airTemp[x][y] = 0;
  }

  public void igniteGISFires() {
    for (Iterator it = getBuildings().iterator(); it.hasNext();) {
      Building b = (Building) it.next();
      if (b.getIgnition() != 0) {
        b.ignite();
        addUpdate(b);
      }
    }
  }

  public Collection getFirebrigades() {
    return firebrigades;
  }

  public void setFirebrigades(Collection collection) {
    firebrigades = collection;
  }

  public void printSummary() {
    LOG.debug("objects total: " + countObjects());
  }

  public long hash() {
    if (hashValue == null) {
      long sum = 0;
      for (Iterator i = buildings.iterator(); i.hasNext();) {
        Building b = (Building) i.next();
        int[] ap = b.getApexes();
        for (int c = 0; c < ap.length; c++) {
          if (Long.MAX_VALUE - sum <= ap[c]) {
            sum = 0;
          }
          sum += ap[c];
        }
      }
      hashValue = Long.valueOf(sum);
    }
    return hashValue.longValue();
  }
}