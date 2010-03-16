package rescuecore2.standard.view;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.view.RenderedObject;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;

import rescuecore2.standard.entities.StandardEntity;

/**
   An abstract base class for StandardWorldModel view layers that render standard entities.
   @param <T> The subclass of StandardEntity that this layer knows how to render.
 */
public abstract class StandardEntityViewLayer<T extends StandardEntity> extends StandardViewLayer {
    /**
       The entities this layer should render.
    */
    protected List<T> entities;

    private Class<T> clazz;

    /**
       Construct a new StandardViewLayer.
       @param clazz The class of entity that this layer can render.
     */
    protected StandardEntityViewLayer(Class<T> clazz) {
        this.clazz = clazz;
        entities = new ArrayList<T>();
    }

    @Override
    public Rectangle2D view(Object... objects) {
        synchronized (entities) {
            entities.clear();
            preView();
            Rectangle2D result = super.view(objects);
            postView();
            return result;
        }
    }

    @Override
    protected void viewObject(Object o) {
        super.viewObject(o);
        if (clazz.isAssignableFrom(o.getClass())) {
            entities.add(clazz.cast(o));
        }
        if (o instanceof WorldModel) {
            WorldModel<? extends Entity> wm = (WorldModel<? extends Entity>)o;
            for (Entity next : wm) {
                viewObject(next);
            }
        }
    }

    @Override
    public Collection<RenderedObject> render(Graphics2D g, ScreenTransform transform, int width, int height) {
        synchronized (entities) {
            Collection<RenderedObject> result = new ArrayList<RenderedObject>();
            for (T next : entities) {
                result.add(new RenderedObject(next, render(next, g, transform)));
            }
            return result;
        }
    }

    /**
       Render an entity and return the shape. This shape is used for resolving mouse-clicks so should represent a hit-box for the entity.
       @param entity The entity to render.
       @param graphics The graphics to render on.
       @param transform A helpful coordinate transformer.
       @return A Shape that represents the hit-box of the rendered entity.
     */
    public abstract Shape render(T entity, Graphics2D graphics, ScreenTransform transform);

    /**
       Perform any pre-processing required before {@link #view} has been called.
    */
    protected void preView() {
    }

    /**
       Perform any post-processing required after {@link #view} has been called.
    */
    protected void postView() {
    }
}
