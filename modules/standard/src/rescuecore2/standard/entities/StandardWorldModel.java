package rescuecore2.standard.entities;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.EnumMap;
import java.util.List;
import java.util.ArrayList;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;
import java.awt.Shape;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.DefaultWorldModel;
import rescuecore2.worldmodel.WorldModelListener;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Entity;
import rescuecore2.misc.Pair;
import rescuecore2.log.Logger;

/**
   A wrapper around a WorldModel that indexes Entities by location.
 */
public class StandardWorldModel extends DefaultWorldModel<StandardEntity> {
    /** Config key entry for specifying the grid mesh size. */
    public static final String MESH_SIZE_KEY = "worldmodel.standard.mesh-size";

    /** The default mesh size. */
    public static final int DEFAULT_MESH_SIZE = 10000;

    private Map<StandardEntityURN, Collection<StandardEntity>> storedTypes;
    private Collection<StandardEntity> mobileEntities;
    private Collection<StandardEntity> staticEntities;
    private Collection<StandardEntity> unindexedEntities;

    private int meshSize;
    private int minX;
    private int maxX;
    private int minY;
    private int maxY;
    private List<List<Cell>> grid;
    private int gridWidth;
    private int gridHeight;
    private boolean indexed;

    /**
       Create a StandardWorldModel.
    */
    public StandardWorldModel() {
        super(StandardEntity.class);
        storedTypes = new EnumMap<StandardEntityURN, Collection<StandardEntity>>(StandardEntityURN.class);
        mobileEntities = new HashSet<StandardEntity>();
        staticEntities = new HashSet<StandardEntity>();
        unindexedEntities = new HashSet<StandardEntity>();
        addWorldModelListener(new AddRemoveListener());
        meshSize = DEFAULT_MESH_SIZE;
        indexed = false;
    }

    /**
       Tell this index to remember a certain class of entities.
       @param urns The type URNs to remember.
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
       Re-index the world model with a default mesh size.
    */
    public void index() {
        index(DEFAULT_MESH_SIZE);
    }

    /**
       Re-index the world model.
       @param newMeshSize The size of the mesh to create. A zero or negative value will result in a default mesh size being used.
    */
    public void index(int newMeshSize) {
        if (newMeshSize <= 0) {
            newMeshSize = DEFAULT_MESH_SIZE;
        }
        if (indexed && unindexedEntities.isEmpty() && meshSize == newMeshSize) {
            Logger.debug("Not bothering with reindex: new mesh size is same as old and no entities are currently unindexed");
            return;
        }
        this.meshSize = newMeshSize;
        Logger.debug("Re-indexing world model");
        mobileEntities.clear();
        staticEntities.clear();
        unindexedEntities.clear();
        // Find the bounds of the world first
        minX = Integer.MAX_VALUE;
        maxX = Integer.MIN_VALUE;
        minY = Integer.MAX_VALUE;
        maxY = Integer.MIN_VALUE;
        Pair<Integer, Integer> location;
        for (StandardEntity next : this) {
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
            if (next instanceof Area) {
                // Include apexes in the bounds
                Area a = (Area)next;
                if (a.isEdgesDefined()) {
                    List<Edge> edges = a.getEdges();
                    for (Edge edge : edges) {
                        minX = Math.min(minX, edge.getStartX());
                        minX = Math.min(minX, edge.getEndX());
                        maxX = Math.max(maxX, edge.getStartX());
                        maxX = Math.max(maxX, edge.getEndX());
                        minY = Math.min(minY, edge.getStartY());
                        minY = Math.min(minY, edge.getEndY());
                        maxY = Math.max(maxY, edge.getStartY());
                        maxY = Math.max(maxY, edge.getEndY());
                    }
                }
            }
        }
        // Now divide the world into a grid
        int width = maxX - minX;
        int height = maxY - minY;
        Logger.trace("World dimensions: " + minX + ", " + minY + " to " + maxX + ", " + maxY + " (width " + width + ", height " + height + ")");
        gridWidth = 1 + (int)Math.ceil(width / (double)meshSize);
        gridHeight = 1 + (int)Math.ceil(height / (double)meshSize);
        grid = new ArrayList<List<Cell>>(gridWidth);
        Logger.trace("Creating a mesh " + gridWidth + " cells wide and " + gridHeight + " cells high.");
        for (int i = 0; i < gridWidth; ++i) {
            List<Cell> list = new ArrayList<Cell>(gridHeight);
            grid.add(list);
            for (int j = 0; j < gridHeight; ++j) {
                Cell cell = new Cell(minX + (i * meshSize), minY + (j * meshSize), meshSize, meshSize);
                list.add(cell);
            }
        }
        for (StandardEntity next : staticEntities) {
            // Find all cells this entity covers
            if (next instanceof Area) {
                Logger.debug("Finding cells covered by " + next);
                for (Cell cell : getCellsCoveredBy(((Area)next).getShape())) {
                    cell.add(next);
                }
            }
            else if (next instanceof Blockade) {
                Logger.debug("Finding cells covered by " + next);
                for (Cell cell : getCellsCoveredBy(((Blockade)next).getShape())) {
                    cell.add(next);
                }
            }
            else {
                Logger.debug("Finding cell for " + next);
                location = next.getLocation(this);
                if (location != null) {
                    Cell cell = getCell(getXCell(location.first()), getYCell(location.second()));
                    cell.add(next);
                }
            }
        }
        int biggest = 0;
        for (int i = 0; i < gridWidth; ++i) {
            for (int j = 0; j < gridHeight; ++j) {
                biggest = Math.max(biggest, getCell(i, j).size());
            }
        }
        Logger.trace("Sorted " + staticEntities.size() + " objects. Biggest cell contains " + biggest + " objects.");
        indexed = true;
    }

