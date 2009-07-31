package rescuecore2.view;

import java.awt.Graphics2D;
import java.util.Collection;

import rescuecore2.worldmodel.WorldModel;

/**
   A layer of a view. The WorldModelViewer is composed of a list of ViewLayer objects.
   @param <T> The subclass of WorldModel that this layer understands.
 */
public interface ViewLayer<T extends WorldModel> {
    /**
       Set the world model that is being viewed.
       @param world The new world model.
     */
    void setWorldModel(T world);

    /**
       Render this layer and return a collection of RenderedObjects.
       @param g The graphics to render to.
       @param width The width of the screen in pixels.
       @param height The height of the screen in pixels.
       @return A set of RenderedObjects representing the things that were actually rendered.
     */
    Collection<RenderedObject> render(Graphics2D g, int width, int height);
}