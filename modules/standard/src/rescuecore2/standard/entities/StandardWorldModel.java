package rescuecore2.standard.entities;

import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.rtree.RTree;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import rescuecore2.log.Logger;
import rescuecore2.misc.Pair;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.DefaultWorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.WorldModelListener;
import gnu.trove.TIntProcedure;

/**
 * A wrapper around a WorldModel that indexes Entities by location.
 */
public class StandardWorldModel extends DefaultWorldModel<StandardEntity> {

  private SpatialIndex index;

  private Map<StandardEntityURN, Collection<StandardEntity>> storedTypes;
  private Set<StandardEntity> unindexedEntities;
  private Map<Human, Rectangle> humanRectangles;

  private boolean indexed;
  private int minX;
  private int maxX;
  private int minY;
  private int maxY;

  /**
   * Create a StandardWorldModel.
   */
  public StandardWorldModel() {
    super(StandardEntity.class);
    storedTypes = new EnumMap<StandardEntityURN, Collection<StandardEntity>>(
        StandardEntityURN.class);
    unindexedEntities = new HashSet<StandardEntity>();
    humanRectangles = new HashMap<Human, Rectangle>();
    addWorldModelListener(new AddRemoveListener());
    indexed = false;
  }


  @Override
  public void merge(ChangeSet changeSet) {
    super.merge(changeSet);
    // Update human rectangles
    for (Map.Entry<Human, Rectangle> next : humanRectangles.entrySet()) {
      Human h = next.getKey();
      Rectangle r = next.getValue();
      index.delete(r, h.getID().getValue());
      r = makeRectangle(h);
      if (r != null) {
        index.add(r, h.getID().getValue());
        next.setValue(r);
      }
    }
  }


  /**
   * Tell this index to remember a certain class of entities.
   *
   * @param urns
   *   The type URNs to remember.
   */
  public void indexClass(StandardEntityURN... urns) {
    for (StandardEntityURN urn : urns) {
      Collection<StandardEntity> bucket = new HashSet<StandardEntity>();
      for (StandardEntity next : this) {
        if (next.getStandardURN().equals(urn)) {
          bucket.add(next);
        }
      }
      storedTypes.put(urn, bucket);
    }
  }


  /**
   * Re-index the world model.
   */
  public void index() {
    if (indexed && unindexedEntities.isEmpty()) {
      Logger.debug(
          "Not bothering with reindex: No entities are currently unindexed");
      return;
    }
    Logger.debug("Re-indexing world model");
    long start = System.currentTimeMillis();
    index = new RTree();
    index.init(new Properties());
    humanRectangles.clear();
    unindexedEntities.clear();
    minX = Integer.MAX_VALUE;
    maxX = Integer.MIN_VALUE;
    minY = Integer.MAX_VALUE;
    maxY = Integer.MIN_VALUE;
    // Add all rectangles
    for (StandardEntity next : this) {
      Rectangle r = makeRectangle(next);
      if (r != null) {
        index.add(r, next.getID().getValue());
        minX = Math.min(minX, (int) r.minX);
        maxX = Math.max(maxX, (int) r.maxX);
        minY = Math.min(minY, (int) r.minY);
        maxY = Math.max(maxY, (int) r.maxY);
        if (next instanceof Human) {
          humanRectangles.put((Human) next, r);
        }
      }
    }
    long end = System.currentTimeMillis();
    Logger.debug("Finished re-index. Took " + (end - start) + "ms");
    indexed = true;
  }


  /**
   * Get objects within a certain range of an entity.
   *
   * @param entity
   *   The entity to centre the search on.
   * @param range
   *   The range to look up.
   *
   * @return A collection of StandardEntitys that are within range.
   */
  public Collection<StandardEntity> getObjectsInRange(EntityID entity,
      int range) {
    return getObjectsInRange(getEntity(entity), range);
  }


  /**
   * Get objects within a certain range of an entity.
   *
   * @param entity
   *   The entity to centre the search on.
   * @param range
   *   The range to look up.
   *
   * @return A collection of StandardEntitys that are within range.
   */
  public Collection<StandardEntity> getObjectsInRange(StandardEntity entity,
      int range) {
    Pair<Integer, Integer> location = entity.getLocation(this);
    if (location == null) {
      return new HashSet<StandardEntity>();
    }
    return getObjectsInRange(location.first(), location.second(), range);
  }


