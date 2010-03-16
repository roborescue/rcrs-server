package rescuecore2.view;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JMenuItem;

import java.util.Collection;
import java.util.List;

import rescuecore2.config.Config;
import rescuecore2.misc.gui.ScreenTransform;

/**
   A layer of a view. The LayerViewComponent is composed of a list of ViewLayer objects.
 */
public interface ViewLayer {
    /**
       Initialise this view layer.
       @param config The system configuration.
    */
    void initialise(Config config);

    /**
       Set the LayerViewComponent for this layer.
       @param component The LayerViewComponent that this layer is part of.
    */
    void setLayerViewComponent(LayerViewComponent component);

    /**
       Get the menu items this layer wants added to the LayerViewComponent popup menu.
       @return A list of menu items, or null if no menu items are required.
    */
    List<JMenuItem> getPopupMenuItems();

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

    /**
       Set whether this layer should be rendered or not.
       @param b True if this layer should be rendered, false otherwise.
    */
    void setVisible(boolean b);

    /**
       Find out if this layer should be rendered or not.
       @return True if this layer should be rendered, false otherwise.
    */
    boolean isVisible();

    /**
       Get the name of this layer.
       @return The name of the layer.
    */
    String getName();
}