    /**
       Get objects within a certain range of an entity.
       @param entity The entity to centre the search on.
       @param range The range to look up.
       @return A collection of StandardEntitys that are within range.
    */
    public Collection<StandardEntity> getObjectsInRange(StandardEntity entity, int range) {
        Pair<Integer, Integer> location = entity.getLocation(this);
        if (location == null) {
            return new HashSet<StandardEntity>();
        }
        return getObjectsInRange(location.first(), location.second(), range);
    }

    /**
       Get objects within a certain range of a location.
       @param x The x coordinate of the location.
       @param y The y coordinate of the location.
       @param range The range to look up.
       @return A collection of StandardEntitys that are within range.
    */
    public Collection<StandardEntity> getObjectsInRange(int x, int y, int range) {
        Collection<StandardEntity> result = new HashSet<StandardEntity>();
        int cellX = getXCell(x);
        int cellY = getYCell(y);
        int cellRange = range / meshSize;
        Shape visibleRange = new Ellipse2D.Double(x - range, y - range, range * 2, range * 2);
        for (int i = Math.max(0, cellX - cellRange); i <= Math.min(gridWidth - 1, cellX + cellRange); ++i) {
            for (int j = Math.max(0, cellY - cellRange); j <= Math.min(gridHeight - 1, cellY + cellRange); ++j) {
                Cell cell = getCell(i, j);
                for (StandardEntity next : cell.getEntities()) {
                    if (isInside(next, visibleRange)) {
                        result.add(next);
                    }
                }
            }
        }
        // Now do mobile and unindexed entities
        for (StandardEntity next : mobileEntities) {
            if (isInside(next, visibleRange)) {
                result.add(next);
            }
        }
        for (StandardEntity next : unindexedEntities) {
            if (isInside(next, visibleRange)) {
                result.add(next);
            }
        }
        return result;
    }

    /**
       Find out if any part of an entity is inside a shape.
       @param entity The entity to check.
       @param shape The shape to check.
       @return true if any part of the entity is inside the shape.
    */
    private boolean isInside(StandardEntity entity, Shape shape) {
        // If this is an area then check for intersection with the range ellipse
        if (entity instanceof Area) {
            java.awt.geom.Area area = new java.awt.geom.Area(shape);
            java.awt.geom.Area test = new java.awt.geom.Area(((Area)entity).getShape());
            area.intersect(test);
            return !area.isEmpty();
        }
        else if (entity instanceof Blockade) {
            java.awt.geom.Area area = new java.awt.geom.Area(shape);
            java.awt.geom.Area test = new java.awt.geom.Area(((Blockade)entity).getShape());
            area.intersect(test);
            return !area.isEmpty();
        }
        else {
            Pair<Integer, Integer> location = entity.getLocation(this);
            if (location != null) {
                int targetX = location.first();
                int targetY = location.second();
                return shape.contains(targetX, targetY);
            }
        }
        return false;
    }

    /**
       Get all entities of a particular type.
       @param urn The type urn to look up.
       @return A new Collection of entities of the specified type.
    */
    public Collection<StandardEntity> getEntitiesOfType(StandardEntityURN urn) {
        if (storedTypes.containsKey(urn)) {
            return storedTypes.get(urn);
        }
        indexClass(urn);
        return storedTypes.get(urn);
    }

