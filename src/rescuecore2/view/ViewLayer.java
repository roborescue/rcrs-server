package rescuecore2.view;

import java.awt.Graphics;
import java.util.Collection;

/**
   A layer of a view. The WorldModelViewer is composed of a list of ViewLayer objects.
 */
public interface ViewLayer {
    /**
       Render this layer and return a collection of RenderedObjects.
       @param g The graphics to render to.
       @param width The width of the screen in pixels.
       @param height The height of the screen in pixels.
       @return A set of RenderedObjects representing the things that were actually rendered.
     */
    Collection<RenderedObject> render(Graphics g, int width, int height);
}