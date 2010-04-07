package traffic3.objects;

/**
   Container for force information.
 */
public class Force {
    private double forceX;
    private double forceY;

    /**
       Construct a zero force.
    */
    public Force() {
        this(0, 0);
    }

    /**
       Construct a Force with given components.
       @param x The X component of this force.
       @param y The Y component of this force.
    */
    public Force(double x, double y) {
        forceX = x;
        forceY = y;
    }

    /**
       Get the X component of this force.
       @return The X component of the force.
    */
    public double getX() {
        return forceX;
    }

    /**
       Get the Y component of this force.
       @return The Y component of the force.
    */
    public double getY() {
        return forceY;
    }

    /**
       Add to this force.
       @param x The amount to add to the X component.
       @param y The amount to add to the Y component.
    */
    public void add(double x, double y) {
        forceX += x;
        forceY += y;
    }

    /**
       Add some other forces to this force.
       @param forces The forces to add.
    */
    public void add(Force... forces) {
        for (Force next : forces) {
            forceX += next.forceX;
            forceY += next.forceY;
        }
    }

    /**
       Zero this force.
    */
    public void clear() {
        forceX = 0;
        forceY = 0;
    }
}
