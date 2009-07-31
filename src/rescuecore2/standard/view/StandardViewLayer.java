package rescuecore2.standard.view;

import rescuecore2.standard.entities.StandardWorldModel;

import rescuecore2.view.AbstractViewLayer;

/**
   An abstract base class for StandardWorldModel view layers.
 */
public abstract class StandardViewLayer extends AbstractViewLayer<StandardWorldModel> {
    /**
       The current screen transform.
     */
    protected ScreenTransform transform;

    /**
       Construct a new StandardViewLayer.
     */
    protected StandardViewLayer() {
    }

    /**
       Set the screen transform for this view layer.
       @param t The new ScreenTransform.
     */
    public void setTransform(ScreenTransform t) {
        transform = t;
    }
}