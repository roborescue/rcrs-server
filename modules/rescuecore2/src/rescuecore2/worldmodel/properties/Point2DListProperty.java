package rescuecore2.worldmodel.properties;

import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.writeInt32;
import static rescuecore2.misc.EncodingTools.readDouble;
import static rescuecore2.misc.EncodingTools.writeDouble;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.Arrays;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.AbstractProperty;
import rescuecore2.misc.geometry.Point2D;

/**
   An property that is a list of Point2D objects.
 */
public class Point2DListProperty extends AbstractProperty {
    private List<Point2D> data;

    /**
       Construct a Point2DListProperty with no defined value.
       @param urn The urn of this property.
    */
    public Point2DListProperty(String urn) {
        super(urn);
        data = new ArrayList<Point2D>();
    }

    /**
       Construct a Point2DListProperty with no defined value.
       @param urn The urn of this property.
    */
    public Point2DListProperty(Enum<?> urn) {
        super(urn);
        data = new ArrayList<Point2D>();
    }

    /**
       Construct a Point2DListProperty with a defined value.
       @param urn The urn of this property.
       @param values The initial values of the property.
    */
    public Point2DListProperty(String urn, Point2D... values) {
        super(urn, true);
        data = new ArrayList<Point2D>(values.length);
        for (Point2D next : values) {
            data.add(next);
        }
    }

    /**
       Construct a Point2DListProperty with a defined value.
       @param urn The urn of this property.
       @param values The initial values of the property.
    */
    public Point2DListProperty(Enum<?> urn, Point2D... values) {
        super(urn, true);
        data = new ArrayList<Point2D>(values.length);
        for (Point2D next : values) {
            data.add(next);
        }
    }

    /**
       Point2DListProperty copy constructor.
       @param other The Point2DListProperty to copy.
     */
    public Point2DListProperty(Point2DListProperty other) {
        super(other);
        this.data = new ArrayList<Point2D>(other.data);
    }

    @Override
    public List<Point2D> getValue() {
        if (!isDefined()) {
            return null;
        }
        return new ArrayList<Point2D>(data);
    }

    /**
       Set the value of this property. Future calls to {@link #isDefined()} will return true.
       @param values The new values.
    */
    public void setValue(Collection<Point2D> values) {
        this.data = new ArrayList<Point2D>(values);
        setDefined();
    }

    /**
       Set the value of this property. Future calls to {@link #isDefined()} will return true.
       @param values The new values.
    */
    public void setValue(Point2D... values) {
        setValue(Arrays.asList(values));
    }

    /**
       Add a point to the array.
       @param p The point to add.
    */
    public void push(Point2D p) {
        setDefined();
        data.add(p);
    }

    @Override
    public void takeValue(Property p) {
        if (p instanceof Point2DListProperty) {
            Point2DListProperty i = (Point2DListProperty)p;
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
        writeInt32(data.size(), out);
        for (Point2D next : data) {
            writeDouble(next.getX(), out);
            writeDouble(next.getY(), out);
        }
    }

    @Override
    public void read(InputStream in) throws IOException {
        int size = readInt32(in);
        data.clear();
        for (int i = 0; i < size; ++i) {
            double x = readDouble(in);
            double y = readDouble(in);
            push(new Point2D(x, y));
        }
    }

    @Override
    public Point2DListProperty copy() {
        return new Point2DListProperty(this);
    }
}