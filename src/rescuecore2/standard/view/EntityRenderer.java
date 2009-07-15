package rescuecore2.standard.view;

import java.awt.Graphics2D;
import java.awt.Shape;

import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntity;

/**
   Interface for rendering entities.
 */
public interface EntityRenderer {
    /**
       Can this renderer render objects of a particular class?
       @param clazz The class to check.
       @return true iff this renderer can render instances of the given class.
     */
    boolean canRender(Class<? extends StandardEntity> clazz);

    /**
       Render an entity and return the shape. This shape is used for resolving mouse-clicks so should represent a hit-box for the entity.
       @param e The entity to render.
       @param g The graphics to render on.
       @param t A helpful coordinate transformer.
       @param w The world model.
       @return A Shape that represents the hit-box of the rendered entity.
     */
    Shape render(StandardEntity e, Graphics2D g, ScreenTransform t, StandardWorldModel w);
}