package rescuecore2.standard.view;

import java.awt.geom.Rectangle2D;

import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.view.AbstractViewLayer;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;

/**
   An abstract base class for StandardWorldModel view layers.
 */
public abstract class StandardViewLayer extends AbstractViewLayer {
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
