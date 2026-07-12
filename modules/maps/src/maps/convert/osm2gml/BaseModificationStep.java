package maps.convert.osm2gml;

import maps.convert.ConvertStep;
import maps.convert.osm2gml.debug.ObjectShapeInfo;
import rescuecore2.misc.gui.ShapeDebugFrame;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * An abstract base class for steps that modify collections of TemporaryObjects,
 * providing a common diff visualization logic.
 */
public abstract class BaseModificationStep extends ConvertStep {
    protected final TemporaryMap map;

    /**
     * Construct a BaseModificationStep.
     */
    public BaseModificationStep(TemporaryMap map) {
        super();
        this.map = map;
    }

    /**
     * Visualizes the difference between two collections of TemporaryObjects.
     * It highlights what was added, what was removed, and what was kept.
     * @param before The collection of objects before the modification.
     * @param after  The collection of objects after the modification.
     * @param title  The title for the debug view.
     * @param <T>    The type of TemporaryObject being compared.
     */
    protected <T extends TemporaryObject> void visualizeDifference(Collection<T> before, Collection<T> after, String title) {
        List<ShapeDebugFrame.ShapeInfo> shapes = new ArrayList<>();
        Set<T> beforeSet = new HashSet<>(before);
        Set<T> afterSet = new HashSet<>(after);

        final Color colourForOld  = new Color(229, 115, 115, 128); // Transparent red
        final Color colourForNew  = new Color(129, 199, 132, 128); // Transparent green
        final Color contextColour = new Color(200, 200, 200, 100); // Transparent grey

        // Draw all context objects in a natural colour.
        Collection<TemporaryObject> allObjects = new ArrayList<>(map.getAllObjects());
        for (TemporaryObject obj : allObjects) {
            shapes.add(new ObjectShapeInfo(obj, "Context", Color.DARK_GRAY, contextColour));
        }

        // Draw the items that were KEPT on the top, so they are not obscured.
        Set<T> kept = new HashSet<>(beforeSet);
        kept.retainAll(afterSet);
        for (T obj : kept) {
            shapes.add(new ObjectShapeInfo(obj, "Kept", Color.BLACK, Color.GRAY));
        }

        // Draw the items that were REMOVED.
        Set<T> removed = new HashSet<>(beforeSet);
        removed.removeAll(afterSet);
        for (T obj : removed) {
            shapes.add(new ObjectShapeInfo(obj, "Removed (Original)", Color.RED.darker(), colourForOld));
        }

        // Draw the items that were ADDED.
        Set<T> added = new HashSet<>(afterSet);
        added.removeAll(beforeSet);
        for (T obj : added) {
            shapes.add(new ObjectShapeInfo(obj, "Added", Color.GREEN.darker(), colourForNew));
        }

        debug.show(title, shapes);
    }
}
