package rescuecore2.view;

import java.awt.Graphics2D;
import java.util.Collection;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.messages.Command;
import rescuecore2.misc.gui.ScreenTransform;

/**
   A layer of a view. The LayerWorldModelViewer is composed of a list of ViewLayer objects.
 */
public interface ViewLayer {
    /**
       Render this layer and return a collection of RenderedObjects.
       @param g The graphics to render to.
       @param transform The ScreenTransform that will convert world coordinates to screen coordinates.
       @param width The width of the screen in pixels.
       @param height The height of the screen in pixels.
       @param world The world model.
       @param commands The commands.
       @param updates The updates.
       @return A set of RenderedObjects representing the things that were actually rendered.
     */
    Collection<RenderedObject> render(Graphics2D g, ScreenTransform transform, int width, int height, WorldModel<? extends Entity> world, Collection<Command> commands, Collection<Entity> updates);
}