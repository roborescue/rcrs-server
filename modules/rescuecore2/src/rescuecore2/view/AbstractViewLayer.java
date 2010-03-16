package rescuecore2.view;

import java.util.Collection;
import java.util.List;

import javax.swing.JMenuItem;

import rescuecore2.config.Config;

/**
   An abstract ViewLayer implementation.
 */
public abstract class AbstractViewLayer implements ViewLayer {
    /** The LayerViewComponent this layer is part of. */
    protected LayerViewComponent component;

    private boolean visible;

    /**
       Construct a new, visible AbstractViewLayer.
    */
    protected AbstractViewLayer() {
        visible = true;
    }

    @Override
    public void setLayerViewComponent(LayerViewComponent c) {
        component = c;
    }

    @Override
    public List<JMenuItem> getPopupMenuItems() {
        return null;
    }

    @Override
    public void initialise(Config config) {
    }

    @Override
    public void setVisible(boolean b) {
        visible = b;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    /**
       Process a set of objects and recursively inspect collections and arrays.
       @param objects The objects to process.
    */
    protected void processView(Object... objects) {
        if (objects == null) {
            return;
        }
        for (Object next : objects) {
            process(next);
        }
    }

    /**
       Callback function for processing a concrete viewable object.
       @param o The object to process.
     */
    protected abstract void viewObject(Object o);

    private void process(Object o) {
        if (o == null) {
            return;
        }
        if (o instanceof Collection) {
            for (Object next : ((Collection)o)) {
                process(next);
            }
        }
        else if (o.getClass().isArray()) {
            for (Object next : (Object[])o) {
                process(next);
            }
        }
        else {
            viewObject(o);
        }
    }
}
