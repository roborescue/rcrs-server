package rescuecore2.worldmodel.properties;

import static rescuecore2.misc.EncodingTools.readDouble;
import static rescuecore2.misc.EncodingTools.writeDouble;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.AbstractProperty;
import rescuecore2.misc.geometry.Point2D;

/**
   A Point2D property.
 */
public class Point2DProperty extends AbstractProperty {
    private Point2D value;

    /**
       Construct a Point2DProperty with no defined value.
       @param urn The urn of this property.
    */
    public Point2DProperty(String urn) {
        super(urn);
    }

    /**
       Construct a Point2DProperty with no defined value.
       @param urn The urn of this property.
    */
    public Point2DProperty(Enum<?> urn) {
        super(urn);
    }

    /**
       Construct a Point2DProperty with a defined value.
       @param urn The urn of this property.
       @param value The initial value of the property.
    */
    public Point2DProperty(String urn, Point2D value) {
        super(urn, true);
        this.value = value;
    }

    /**
       Construct a Point2DProperty with a defined value.
       @param urn The urn of this property.
       @param value The initial value of the property.
    */
    public Point2DProperty(Enum<?> urn, Point2D value) {
        super(urn, true);
        this.value = value;
    }

    /**
       Point2DProperty copy constructor.
       @param other The Point2DProperty to copy.
     */
    public Point2DProperty(Point2DProperty other) {
        super(other);
        this.value = other.value;
    }

    @Override
    public Point2D getValue() {
        if (!isDefined()) {
            return null;
        }
        return value;
    }

    /**
       Set the value of this property. Future calls to {@link #isDefined()} will return true.
       @param value The new value.
    */
    public void setValue(Point2D value) {
        this.value = value;
        setDefined();
    }

    @Override
    public void takeValue(Property p) {
        if (p instanceof Point2DProperty) {
            Point2DProperty i = (Point2DProperty)p;
            if (i.isDefined()) {
                setValue(i.getValue());
            }
            else {
                undefine();
            }
        }
        else {
            throw new IllegalArgumentException(this + " cannot take value from " + p);
        }
    }

    @Override
    public void write(OutputStream out) throws IOException {
        writeDouble(value.getX(), out);
        writeDouble(value.getY(), out);
    }

    @Override
    public void read(InputStream in) throws IOException {
        double x = readDouble(in);
        double y = readDouble(in);
        setValue(new Point2D(x, y));
    }

    @Override
    public Point2DProperty copy() {
        return new Point2DProperty(this);
    }
}
