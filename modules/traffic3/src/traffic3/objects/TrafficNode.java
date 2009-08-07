package traffic3.objects;

import traffic3.manager.WorldManager;

/**
 *
 */
public class TrafficNode extends TrafficObject {

    private double locationX;
    private double locationY;
    private double locationZ;

    /**
     * Constructor.
     * @param wm world manager
     */
    public TrafficNode(WorldManager wm) {
        super(wm);
    }

    /**
     * Constructor.
     * @param wm world manager
     * @param id id
     */
    public TrafficNode(WorldManager wm, String id) {
        super(wm, id);
    }

    /**
     * check this object.
     */
    public void checkObject() {
        checked = true;
    }

    /**
     * set location.
     * @param x x
     * @param y y
     */
    public void setLocation(double x, double y) {
        setLocation(x, y, 0);
    }

    /**
     * set location.
     * @param x x
     * @param y y
     * @param z z
     */
    public void setLocation(double x, double y, double z) {
        locationX = x;
        locationY = y;
        locationZ = z;
        fireChanged();
    }

    /**
     * get x value.
     * @return x
     */
    public double getX() { return locationX; }

    /**
     * get y value.
     * @return y
     */
    public double getY() { return locationY; }

    /**
     * get z value.
     * @return z
     */
    public double getZ() { return locationZ; }

    /**
     * is same location.
     * @param tan node
     * @return same location
     */
    public boolean isSameLocation(TrafficNode tan) {
        return (getX() == tan.getX() && getY() == tan.getY() && getZ() == tan.getZ());
    }

    /**
     * get distance from a node.
     * @param node node
     * @return distance
     */
    public double getDistance(TrafficNode node) {
        return getDistance(node.getX(), node.getY(), node.getZ());
    }

    /**
     * get distance.
     * @param x x
     * @param y y
     * @param z z
     * @return distance
     */
    public double getDistance(double x, double y, double z) {
        double dx = x - locationX;
        double dy = y - locationY;
        double dz = z - locationZ;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