  /**
   * Get objects within a certain range of a location.
   *
   * @param x
   *   The x coordinate of the location.
   * @param y
   *   The y coordinate of the location.
   * @param range
   *   The range to look up.
   *
   * @return A collection of StandardEntitys that are within range.
   */
  public Collection<StandardEntity> getObjectsInRange(int x, int y, int range) {
    if (!indexed) {
      index();
    }
    return getObjectsInRectangle(x - range, y - range, x + range, y + range);
  }


  /**
   * Get objects inside a given rectangle.
   *
   * @param x1
   *   The x coordinate of the top left corner.
   * @param y1
   *   The y coordinate of the top left corner.
   * @param x2
   *   The x coordinate of the bottom right corner.
   * @param y2
   *   The y coordinate of the bottom right corner.
   *
   * @return A collection of StandardEntitys that are inside the rectangle.
   */
  public Collection<StandardEntity> getObjectsInRectangle(int x1, int y1,
      int x2, int y2) {
    if (!indexed) {
      index();
    }
    final Collection<StandardEntity> result = new HashSet<StandardEntity>();
    Rectangle r = new Rectangle(x1, y1, x2, y2);
    index.intersects(r, new TIntProcedure() {

      @Override
      public boolean execute(int id) {
        StandardEntity e = getEntity(new EntityID(id));
        if (e != null) {
          result.add(e);
        }
        return true;
      }
    });
    return result;
  }


  /**
   * Get all entities of a particular type.
   *
   * @param urn
   *   The type urn to look up.
   *
   * @return A new Collection of entities of the specified type.
   */
  public Collection<StandardEntity> getEntitiesOfType(StandardEntityURN urn) {
    if (storedTypes.containsKey(urn)) {
      return storedTypes.get(urn);
    }
    indexClass(urn);
    return storedTypes.get(urn);
  }


  /**
   * Get all entities of a set of types.
   *
   * @param urns
   *   The type urns to look up.
   *
   * @return A new Collection of entities of the specified types.
   */
  public Collection<StandardEntity>
      getEntitiesOfType(StandardEntityURN... urns) {
    Collection<StandardEntity> result = new HashSet<StandardEntity>();
    for (StandardEntityURN urn : urns) {
      result.addAll(getEntitiesOfType(urn));
    }
    return result;
  }


  /**
   * Get the distance between two entities.
   *
   * @param first
   *   The ID of the first entity.
   * @param second
   *   The ID of the second entity.
   *
   * @return The distance between the two entities. A negative value indicates
   * that one or both objects either doesn't exist or could not be
   * located.
   */
  public int getDistance(EntityID first, EntityID second) {
    StandardEntity a = getEntity(first);
    StandardEntity b = getEntity(second);
    if (a == null || b == null) {
      return -1;
    }
    return getDistance(a, b);
  }


  /**
   * Get the distance between two entities.
   *
   * @param first
   *   The first entity.
   * @param second
   *   The second entity.
   *
   * @return The distance between the two entities. A negative value indicates
   * that one or both objects could not be located.
   */
  public int getDistance(StandardEntity first, StandardEntity second) {
    Pair<Integer, Integer> a = first.getLocation(this);
    Pair<Integer, Integer> b = second.getLocation(this);
    if (a == null || b == null) {
      return -1;
    }
    return distance(a, b);
  }


  /**
   * Get the world bounds.
   *
   * @return A Rectangle2D describing the bounds of the world.
   */
  public Rectangle2D getBounds() {
    if (!indexed) {
      index();
    }
    return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
  }


  /**
   * Get the world bounds.
   *
   * @return A pair of coordinates for the top left and bottom right corners.
   */
  public Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> getWorldBounds() {
    if (!indexed) {
      index();
    }
    Pair<Integer, Integer> topLeft = new Pair<Integer, Integer>(minX, minY);
    Pair<Integer, Integer> bottomRight = new Pair<Integer, Integer>(maxX, maxY);
    return new Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>(topLeft,
        bottomRight);
  }


