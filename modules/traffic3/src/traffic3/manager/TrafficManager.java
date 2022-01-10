package traffic3.manager;

import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.rtree.RTree;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import rescuecore2.misc.collections.LazyMap;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import gnu.trove.TIntProcedure;
import traffic3.objects.TrafficAgent;
import traffic3.objects.TrafficArea;
import traffic3.objects.TrafficBlockade;

/**
 * The traffic manager maintains information about traffic simulator objects.
 */
public class TrafficManager {

  private Map<Integer, TrafficArea> areaByID;
  private Map<Integer, TrafficBlockade> blockadeByID;
  private Map<Area, TrafficArea> areas;
  private Map<Blockade, TrafficBlockade> blocks;
  private Map<Human, TrafficAgent> agents;
  private Map<TrafficArea, Collection<TrafficArea>> areaNeighbours;

  private SpatialIndex index;

  /**
   * Construct a new TrafficManager.
   */
  public TrafficManager() {
    areas = new ConcurrentHashMap<Area, TrafficArea>();
    areaByID = new ConcurrentHashMap<Integer, TrafficArea>();
    blocks = new ConcurrentHashMap<Blockade, TrafficBlockade>();
    blockadeByID = new ConcurrentHashMap<Integer, TrafficBlockade>();
    agents = new ConcurrentHashMap<Human, TrafficAgent>();
    areaNeighbours = new LazyMap<TrafficArea, Collection<TrafficArea>>() {

      @Override
      public Collection<TrafficArea> createValue() {
        return new HashSet<TrafficArea>();
      }
    };
    index = new RTree();
    index.init(new Properties());
  }


  /**
   * Find the area that contains a point.
   *
   * @param x
   *   The X coordinate.
   * @param y
   *   The Y coordinate.
   *
   * @return The TrafficArea that contains the given point, or null if no such
   * area is found.
   */
  public TrafficArea findArea(double x, double y) {
    final List<TrafficArea> found = new ArrayList<TrafficArea>();
    index.intersects(new Rectangle((float) x, (float) y, (float) x, (float) y),
        new TIntProcedure() {

          @Override
          public boolean execute(int id) {
            found.add(areaByID.get(id));
            return true;
          }
        });
    for (TrafficArea next : found) {
      if (next.contains(x, y)) {
        return next;
      }
    }
    return null;
  }


  /**
   * Get the neighbouring areas to a TrafficArea.
   *
   * @param area
   *   The area to look up.
   *
   * @return All TrafficAreas that share an edge with the given area.
   */
  public Collection<TrafficArea> getNeighbours(TrafficArea area) {
    return areaNeighbours.get(area);
  }


  /**
   * Get all agents in the same area or a neighbouring area as an agent.
   *
   * @param agent
   *   The agent to look up.
   *
   * @return All agents (except the input agent) that are in the same or a
   * neighbouring area.
   */
  public Collection<TrafficAgent> getNearbyAgents(TrafficAgent agent) {
    Set<TrafficAgent> result = new HashSet<TrafficAgent>();
    result.addAll(agent.getArea().getAgents());
    for (TrafficArea next : getNeighbours(agent.getArea())) {
      result.addAll(next.getAgents());
    }
    result.remove(agent);
    return result;
  }


  /**
   * Remove all objects from this manager.
   */
  public void clear() {
    areas.clear();
    blocks.clear();
    agents.clear();
    areaNeighbours.clear();
    areaByID.clear();
    blockadeByID.clear();
    index = new RTree();
    index.init(new Properties());
  }


  /**
   * Register a new TrafficArea.
   *
   * @param area
   *   The TrafficArea to register.
   */
  public void register(TrafficArea area) {
    areas.put(area.getArea(), area);
    int id = area.getArea().getID().getValue();
    areaByID.put(id, area);
    index.add(area.getBounds(), id);
  }


  /**
   * Register a new TrafficAgent.
   *
   * @param agent
   *   The TrafficAgent to register.
   */
  public void register(TrafficAgent agent) {
    agents.put(agent.getHuman(), agent);
  }


  /**
   * Register a new TrafficBlockade.
   *
   * @param block
   *   The TrafficBlockade to register.
   */
  public void register(TrafficBlockade block) {
    blocks.put(block.getBlockade(), block);
    blockadeByID.put(block.getBlockade().getID().getValue(), block);
  }


  /**
   * Remove a blockade.
   *
   * @param block
   *   The TrafficBlockade to remove.
   */
  public void remove(TrafficBlockade block) {
    remove(block.getBlockade());
  }


  /**
   * Remove a blockade.
   *
   * @param block
   *   The Blockade to remove.
   */
  public void remove(Blockade block) {
    blocks.remove(block);
    blockadeByID.remove(block.getID().getValue());
  }


  /**
   * Get all TrafficAgents.
   *
   * @return All TrafficAgents.
   */
  public Collection<TrafficAgent> getAgents() {
    return Collections.unmodifiableCollection(agents.values());
  }


  /**
   * Get all TrafficAreas.
   *
   * @return All TrafficAreas.
   */
  public Collection<TrafficArea> getAreas() {
    return Collections.unmodifiableCollection(areas.values());
  }


  /**
   * Get all TrafficBlockades.
   *
   * @return All TrafficBlockades.
   */
  public Collection<TrafficBlockade> getBlockades() {
    return Collections.unmodifiableCollection(blocks.values());
  }


  /**
   * Compute pre-cached information about the world. TrafficArea and
   * TrafficAgent objects must have already been registered with
   * {@link #register(TrafficArea)} and {@link #register(TrafficAgent)}.
   *
   * @param world
   *   The world model.
   */
  public void cacheInformation(StandardWorldModel world) {
    areaNeighbours.clear();
    for (StandardEntity next : world) {
      if (next instanceof Area) {
        computeNeighbours((Area) next, world);
      }
    }
  }


  /**
   * Get the TrafficArea that wraps a given Area.
   *
   * @param a
   *   The area to look up.
   *
   * @return The TrafficArea that wraps the given area or null if no such
   * TrafficArea exists.
   */
  public TrafficArea getTrafficArea(Area a) {
    return areas.get(a);
  }


  /**
   * Get the TrafficBlockade that wraps a given Blockade.
   *
   * @param b
   *   The blockade to look up.
   *
   * @return The TrafficBlockade that wraps the given blockade or null if no
   * such TrafficBlockade exists.
   */
  public TrafficBlockade getTrafficBlockade(Blockade b) {
    return blocks.get(b);
  }


  /**
   * Get the TrafficAgent that wraps a given human.
   *
   * @param h
   *   The human to look up.
   *
   * @return The TrafficAgent that wraps the given human or null if no such
   * TrafficAgent exists.
   */
  public TrafficAgent getTrafficAgent(Human h) {
    return agents.get(h);
  }


  private void computeNeighbours(Area a, StandardWorldModel world) {
    Collection<TrafficArea> neighbours = areaNeighbours.get(getTrafficArea(a));
    neighbours.clear();
    for (EntityID id : a.getNeighbours()) {
      Entity e = world.getEntity(id);
      if (e instanceof Area) {
        neighbours.add(getTrafficArea((Area) e));
      }
    }
  }
}