package maps.convert.osm2gml;

import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * A spatial grid for indexing {@link SpatialIndexable} objects.
 *
 * @param <T> the type of object stored in this grid
 */
public class SpatialGrid<T extends SpatialIndexable> {

    private final Map<GridPoint, Set<T>> grid;
    private final double cellWidth;
    private final double cellHeight;
    private final double minX;
    private final double minY;

    private record GridPoint(int x, int y) {}

    /**
     * Constructs a spatial grid with the specified bounds and cell size.
     *
     * @param bounds the bounds of the grid
     * @param cellSize the size of each grid cell
     * @throws NullPointerException if {@code bounds} is {@code null}
     * @throws IllegalArgumentException if {@code bounds} is empty or
     *         {@code cellSize} is not positive
     */
    public SpatialGrid(Rectangle2D bounds, double cellSize) {
        Objects.requireNonNull(bounds, "bounds");

        if (bounds.isEmpty()) {
            throw new IllegalArgumentException("bounds must not be empty");
        }
        if (cellSize <= 0) {
            throw new IllegalArgumentException("cellSize must be positive");
        }

        this.minX = bounds.getMinX();
        this.minY = bounds.getMinY();
        this.cellWidth = cellSize;
        this.cellHeight = cellSize;
        this.grid = new HashMap<>();
    }

    /**
     * Adds the specified object to this grid.
     *
     * @param item the object to add
     */
    public void add(T item) {
        forEachCell(item, (x, y) -> addToCell(x, y, item));
    }

    /**
     * Removes the specified object from this grid.
     *
     * @param item the object to remove
     */
    public void remove(T item) {
        forEachCell(item, (x, y) -> {
            Set<T> cell = grid.get(new GridPoint(x, y));
            if (cell == null) return;
            cell.remove(item);
            if (cell.isEmpty()) {
                grid.remove(new GridPoint(x, y));
            }
        });
    }

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
     * Returns the objects contained in the grid cells surrounding the specified
     * object.
     *
     * @param item the object to find nearby for
     * @return the objects contained in the surrounding grid cells
     */
    public Set<T> getNearbyItems(T item) {
        Set<T> nearbyItems = new HashSet<>();
        Rectangle2D bounds = item.getBounds();

        int minCellX = getXCell(bounds.getMinX()) - 1;
        int minCellY = getYCell(bounds.getMinY()) - 1;
        int maxCellX = getXCell(bounds.getMaxX()) + 1;
        int maxCellY = getYCell(bounds.getMaxY()) + 1;

        for (int x = minCellX; x <= maxCellX; x++) {
            for (int y = minCellY; y <= maxCellY; y++) {
                Set<T> cellItems = getCellItems(x, y);
                if (cellItems != null) {
                    nearbyItems.addAll(cellItems);
                }
            }
        }
        return nearbyItems;
    }

    private int getXCell(double x) { return (int) Math.floor((x - minX) / cellWidth); }
    private int getYCell(double y) { return (int) Math.floor((y - minY) / cellHeight); }

    private void addToCell(int x, int y, T item) {
        grid.computeIfAbsent(new GridPoint(x, y), k -> new HashSet<>()).add(item);
    }

    private Set<T> getCellItems(int x, int y) {
        return grid.get(new GridPoint(x, y));
    }
}
