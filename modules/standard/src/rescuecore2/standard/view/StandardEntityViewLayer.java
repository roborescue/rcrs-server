package rescuecore2.standard.view;

import java.awt.Graphics2D;
import java.awt.Shape;

import java.util.Collection;
import java.util.ArrayList;

import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.view.RenderedObject;
import rescuecore2.view.ViewLayer;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.messages.Command;

/**
   An abstract base class for StandardWorldModel view layers that render standard entities.
   @param <T> The subclass of StandardEntity that this layer knows how to render.
 */
public abstract class StandardEntityViewLayer<T extends StandardEntity> implements ViewLayer {
    private Class<T> clazz;

    /**
       Construct a new StandardViewLayer.
       @param clazz The class of entity that this layer can render.
     */
    protected StandardEntityViewLayer(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Collection<RenderedObject> render(Graphics2D g, ScreenTransform transform, int width, int height, WorldModel<? extends Entity> world, Collection<Command> commands, Collection<Entity> updates) {
        Collection<RenderedObject> result = new ArrayList<RenderedObject>();
        StandardWorldModel model = StandardWorldModel.createStandardWorldModel(world);
        for (Entity next : model) {
            if (clazz.isAssignableFrom(next.getClass())) {
                result.add(new RenderedObject(next, render(clazz.cast(next), g, transform, model)));
            }
        }
        return result;
    }

    /**
       Render an entity and return the shape. This shape is used for resolving mouse-clicks so should represent a hit-box for the entity.
       @param entity The entity to render.
       @param graphics The graphics to render on.
       @param transform A helpful coordinate transformer.
       @param world The world model.
       @return A Shape that represents the hit-box of the rendered entity.
     */
    public abstract Shape render(T entity, Graphics2D graphics, ScreenTransform transform, StandardWorldModel world);
}