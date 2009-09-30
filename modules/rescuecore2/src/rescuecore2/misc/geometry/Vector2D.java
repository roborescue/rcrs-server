package rescuecore2.misc.geometry;

/**
   A vector in 2D space. Points are immutable.
 */
public class Vector2D {
    private double dx;
    private double dy;
    private double length;

    /**
       Create a new Vector2D.
       @param dx The x component.
       @param dy The y component.
    */
    public Vector2D(double dx, double dy) {
        this.dx = dx;
        this.dy = dy;
        length = Double.NaN;
    }

    /**
       Get the length of this vector.
       @return The length of the vector.
     */
    public double getLength() {
        if (Double.isNaN(length)) {
            length = Math.hypot(dx, dy);
        }
        return length;
    }

    /**
       Calculate the dot product of this vector and another vector.
       @param v The other vector.
       @return The dot product of this vector and the other vector.
     */
    public double dot(Vector2D v) {
        return dx * v.dx + dy * v.dy;
    }

    /**
       Get the X component of this vector.
       @return The X component.
     */
    public double getX() {
        return dx;
    }

    /**
       Get the Y component of this vector.
       @return The Y component.
    */
    public double getY() {
        return dy;
    }

    /**
       Create a new Vector2D by adding a vector to this one.
       @param v The vector to add.
       @return A new Vector2D.
    */
    public Vector2D add(Vector2D v) {
        return new Vector2D(dx + v.dx, dy + v.dy);
    }

    /**
       Create a scaled version of this vector.
       @param amount The scale factor.
       @return A new Vector2D.
     */
    public Vector2D scale(double amount) {
        return new Vector2D(dx * amount, dy * amount);
    }

    /**
       Create a normalised version of this vector.
       @return A new Vector2D with length 1.
     */
    public Vector2D normalised() {
        return scale(1.0 / getLength());
    }

    /**
       Get a Vector2D that is normal to this one.
       @return A new Vector2D that is normal to this one. This will be the "left-hand" normal.
     */
    public Vector2D getNormal() {
        return new Vector2D(-dy, dx);
    }

    @Override
    public String toString() {
        return dx + ", " + dy;
    }
}
