package gis2.scenario;

import gis2.GisScenario;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import maps.MapException;
import maps.MapReader;
import maps.gml.GMLBuilding;
import maps.gml.GMLMap;
import maps.gml.GMLShape;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * A class for generating random scenarios.
 */
public class RandomScenarioGenerator {
  private static final int DEFAULT_MIN_CIVS = 50;
  private static final int DEFAULT_MAX_CIVS = 200;
  private static final int DEFAULT_MIN_PLATOONS = 0;
  private static final int DEFAULT_MAX_PLATOONS = 30;
  private static final int DEFAULT_MIN_CENTRES = 0;
  private static final int DEFAULT_MAX_CENTRES = 5;
  private static final int DEFAULT_MIN_REFUGES = 0;
  private static final int DEFAULT_MAX_REFUGES = 5;
  private static final int DEFAULT_MIN_FIRES = 1;
  private static final int DEFAULT_MAX_FIRES = 10;

  private int minCivs;
  private int maxCivs;
  private int minFBs;
  private int maxFBs;
  private int minFSs;
  private int maxFSs;
  private int minPOs;
  private int maxPOs;
  private int minPFs;
  private int maxPFs;
  private int minATs;
  private int maxATs;
  private int minACs;
  private int maxACs;
  private int minFires;
  private int maxFires;
  private int minRefuges;
  private int maxRefuges;

  /**
   * Construct a RandomScenarioGenerator with default parameters.
   */
  public RandomScenarioGenerator() {
    minCivs = DEFAULT_MIN_CIVS;
    maxCivs = DEFAULT_MAX_CIVS;
    minFBs = DEFAULT_MIN_PLATOONS;
    maxFBs = DEFAULT_MAX_PLATOONS;
    minPFs = DEFAULT_MIN_PLATOONS;
    maxPFs = DEFAULT_MAX_PLATOONS;
    minATs = DEFAULT_MIN_PLATOONS;
    maxATs = DEFAULT_MAX_PLATOONS;
    minFSs = DEFAULT_MIN_CENTRES;
    maxFSs = DEFAULT_MAX_CENTRES;
    minPOs = DEFAULT_MIN_CENTRES;
    maxPOs = DEFAULT_MAX_CENTRES;
    minACs = DEFAULT_MIN_CENTRES;
    maxACs = DEFAULT_MAX_CENTRES;
    minFires = DEFAULT_MIN_FIRES;
    maxFires = DEFAULT_MAX_FIRES;
    minRefuges = DEFAULT_MIN_REFUGES;
    maxRefuges = DEFAULT_MAX_REFUGES;
  }

