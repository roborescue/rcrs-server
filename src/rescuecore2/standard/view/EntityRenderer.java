package rescuecore2.standard.view;

import java.awt.Graphics2D;
import java.awt.Shape;

import rescuecore2.worldmodel.Entity;

/**
   Interface for rendering entities.
 */
public interface EntityRenderer {
    /**
       Can this renderer render objects of a particular class?
       @param clazz The class to check.
       @return true iff this renderer can render instances of the given class.
     */
    boolean canRender(Class<?> clazz);

    /**
       Render an entity and return the shape. This shape is used for resolving mouse-clicks so should represent a hit-box for the entity.
       @param e The entity to render.
       @param g The graphics to render on.
       @param t A helpful coordinate transformer.
       @return A Shape that represents the hit-box of the rendered entity.
     */
    Shape render(Entity e, Graphics2D g, ScreenTransform t);
}