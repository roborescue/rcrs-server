package maps.convert.osm2gml;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * A generic spatial grid that can index any object implementing the SpatialIndexable interface.
 * @param <T> The type of object to be stored in the grid.
 */
public class SpatialGrid<T extends SpatialIndexable> {

    private final Map<GridPoint, Set<T>> grid;
    private final double cellWidth;
    private final double cellHeight;
    private final double minX;
    private final double minY;

    private record GridPoint(int x, int y) {}

    public SpatialGrid(Rectangle2D bounds, double cellSize) {
        if (bounds == null || bounds.isEmpty() || cellSize <= 0) {
            // Create a dummy grid if bounds are invalid
            this.minX = 0; this.minY = 0; this.cellWidth = 1; this.cellHeight = 1;
            this.grid = new HashMap<>();
            return;
        }
        this.minX = bounds.getMinX();
        this.minY = bounds.getMinY();
        this.cellWidth = cellSize;
        this.cellHeight = cellSize;
        this.grid = new HashMap<>();
    }

    /**
     * Registers a SpatialIndexable object into the grid.
     * @param item The object to add.
     */
    public void add(final T item) {
        forEachCell(item, (x, y) -> addToCell(x, y, item));
    }

    /**
     * Removes a {@link SpatialIndexable} object from the grid.
     * Empty cells are cleaned up immediately to avoid memory leaks.
     * @param item The object to remove.
     */
    public void remove(final T item) {
        forEachCell(item, (x, y) -> {
            final Set<T> cell = grid.get(new GridPoint(x, y));
            if (cell == null) return;
            cell.remove(item);
            if (cell.isEmpty()) {
                grid.remove(new GridPoint(x, y));
            }
        });
    }

    // Iterates over all grid cells covered by the bounding box of the given item
    // and applies the given action to each cell coordinate.
    // If the item's bounds are null or empty, no action is taken.
    private void forEachCell(final T item, final BiConsumer<Integer, Integer> action) {
        final Rectangle2D bounds = item.getBounds();
        if (hasInvalidBounds(bounds)) return;

        int minCellX = getXCell(bounds.getMinX());
        int minCellY = getYCell(bounds.getMinY());
        int maxCellX = getXCell(bounds.getMaxX());
        int maxCellY = getYCell(bounds.getMaxY());

        for (int x = minCellX; x <= maxCellX; x++) {
            for (int y = minCellY; y <= maxCellY; y++) {
                action.accept(x, y);
            }
        }
    }

    private boolean hasInvalidBounds(final Rectangle2D bounds) {
        return bounds == null || bounds.getWidth() < 0 || bounds.getHeight() < 0;
    }

    /**
     * Gets all objects that are potentially near the given object.
     * @param item The object to find neighbors for.
     * @return A Set of nearby objects.
     */
    public Set<T> getNearbyItems(T item) {
        Set<T> nearby = new HashSet<>();
        Rectangle2D bounds = item.getBounds();
        if (bounds == null || bounds.isEmpty()) return nearby;

        int minCellX = getXCell(bounds.getMinX()) - 1;
        int minCellY = getYCell(bounds.getMinY()) - 1;
        int maxCellX = getXCell(bounds.getMaxX()) + 1;
        int maxCellY = getYCell(bounds.getMaxY()) + 1;

        for (int x = minCellX; x <= maxCellX; x++) {
            for (int y = minCellY; y <= maxCellY; y++) {
                Set<T> cellContent = getFromCell(x, y);
                if (cellContent != null) {
                    nearby.addAll(cellContent);
                }
            }
        }
        return nearby;
    }

    private int getXCell(double x) { return (int) Math.floor((x - minX) / cellWidth); }
    private int getYCell(double y) { return (int) Math.floor((y - minY) / cellHeight); }

    private void addToCell(int x, int y, T item) {
        grid.computeIfAbsent(new GridPoint(x, y), k -> new HashSet<>()).add(item);
    }

    private Set<T> getFromCell(int x, int y) {
        return grid.get(new GridPoint(x, y));
    }
}
