package maps.gml.view;

import java.awt.Graphics2D;

import rescuecore2.misc.gui.ScreenTransform;

/**
   Interface for overlays that appear on the GML map viewer.
*/
public interface Overlay {
    /**
       Render this overlay.
       @param g The graphics to draw on.
       @param transform The current screen transform.
    */
    void render(Graphics2D g, ScreenTransform transform);
}