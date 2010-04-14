package maps.gml.editor;

import maps.gml.GMLCoordinates;

/**
   Class for snapping coordinates to a grid.
*/
public class Snap {
    private static final double DEFAULT_RESOLUTION = 0.001;

    private double resolution;
    private boolean enabled;

    /**
       Create a disabled Snap with default resolution.
    */
    public Snap() {
        resolution = DEFAULT_RESOLUTION;
        enabled = false;
    }

    /**
       Set the resolution.
       @param d The new resolution.
    */
    public void setResolution(double d) {
        resolution = d;
    }

    /**
       Get the resolution.
       @return The resolution.
    */
    public double getResolution() {
        return resolution;
    }

    /**
       Set whether to enable snapping.
       @param b Whether to enable snapping.
    */
    public void setEnabled(boolean b) {
        enabled = b;
    }

    /**
       Find out if this Snap is enabled.
       @return True if enabled.
    */
    public boolean isEnabled() {
        return enabled;
    }

    /**
       Snap a set of coordinates to the grid.
       @param c The coordinates to snap.
    */
    public void snap(GMLCoordinates c) {
        if (!enabled) {
            return;
        }
        double x = round(c.getX());
        double y = round(c.getY());
        c.setX(x);
        c.setY(y);
    }

    private double round(double d) {
        // CHECKSTYLE:OFF:MagicNumber
        return Math.floor((d / resolution) + 0.5) * resolution;
        // CHECKSTYLE:ON:MagicNumber
    }
}