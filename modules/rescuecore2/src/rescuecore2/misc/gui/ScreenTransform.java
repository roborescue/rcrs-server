package rescuecore2.misc.gui;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.awt.Shape;

/**
   A class that handles transforming between world coordinates and screen coordinates. This class should be used rather than affine transforms when you want to draw something in the right position (in world coordinates) but with a particular on-screen size. Affine transforms make it hard to control the output size.
 */
public class ScreenTransform {
    private double minX;
    private double minY;
    private double xRange;
    private double yRange;
    private double centreX;
    private double centreY;
    private double zoom;

    private double pixelsPerX;
    private double pixelsPerY;
    private int xOffset;
    private int yOffset;
    private int lastScreenWidth;
    private int lastScreenHeight;
    private Rectangle2D viewBounds;

    private boolean fixedAspectRatio;

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
        this.centreX = (minX + maxX) / 2;
        this.centreY = (minY + maxY) / 2;
        this.zoom = 1;
        fixedAspectRatio = true;
    }

    /**
       Set the on-screen centre point.
       @param x The X world coordinate to put in the centre.
       @param y The Y world coordinate to put in the centre.
    */
    public void setCentrePoint(double x, double y) {
        centreX = x;
        centreY = y;
        rescale(lastScreenWidth, lastScreenHeight);
    }

    /**
       Set the on-screen center point relative to another point.
       @param x The world X coordinate.
       @param y The world Y coordinate.
       @param screenX The screen X coordinate.
       @param screenY The screen Y coordinate.
    */
    public void makeCentreRelativeTo(double x, double y, int screenX, int screenY) {
        int dx = screenX - (lastScreenWidth / 2);
        int dy = screenY - (lastScreenHeight / 2);
        centreX = x - (dx / pixelsPerX);
        centreY = y + (dy / pixelsPerY);
        rescale(lastScreenWidth, lastScreenHeight);
    }

    /**
       Set whether to use a fixed aspect ratio or not.
       @param b True to use a fixed aspect ratio, false otherwise.
    */
    public void setFixedAspectRatio(boolean b) {
        fixedAspectRatio = b;
        rescale(lastScreenWidth, lastScreenHeight);
    }

    /**
       Increase the zoom level by one step.
    */
    public void zoomIn() {
        zoom *= 1.5d;
        rescale(lastScreenWidth, lastScreenHeight);
    }

    /**
       Decrease the zoom level by one step.
    */
    public void zoomOut() {
        zoom /= 1.5d;
        rescale(lastScreenWidth, lastScreenHeight);
    }

    /**
       Reset the zoom level to one. This also clears the fixed point coordinate.
    */
    public void resetZoom() {
        zoom = 1;
        centreX = minX + (xRange / 2);
        centreY = minY + (yRange / 2);
        rescale(lastScreenWidth, lastScreenHeight);
    }

    /**
       Zoom and translate to show a particular rectangle.
       @param bounds The bounds of the rectangle to show.
    */
    public void show(Rectangle2D bounds) {
        centreX = bounds.getMinX() + (bounds.getWidth() / 2);
        centreY = bounds.getMinY() + (bounds.getHeight() / 2);
        double xZoom = xRange / bounds.getWidth();
        double yZoom = yRange / bounds.getHeight();
        zoom = Math.min(xZoom, yZoom);
        rescale(lastScreenWidth, lastScreenHeight);
    }

    /**
       Recalculate the transform based on screen geometry.
       @param width The width of the screen.
       @param height The height of the screen.
    */
    public void rescale(int width, int height) {
        xOffset = 0;
        yOffset = height;
        pixelsPerX = (width / xRange) * zoom;
        pixelsPerY = (height / yRange) * zoom;
        if (fixedAspectRatio) {
            if (pixelsPerX < pixelsPerY) {
                pixelsPerY = pixelsPerX;
            }
            else if (pixelsPerY < pixelsPerX) {
                pixelsPerX = pixelsPerY;
            }
        }
        // Work out how to offset points so that the centre point is in the right place
        //        System.out.println("pixelsPerX = " + pixelsPerX + ", pixelsPerY = " + pixelsPerY);
        //        System.out.println("Before adjustment: fixed point " + fixedX + ", " + fixedY + " should be at " + fixedScreenX + ", " + fixedScreenY + "; actually at " + xToScreen(fixedX) + ", " + yToScreen(fixedY));
        int actualCentreX = xToScreen(centreX);
        xOffset = (width / 2) - actualCentreX;
        int actualCentreY = yToScreen(centreY);
        yOffset = height + (height / 2) - actualCentreY;
        //        System.out.println("xOffset = " + xOffset + ", yOffset = " + yOffset);
        //        System.out.println("Fixed point now at " + xToScreen(fixedX) + ", " + yToScreen(fixedY));
        lastScreenWidth = width;
        lastScreenHeight = height;
        double x1 = screenToX(0);
        double x2 = screenToX(width);
        double y1 = screenToY(height);
        double y2 = screenToY(0);
        viewBounds = new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
    }

    /**
       Find out if a shape is inside the current view bounds.
       @param s The shape to test.
       @return True if any part of the shape is inside the current view bounds, false otherwise.
    */
    public boolean isInView(Shape s) {
        if (s == null) {
            return false;
        }
        return s.intersects(viewBounds);
    }

    /**
       Find out if a point is inside the current view bounds.
       @param p The point to test.
       @return True if the point is inside the current view bounds, false otherwise.
    */
    public boolean isInView(Point2D p) {
        if (p == null) {
            return false;
        }
        return viewBounds.contains(p);
    }

    /**
       Get the current view bounds in world coordinates.
       @return The view bounds.
    */
    public Rectangle2D getViewBounds() {
        return viewBounds;
    }

    /**
       Convert a world X coordinate to a screen coordinate.
       @param x The world X coordinate.
       @return The screen coordinate.
    */
    public int xToScreen(double x) {
        return xOffset + (int)Math.round((x - minX) * pixelsPerX);
    }

    /**
       Convert a world Y coordinate to a screen coordinate.
       @param y The world Y coordinate.
       @return The screen coordinate.
    */
    public int yToScreen(double y) {
        return yOffset - (int)Math.round((y - minY) * pixelsPerY);
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