  /**
   * Create a StandardWorldModel that wraps an existing world model. If the
   * existing model is already a StandardWorldModel then it will be returned
   * directly, otherwise a new StandardWorldModel will be created that contains
   * all the entities in the existing model that are instances of
   * StandardEntity. Changes to the existing world model will be reflected in
   * the returned StandardWorldModel.
   *
   * @param existing
   *   The existing world model to wrap. This may be null.
   *
   * @return The existing world model if it is an instance of
   * StandardWorldModel; a new model otherwise.
   */
  public static StandardWorldModel
      createStandardWorldModel(WorldModel<? extends Entity> existing) {
    if (existing instanceof StandardWorldModel) {
      return (StandardWorldModel) existing;
    } else {
      final StandardWorldModel result = new StandardWorldModel();
      if (existing != null) {
        result.addEntities(existing.getAllEntities());
        existing.addWorldModelListener(new WorldModelListener<Entity>() {

          @Override
          public void entityAdded(WorldModel<? extends Entity> model,
              Entity e) {
            result.addEntity(e);
          }


          @Override
          public void entityRemoved(WorldModel<? extends Entity> model,
              Entity e) {
            if (e instanceof StandardEntity) {
              result.removeEntity((StandardEntity) e);
            }
          }
        });
      }
      return result;
    }
  }


  private Rectangle makeRectangle(StandardEntity e) {
    int x1 = Integer.MAX_VALUE;
    int x2 = Integer.MIN_VALUE;
    int y1 = Integer.MAX_VALUE;
    int y2 = Integer.MIN_VALUE;
    if (e instanceof Area) {
      int[] apexes = ((Area) e).getApexList();
      if (apexes.length == 0) {
        return null;
      }
      for (int i = 0; i < apexes.length - 1; i += 2) {
        x1 = Math.min(x1, apexes[i]);
        x2 = Math.max(x2, apexes[i]);
        y1 = Math.min(y1, apexes[i + 1]);
        y2 = Math.max(y2, apexes[i + 1]);
      }
    } else if (e instanceof Blockade) {
      int[] apexes = ((Blockade) e).getApexes();
      if (apexes.length == 0) {
        return null;
      }
      for (int i = 0; i < apexes.length - 1; i += 2) {
        x1 = Math.min(x1, apexes[i]);
        x2 = Math.max(x2, apexes[i]);
        y1 = Math.min(y1, apexes[i + 1]);
        y2 = Math.max(y2, apexes[i + 1]);
      }
    } else if (e instanceof Human) {
      Human h = (Human) e;
      Pair<Integer, Integer> location = h.getLocation(this);
      if (location == null) {
        return null;
      }
      x1 = location.first();
      x2 = location.first();
      y1 = location.second();
      y2 = location.second();
    } else {
      return null;
    }
    return new Rectangle(x1, y1, x2, y2);
  }


  private int distance(Pair<Integer, Integer> a, Pair<Integer, Integer> b) {
    return distance(a.first(), a.second(), b.first(), b.second());
  }


  private int distance(int x1, int y1, int x2, int y2) {
    double dx = x1 - x2;
    double dy = y1 - y2;
    return (int) Math.hypot(dx, dy);
  }

  private class AddRemoveListener
      implements WorldModelListener<StandardEntity> {

    @Override
    public void entityAdded(WorldModel<? extends StandardEntity> model,
        StandardEntity e) {
      StandardEntityURN type = e.getStandardURN();
      if (storedTypes.containsKey(type)) {
        Collection<StandardEntity> bucket = storedTypes.get(type);
        bucket.add(e);
      }
      unindexedEntities.add(e);
    }


    @Override
    public void entityRemoved(WorldModel<? extends StandardEntity> model,
        StandardEntity e) {
      StandardEntityURN type = e.getStandardURN();
      if (storedTypes.containsKey(type)) {
        Collection<StandardEntity> bucket = storedTypes.get(type);
        bucket.remove(e);
      }
      unindexedEntities.remove(e);
    }
  }
}
