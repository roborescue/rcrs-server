package rescuecore2.view;

import java.awt.Graphics;
import rescuecore2.worldmodel.Entity;

public interface EntityRenderer {
    public void render(Entity e, Graphics g);
}