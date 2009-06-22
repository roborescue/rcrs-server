package rescuecore2.standard.view;

import java.awt.Graphics2D;
import java.awt.Shape;

import rescuecore2.worldmodel.Entity;

public interface EntityRenderer {
    public boolean canRender(Class<?> clazz);
    public Shape render(Entity e, Graphics2D g, ScreenTransform t);
}