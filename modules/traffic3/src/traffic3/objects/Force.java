package traffic3.objects;

/**
   Container for force information.
 */
public class Force {
    private double forceX;
    private double forceY;
    private double forceZ;

    public Force() {
        forceX = 0;
        forceY = 0;
        forceZ = 0;
    }

    public Force(double x, double y) {
        this(x, y, 0);
    }

    public Force(double x, double y, double z) {
        forceX = x;
        forceY = y;
        forceZ = z;
    }

    public double getX() {
        return forceX;
    }

    public double getY() {
        return forceY;
    }

    public double getZ() {
        return forceZ;
    }

    public void add(double x, double y) {
        add(x, y, 0);
    }

    public void add(double x, double y, double z) {
        forceX += x;
        forceY += y;
        forceZ += z;
    }

    public void add(Force... forces) {
        for (Force next : forces) {
            forceX += next.forceX;
            forceY += next.forceY;
            forceZ += next.forceZ;
        }
    }

    public void clear() {
        forceX = 0;
        forceY = 0;
        forceZ = 0;
    }
}