    /**
       Get all entities of a set of types.
       @param urns The type urns to look up.
       @return A new Collection of entities of the specified types.
    */
    public Collection<StandardEntity> getEntitiesOfType(StandardEntityURN... urns) {
        Collection<StandardEntity> result = new HashSet<StandardEntity>();
        for (StandardEntityURN urn : urns) {
            result.addAll(getEntitiesOfType(urn));
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
        StandardEntity a = getEntity(first);
        StandardEntity b = getEntity(second);
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
    public int getDistance(StandardEntity first, StandardEntity second) {
        Pair<Integer, Integer> a = first.getLocation(this);
        Pair<Integer, Integer> b = second.getLocation(this);
        if (a == null || b == null) {
            return -1;
        }
        return distance(a, b);
    }

    /**
       Get the world bounds.
       @return A Rectangle2D describing the bounds of the world.
    */
    public Rectangle2D getBounds() {
        if (!indexed) {
            index();
        }
        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }

    /**
       Get the world bounds.
       @return A pair of coordinates for the top left and bottom right corners.
    */
    public Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> getWorldBounds() {
        if (!indexed) {
            index();
        }
        Pair<Integer, Integer> topLeft = new Pair<Integer, Integer>(minX, minY);
        Pair<Integer, Integer> bottomRight = new Pair<Integer, Integer>(maxX, maxY);
        return new Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>(topLeft, bottomRight);
    }

    /**
       Create a StandardWorldModel that wraps an existing world model. If the existing model is already a StandardWorldModel then it will be returned directly, otherwise a new StandardWorldModel will be created that contains all the entities in the existing model that are instances of StandardEntity. Changes to the existing world model will be reflected in the returned StandardWorldModel.
       @param existing The existing world model to wrap. This may be null.
       @return The existing world model if it is an instance of StandardWorldModel; a new model otherwise.
    */
    public static StandardWorldModel createStandardWorldModel(WorldModel<? extends Entity> existing) {
        if (existing instanceof StandardWorldModel) {
            return (StandardWorldModel)existing;
        }
        else {
            final StandardWorldModel result = new StandardWorldModel();
            if (existing != null) {
                result.addEntities(existing.getAllEntities());
                existing.addWorldModelListener(new WorldModelListener<Entity>() {
                        @Override
                        public void entityAdded(WorldModel<? extends Entity> model, Entity e) {
                            result.addEntity(e);
                        }

                        @Override
                        public void entityRemoved(WorldModel<? extends Entity> model, Entity e) {
                            if (e instanceof StandardEntity) {
                                result.removeEntity((StandardEntity)e);
                            }
                        }
                    });
            }
            return result;
        }
    }

    private int getXCell(int x) {
        return (x - minX) / meshSize;
    }

    private int getYCell(int y) {
        return (y - minY) / meshSize;
    }

    private Cell getCell(int x, int y) {
        return grid.get(x).get(y);
    }

    private int distance(Pair<Integer, Integer> a, Pair<Integer, Integer> b) {
        return distance(a.first(), a.second(), b.first(), b.second());
    }

    private int distance(int x1, int y1, int x2, int y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        return (int)Math.hypot(dx, dy);
    }

    private Set<Cell> getCellsCoveredBy(Shape shape) {
        Set<Cell> result = new HashSet<Cell>();
        for (int x = 0; x < gridWidth; ++x) {
            for (int y = 0; y < gridHeight; ++y) {
                Cell cell = getCell(x, y);
                Rectangle2D cellShape = cell.getShape();
                if (shape.intersects(cellShape)) {
                    result.add(cell);
                }
            }
        }
        return result;
    }

    private static class Cell {
        private Collection<StandardEntity> entities;
        private Rectangle2D shape;

        public Cell(int xMin, int yMin, int width, int height) {
            shape = new Rectangle2D.Double(xMin, yMin, width, height);
            entities = new HashSet<StandardEntity>();
        }

        public Collection<StandardEntity> getEntities() {
            return entities;
        }

        public int size() {
            return entities.size();
        }

        public void add(StandardEntity e) {
            entities.add(e);
        }

        public Rectangle2D getShape() {
            return shape;
        }
    }

    private class AddRemoveListener implements WorldModelListener<StandardEntity> {
        @Override
        public void entityAdded(WorldModel<? extends StandardEntity> model, StandardEntity e) {
            StandardEntityURN type = e.getStandardURN();
            if (storedTypes.containsKey(type)) {
                Collection<StandardEntity> bucket = storedTypes.get(type);
                bucket.add(e);
            }
            unindexedEntities.add(e);
        }

        @Override
        public void entityRemoved(WorldModel<? extends StandardEntity> model, StandardEntity e) {
            StandardEntityURN type = e.getStandardURN();
            if (storedTypes.containsKey(type)) {
                Collection<StandardEntity> bucket = storedTypes.get(type);
                bucket.remove(e);
            }
            unindexedEntities.remove(e);
        }
    }
}