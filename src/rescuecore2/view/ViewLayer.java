package rescuecore2.view;

import java.awt.Graphics;
import java.util.Collection;

public interface ViewLayer {
    public Collection<RenderedObject> render(Graphics g, int width, int height);
}