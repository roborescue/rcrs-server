package rescuecore2.standard.view;

/**
   Helper class for transforming world coordinates into screen coordinates.
 */
public class ScreenTransform {
    private double xMin;
    private double xRange;
    private double yMin;
    private double yRange;
    private double width;
    private double height;

    /**
       Construct a screen transform.
       @param xMin The smallest possible x-coordinate in world coordinates.
       @param yMin The smallest possible y-coordinate in world coordinates.
       @param xRange The range of x-coordinates in world coordinates.
       @param yRange The range of y-coordinates in world coordinates.
       @param width The width of the screen in pixels.
       @param height The height of the screen in pixels.
     */
    public ScreenTransform(double xMin, double yMin, double xRange, double yRange, double width, double height) {
        this.xMin = xMin;
        this.yMin = yMin;
        this.xRange = xRange;
        this.yRange = yRange;
        this.width = width;
        this.height = height;
    }

    /**
       Scale a world x-coordinate to a screen x-coordinate.
       @param x The world coordinate.
       @return The screen coordinate.
     */
    public int scaleX(int x) {
        return scale(x, xMin, xRange, width);
    }

    /**
       Scale a world y-coordinate to a screen y-coordinate.
       @param y The world coordinate.
       @return The screen coordinate.
     */
    public int scaleY(int y) {
        return (int)(height - scale(y, yMin, yRange, height));
    }

    private int scale(double raw, double min, double inRange, double outRange) {
        double d = (raw - min) / inRange;
        return (int)(d * outRange);
    }
}