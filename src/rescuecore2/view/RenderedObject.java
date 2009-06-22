package rescuecore2.view;

import java.awt.Shape;

public class RenderedObject {
    private Shape shape;
    private Object object;

    public RenderedObject(Object object, Shape shape) {
        this.object = object;
        this.shape = shape;
    }

    public Shape getShape() {
        return shape;
    }

    public Object getObject() {
        return object;
    }
}