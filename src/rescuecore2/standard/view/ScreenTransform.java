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

    public ScreenTransform(double xMin, double yMin, double xRange, double yRange, double width, double height) {
        this.xMin = xMin;
        this.yMin = yMin;
        this.xRange = xRange;
        this.yRange = yRange;
        this.width = width;
        this.height = height;
    }

    public int scaleX(int x) {
        return scale(x, xMin, xRange, width);
    }

    public int scaleY(int y) {
        return (int)(height - scale(y, yMin, yRange, height));
    }

    private int scale(double raw, double min, double inRange, double outRange) {
        double d = (raw - min) / inRange;
        return (int)(d * outRange);
    }
    
}