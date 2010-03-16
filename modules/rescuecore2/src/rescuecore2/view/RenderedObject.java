package rescuecore2.view;

import java.awt.Shape;

/**
   A representation of something that has been rendered on screen.
 */
public class RenderedObject {
    private Shape shape;
    private Object object;

    /**
       Construct a new rendered object.
       @param object The thing that was rendered.
       @param shape The on-screen shape of the thing that was rendered.
     */
    public RenderedObject(Object object, Shape shape) {
        this.object = object;
        this.shape = shape;
    }

    /**
       Get the on-screen shape of the item that was rendered.
       @return The on-screen shape.
     */
    public Shape getShape() {
        return shape;
    }

    /**
       Get the item that was rendered.
       @return The item that was rendered.
     */
    public Object getObject() {
        return object;
    }

    @Override
    public String toString() {
        return object.toString();
    }
}