  /**
   * Entry point.
   *
   * @param args Command line arguments: <map directory> [-civ min max] [-fb min
   *             max] [-fs min max] [-pf min max] [-po min max] [-at min max] [-ac
   *             min max] [-refuge min max] [-fire min max].
   */
  public static void main(String[] args) {

    if (args.length < 1) {
      printUsage();
      return;
    }

    String dirName = args[0];
    RandomScenarioGenerator generator = new RandomScenarioGenerator();
    for (int i = 1; i < args.length; ++i) {
      if ("-civ".equals(args[i])) {
        int min = Integer.parseInt(args[i + 1]);
        int max = Integer.parseInt(args[i + 2]);
        i += 2;
        generator.setCivilians(min, max);
      } else if ("-fb".equals(args[i])) {
        int min = Integer.parseInt(args[i + 1]);
        int max = Integer.parseInt(args[i + 2]);
        i += 2;
        generator.setFireBrigades(min, max);
      } else if ("-fs".equals(args[i])) {
        int min = Integer.parseInt(args[i + 1]);
        int max = Integer.parseInt(args[i + 2]);
        i += 2;
        generator.setFireStations(min, max);
      } else if ("-pf".equals(args[i])) {
        int min = Integer.parseInt(args[i + 1]);
        int max = Integer.parseInt(args[i + 2]);
        i += 2;
        generator.setPoliceForces(min, max);
      } else if ("-po".equals(args[i])) {
        int min = Integer.parseInt(args[i + 1]);
        int max = Integer.parseInt(args[i + 2]);
        i += 2;
        generator.setPoliceOffices(min, max);
      } else if ("-at".equals(args[i])) {
        int min = Integer.parseInt(args[i + 1]);
        int max = Integer.parseInt(args[i + 2]);
        i += 2;
        generator.setAmbulanceTeams(min, max);
      } else if ("-ac".equals(args[i])) {
        int min = Integer.parseInt(args[i + 1]);
        int max = Integer.parseInt(args[i + 2]);
        i += 2;
        generator.setAmbulanceCentres(min, max);
      } else if ("-refuge".equals(args[i])) {
        int min = Integer.parseInt(args[i + 1]);
        int max = Integer.parseInt(args[i + 2]);
        i += 2;
        generator.setRefuges(min, max);
      } else if ("-fire".equals(args[i])) {
        int min = Integer.parseInt(args[i + 1]);
        int max = Integer.parseInt(args[i + 2]);
        i += 2;
        generator.setFires(min, max);
      }
    }
    try {
      File dir = new File(dirName);
      GMLMap map = (GMLMap) MapReader.readMap(new File(dir, "map.gml"));
      GisScenario s = generator.makeRandomScenario(map, new Random());
      Document doc = DocumentHelper.createDocument();
      s.write(doc);
      XMLWriter writer = new XMLWriter(new FileOutputStream(new File(dir, "scenario.xml")),
          OutputFormat.createPrettyPrint());
      writer.write(doc);
      writer.flush();
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (MapException e) {
      e.printStackTrace();
    }
  }

  private static void printUsage() {
    System.out.println("Usage: Launch Random Scenario Generator [map] [options]");
    System.out.println();
    System.out.println("[map]  Map directory to generate random scenario");
    System.out.println();
    System.out.println("[options]");
    System.out.println("-civ\tmin max\tSet the minimum and maximum number of civilians");
    System.out.println("-fb\tmin max\tSet the minimum and maximum number of fire brigades");
    System.out.println("-fs\tmin max\tSet the minimum and maximum number of fire stations");
    System.out.println("-pf\tmin max\tSet the minimum and maximum number of police forces");
    System.out.println("-po\tmin max\tSet the minimum and maximum number of police offices");
    System.out.println("-at\tmin max\tSet the minimum and maximum number of ambulance teams");
    System.out.println("-ac\tmin max\tSet the minimum and maximum number of ambulance centers");
    System.out.println("-refuge\tmin max\tSet the minimum and maximum number of refuges");
    System.out.println("-fire\tmin max\tSet the minimum and maximum number of fires");
  }

  /**
   * Set the minimum and maximum number of civilians.
   *
   * @param min The new minimum.
   * @param max The new maximum.
   */
  public void setCivilians(int min, int max) {
    minCivs = min;
    maxCivs = max;
  }

  /**
   * Set the minimum and maximum number of fire brigades.
   *
   * @param min The new minimum.
   * @param max The new maximum.
   */
  public void setFireBrigades(int min, int max) {
    minFBs = min;
    maxFBs = max;
  }

  /**
   * Set the minimum and maximum number of fire stations.
   *
   * @param min The new minimum.
   * @param max The new maximum.
   */
  public void setFireStations(int min, int max) {
    minFSs = min;
    maxFSs = max;
  }

  /**
   * Set the minimum and maximum number of police forces.
   *
   * @param min The new minimum.
   * @param max The new maximum.
   */
  public void setPoliceForces(int min, int max) {
    minPFs = min;
    maxPFs = max;
  }

  /**
   * Set the minimum and maximum number of police offices.
   *
   * @param min The new minimum.
   * @param max The new maximum.
   */
  public void setPoliceOffices(int min, int max) {
    minPOs = min;
    maxPOs = max;
  }

  /**
   * Set the minimum and maximum number of ambulance teams.
   *
   * @param min The new minimum.
   * @param max The new maximum.
   */
  public void setAmbulanceTeams(int min, int max) {
    minATs = min;
    maxATs = max;
  }

  /**
   * Set the minimum and maximum number of ambulance centres.
   *
   * @param min The new minimum.
   * @param max The new maximum.
   */
  public void setAmbulanceCentres(int min, int max) {
    minACs = min;
    maxACs = max;
  }

  /**
   * Set the minimum and maximum number of refuges.
   *
   * @param min The new minimum.
   * @param max The new maximum.
   */
  public void setRefuges(int min, int max) {
    minRefuges = min;
    maxRefuges = max;
  }

  /**
   * Set the minimum and maximum number of fires.
   *
   * @param min The new minimum.
   * @param max The new maximum.
   */
  public void setFires(int min, int max) {
    minFires = min;
    maxFires = max;
  }

  /**
   * Generate a random scenario.
   *
   * @param map    The map to generate a scenario for.
   * @param random A source of randomness.
   * @return A new Scenario.
   */
  public GisScenario makeRandomScenario(GMLMap map, Random random) {
    GisScenario result = new GisScenario();
    int civ = random.nextInt(maxCivs - minCivs + 1) + minCivs;
    int fb = random.nextInt(maxFBs - minFBs + 1) + minFBs;
    int fs = random.nextInt(maxFSs - minFSs + 1) + minFSs;
    int pf = random.nextInt(maxPFs - minPFs + 1) + minPFs;
    int po = random.nextInt(maxPOs - minPOs + 1) + minPOs;
    int at = random.nextInt(maxATs - minATs + 1) + minATs;
    int ac = random.nextInt(maxACs - minACs + 1) + minACs;
    int fire = random.nextInt(maxFires - minFires + 1) + minFires;
    int refuge = random.nextInt(maxRefuges - minRefuges + 1) + minRefuges;
    List<GMLBuilding> buildings = new ArrayList<GMLBuilding>(map.getBuildings());
    Collections.shuffle(buildings, random);
    Iterator<GMLBuilding> it = buildings.iterator();
    placeRefuges(it, result, refuge);
    placeCentres(it, result, fs, po, ac);
    placeFires(it, result, fire);
    placeAgents(map, result, random, fb, pf, at, civ);
    return result;
  }

  private void placeRefuges(Iterator<GMLBuilding> it, GisScenario result, int num) {
    for (int i = 0; i < num; ++i) {
      result.addRefuge(it.next().getID(), 1000, 1000);
    }
  }

  private void placeCentres(Iterator<GMLBuilding> it, GisScenario result, int fire, int police, int ambulance) {
    for (int i = 0; i < fire; ++i) {
      result.addFireStation(it.next().getID());
    }
    for (int i = 0; i < police; ++i) {
      result.addPoliceOffice(it.next().getID());
    }
    for (int i = 0; i < ambulance; ++i) {
      result.addAmbulanceCentre(it.next().getID());
    }
  }

  private void placeFires(Iterator<GMLBuilding> it, GisScenario result, int num) {
    for (int i = 0; i < num; ++i) {
      result.addFire(it.next().getID());
    }
  }

  private void placeAgents(GMLMap map, GisScenario result, Random random, int fire, int police, int ambulance,
      int civ) {
    List<GMLShape> all = new ArrayList<GMLShape>(map.getAllShapes());
    List<GMLBuilding> buildings = new ArrayList<GMLBuilding>(map.getBuildings());
    for (int i = 0; i < fire; ++i) {
      int id = all.get(random.nextInt(all.size())).getID();
      result.addFireBrigade(id);
    }
    for (int i = 0; i < police; ++i) {
      int id = all.get(random.nextInt(all.size())).getID();
      result.addPoliceForce(id);
    }
    for (int i = 0; i < ambulance; ++i) {
      int id = all.get(random.nextInt(all.size())).getID();
      result.addAmbulanceTeam(id);
    }
    for (int i = 0; i < civ; ++i) {
      int id = buildings.get(random.nextInt(buildings.size())).getID();
      result.addCivilian(id);
    }
  }
}