package rescuecore2.standard.view;

import java.awt.geom.Rectangle2D;

import rescuecore2.config.Config;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.view.AbstractViewLayer;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;

import static rescuecore2.standard.view.StandardWorldModelViewer.STANDARD_VIEWER_PREFIX;

/**
   An abstract base class for StandardWorldModel view layers.
 */
public abstract class StandardViewLayer extends AbstractViewLayer {
    /** Default visibility setting for a layer. */
    public static final String VISIBILITY_SUFFIX = "visible";

    /**
       The StandardWorldModel to view.
     */
    protected StandardWorldModel world;

    /**
       Construct a new StandardViewLayer.
     */
    protected StandardViewLayer() {
    }

    @Override
    public void initialise(Config config) {
        String visibleKey = STANDARD_VIEWER_PREFIX + "." + this.getClass().getSimpleName() + "." + VISIBILITY_SUFFIX;
        boolean isVisible = config.getBooleanValue(visibleKey, isVisible());
        setVisible(isVisible);
    }

    @Override
    public Rectangle2D view(Object... objects) {
        processView(objects);
        if (world == null) {
            return null;
        }
        return world.getBounds();
    }

    @Override
    protected void viewObject(Object o) {
        if (o instanceof WorldModel) {
            world = StandardWorldModel.createStandardWorldModel((WorldModel<? extends Entity>)o);
        }
    }
}
