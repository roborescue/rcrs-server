package rescuecore2.view;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import rescuecore2.misc.gui.ScreenTransform;

/**
   A layer of a view. The LayerViewComponent is composed of a list of ViewLayer objects.
 */
public interface ViewLayer {
    /**
       Set the list of objects this layer should view. The layer should record the objects it understands and draw them when {@link #render(Graphics2D, ScreenTransform, int, int)} is called.
       @param objects The objects to view.
       @return A Rectangle2D with the bounds of the area this layer wants to draw. This may be null if this layer has nothing to draw.
     */
    Rectangle2D view(Object... objects);

    /**
       Render this layer and return a collection of RenderedObjects.
       @param g The graphics to render to.
       @param transform The ScreenTransform that will convert world coordinates to screen coordinates.
       @param width The width of the screen in pixels.
       @param height The height of the screen in pixels.
       @return A set of RenderedObjects representing the things that were actually rendered.
     */
    Collection<RenderedObject> render(Graphics2D g, ScreenTransform transform, int width, int height);
}