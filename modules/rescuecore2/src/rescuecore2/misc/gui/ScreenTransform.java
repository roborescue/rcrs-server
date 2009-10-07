package rescuecore2.misc.gui;

/**
   A class that handles transforming between world coordinates and screen coordinates. This class should be used rather than affine transforms when you want to draw something in the right position (in world coordinates) but with a particular on-screen size. Affine transforms make it hard to control the output size.
 */
public class ScreenTransform {
    private double minX;
    private double minY;
    private double xRange;
    private double yRange;
    private double fixedX;
    private double fixedY;
    private int fixedScreenX;
    private int fixedScreenY;
    private int zoom;

    private double pixelsPerX;
    private double pixelsPerY;
    private int xOffset;
    private int yOffset;

    /**
       Create a ScreenTransform that covers a particular world coordinate range.
       @param minX The minimum X world coordinate.
       @param minY The minimum Y world coordinate.
       @param maxX The maximum X world coordinate.
       @param maxY The maximum Y world coordinate.
     */
    public ScreenTransform(double minX, double minY, double maxX, double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.xRange = maxX - minX;
        this.yRange = maxY - minY;
        this.fixedX = minX;
        this.fixedY = maxY;
        this.fixedScreenX = 0;
        this.fixedScreenY = 0;
        this.zoom = 1;
        //        System.out.println("New screen transform: minX = " + minX + ", minY = " + minY + ", xRange = " + xRange + ", yRange = " + yRange);
    }

    /**
       Fix a particular point on-screen.
       @param x The X world coordinate to fix.
       @param y The Y world coordinate to fix.
       @param screenX The screen X coordinate of the fixed world coordinate.
       @param screenY The screen Y coordinate of the fixed world coordinate.
     */
    public void setFixedPoint(double x, double y, int screenX, int screenY) {
        //        System.out.println("Setting fixed point " + x + ", " + y + " to " + screenX + ", " + screenY);
        fixedX = x;
        fixedY = y;
        fixedScreenX = screenX;
        fixedScreenY = screenY;
    }

    /**
       Increase the zoom level by one step.
     */
    public void zoomIn() {
        zoom *= 2;
    }

    /**
       Decrease the zoom level by one step. If this reduces the zoom level to one then any fixed point coordinate will be cleared.
     */
    public void zoomOut() {
        zoom /= 2;
        if (zoom <= 1) {
            zoom = 1;
            fixedX = minX;
            fixedY = minY + yRange;
            fixedScreenX = 0;
            fixedScreenY = 0;
        }
    }

    /**
       Recalculate the transform based on screen geometry.
       @param width The width of the screen.
       @param height The height of the screen.
     */
    public void rescale(int width, int height) {
        //        System.out.println("Rescaling to " + width + ", " + height);
        xOffset = 0;
        yOffset = height;
        pixelsPerX = (width / xRange) * zoom;
        pixelsPerY = (height / yRange) * zoom;
        // Work out how to offset points so that the fixed point is in the right place
        //        System.out.println("pixelsPerX = " + pixelsPerX + ", pixelsPerY = " + pixelsPerY);
        //        System.out.println("Before adjustment: fixed point " + fixedX + ", " + fixedY + " should be at " + fixedScreenX + ", " + fixedScreenY + "; actually at " + xToScreen(fixedX) + ", " + yToScreen(fixedY));
        int actualFixedX = xToScreen(fixedX);
        xOffset = fixedScreenX - actualFixedX;
        int actualFixedY = yToScreen(fixedY);
        yOffset = height + (fixedScreenY - actualFixedY);
        //        System.out.println("xOffset = " + xOffset + ", yOffset = " + yOffset);
        //        System.out.println("Fixed point now at " + xToScreen(fixedX) + ", " + yToScreen(fixedY));
    }

    /**
       Convert a world X coordinate to a screen coordinate.
       @param x The world X coordinate.
       @return The screen coordinate.
     */
    public int xToScreen(double x) {
        return xOffset + (int)((x - minX) * pixelsPerX);
    }

    /**
       Convert a world Y coordinate to a screen coordinate.
       @param y The world Y coordinate.
       @return The screen coordinate.
     */
    public int yToScreen(double y) {
        return yOffset - (int)((y - minY) * pixelsPerY);
    }

    /**
       Convert a screen X coordinate to a world coordinate.
       @param x The screen X coordinate.
       @return The world coordinate.
     */
    public double screenToX(int x) {
        return ((x - xOffset) / pixelsPerX) + minX;
    }

    /**
       Convert a screen Y coordinate to a world coordinate.
       @param y The screen Y coordinate.
       @return The world coordinate.
     */
    public double screenToY(int y) {
        return ((yOffset - y) / pixelsPerY) + minY;
    }
}
