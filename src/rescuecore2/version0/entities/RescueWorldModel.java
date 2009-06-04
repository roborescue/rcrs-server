package rescuecore2.version0.entities;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import rescuecore2.worldmodel.DefaultWorldModel;
import rescuecore2.worldmodel.EntityType;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.misc.Pair;

import rescuecore2.version0.entities.RescueEntity;
import rescuecore2.version0.entities.Human;

/**
   A wrapper around a WorldModel that indexes Entities by location.
 */
public class RescueWorldModel extends DefaultWorldModel<RescueEntity> {
    private Map<EntityType, Collection<RescueEntity>> storedTypes;
    private Collection<RescueEntity> mobileEntities;
    private Collection<RescueEntity> staticEntities;

    private int meshSize;
    private int minX;
    private int maxX;
    private int minY;
    private int maxY;
    private List<List<Collection<RescueEntity>>> grid;
    private int gridWidth;
    private int gridHeight;

    /**
       Create an RescueWorldModel.
       @param meshSize The size of the mesh to create.
     */
    public RescueWorldModel(int meshSize) {
        super(RescueEntity.class);
        this.meshSize = meshSize;
        storedTypes = new HashMap<EntityType, Collection<RescueEntity>>();
        mobileEntities = new HashSet<RescueEntity>();
        staticEntities = new HashSet<RescueEntity>();
    }

    /**
       Tell this index to remember a certain class of entities.
       @param types The EntityTypes to remember.
     */
    public void indexClass(EntityType... types) {
        for (EntityType type : types) {
            Collection<RescueEntity> bucket = new HashSet<RescueEntity>();
            for (RescueEntity next : this) {
                if (next.getType().equals(type)) {
                    bucket.add(next);
                }
            }
            storedTypes.put(type, bucket);
        }
    }

    /**
       Re-index the world model.
     */
    public void index() {
        System.out.println("Re-indexing world model");
        mobileEntities.clear();
        staticEntities.clear();
        // Find the bounds of the world first
        minX = Integer.MAX_VALUE;
        maxX = Integer.MIN_VALUE;
        minY = Integer.MAX_VALUE;
        maxY = Integer.MIN_VALUE;
        Pair<Integer, Integer> location;
        for (RescueEntity next : this) {
            if (next instanceof Human) {
                mobileEntities.add(next);
            }
            else {
                staticEntities.add(next);
            }
            location = next.getLocation(this);
            if (location != null) {
                int x = location.first().intValue();
                int y = location.second().intValue();
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }
        }
        // Now divide the world into a grid
        int width = maxX - minX;
        int height = maxY - minY;
        System.out.println("World dimensions: " + minX + ", " + minY + " to " + maxX + ", " + maxY + " (width " + width + ", height " + height + ")");
        gridWidth = (int)Math.ceil(width / (double)meshSize);
        gridHeight = (int)Math.ceil(height / (double)meshSize);
        grid = new ArrayList<List<Collection<RescueEntity>>>(gridWidth);
        System.out.println("Creating a mesh " + gridWidth + " cells wide and " + gridHeight + " cells high.");
        for (int i = 0; i < gridWidth; ++i) {
            List<Collection<RescueEntity>> list = new ArrayList<Collection<RescueEntity>>(gridHeight);
            grid.add(list);
            for (int j = 0; j < gridHeight; ++j) {
                list.add(new HashSet<RescueEntity>());
            }
        }
        for (RescueEntity next : staticEntities) {
            location = next.getLocation(this);
            if (location != null) {
                Collection<RescueEntity> cell = getCell(getXCell(location.first().intValue()), getYCell(location.second().intValue()));
                cell.add(next);
            }
        }
        int biggest = 0;
        for (int i = 0; i < gridWidth; ++i) {
            for (int j = 0; j < gridHeight; ++j) {
                biggest = Math.max(biggest, getCell(i, j).size());
            }
        }
        System.out.println("Sorted " + staticEntities.size() + " objects. Biggest cell contains " + biggest + " objects.");
    }

    /**
       Get objects within a certain range of a location.
       @param x The x coordinate of the location.
       @param y The y coordinate of the location.
       @param range The range to look up.
       @return A collection of RescueEntitys that are within range.
     */
    public Collection<RescueEntity> getObjectsInRange(int x, int y, int range) {
        Collection<RescueEntity> result = new HashSet<RescueEntity>();
        int cellX = getXCell(x);
        int cellY = getYCell(y);
        int cellRange = range / meshSize;
        for (int i = Math.max(0, cellX - cellRange); i <= Math.min(gridWidth - 1, cellX + cellRange); ++i) {
            for (int j = Math.max(0, cellY - cellRange); j <= Math.min(gridHeight - 1, cellY + cellRange); ++j) {
                Collection<RescueEntity> cell = getCell(i, j);
                for (RescueEntity next : cell) {
                    Pair<Integer, Integer> location = next.getLocation(this);
                    if (location != null) {
                        int targetX = location.first().intValue();
                        int targetY = location.second().intValue();
                        int distance = distance(x, y, targetX, targetY);
                        if (distance <= range) {
                            result.add(next);
                        }
                    }
                }
            }
        }
        // Now do mobile entities
        for (RescueEntity next : mobileEntities) {
            Pair<Integer, Integer> location = next.getLocation(this);
            if (location != null) {
                int targetX = location.first().intValue();
                int targetY = location.second().intValue();
                int distance = distance(x, y, targetX, targetY);
                if (distance <= range) {
                    result.add(next);
                }
            }
        }
        return result;
    }

    /**
       Get all entities of a particular type.
       @param type The type to look up.
       @return A new Collection of entities of the specified type.
     */
    public Collection<RescueEntity> getEntitiesOfType(EntityType type) {
        if (storedTypes.containsKey(type)) {
            return storedTypes.get(type);
        }
        Collection<RescueEntity> result = new HashSet<RescueEntity>();
        for (RescueEntity next : this) {
            if (next.getType().equals(type)) {
                result.add(next);
            }
        }
        return result;
    }

    /**
       Get the distance between two entities.
       @param first The ID of the first entity.
       @param second The ID of the second entity.
       @return The distance between the two entities. A negative value indicates that one or both objects either doesn't exist or could not be located.
    */
    public int getDistance(EntityID first, EntityID second) {
        RescueEntity a = getEntity(first);
        RescueEntity b = getEntity(second);
        if (a == null || b == null) {
            return -1;
        }
        return getDistance(a, b);
    }

    /**
       Get the distance between two entities.
       @param first The first entity.
       @param second The second entity.
       @return The distance between the two entities. A negative value indicates that one or both objects could not be located.
    */
    public int getDistance(RescueEntity first, RescueEntity second) {
        Pair<Integer, Integer> a = first.getLocation(this);
        Pair<Integer, Integer> b = second.getLocation(this);
        if (a == null || b == null) {
            return -1;
        }
        return distance(a, b);
    }

    private int getXCell(int x) {
        return (x - minX) / meshSize;
    }

    private int getYCell(int y) {
        return (y - minY) / meshSize;
    }

    private Collection<RescueEntity> getCell(int x, int y) {
        return grid.get(x).get(y);
    }

    private int distance(Pair<Integer, Integer> a, Pair<Integer, Integer> b) {
        return distance(a.first().intValue(), a.second().intValue(), b.first().intValue(), b.second().intValue());
    }

    private int distance(int x1, int y1, int x2, int y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        return (int)Math.sqrt((dx * dx) + (dy * dy));
    }
}