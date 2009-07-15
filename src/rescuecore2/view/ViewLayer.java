package rescuecore2.view;

import java.awt.Graphics;
import java.util.Collection;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;

/**
   A layer of a view. The WorldModelViewer is composed of a list of ViewLayer objects.
 */
public interface ViewLayer {
    /**
       Set the world model that is being viewed.
       @param world The new world model.
     */
    void setWorldModel(WorldModel<? extends Entity> world);

    /**
       Render this layer and return a collection of RenderedObjects.
       @param g The graphics to render to.
       @param width The width of the screen in pixels.
       @param height The height of the screen in pixels.
       @return A set of RenderedObjects representing the things that were actually rendered.
     */
    Collection<RenderedObject> render(Graphics g, int width, int height);
}