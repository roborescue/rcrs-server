package traffic3.manager;

public class RTreeRectangle<T extends traffic3.objects.TrafficObject> extends com.infomatiq.jsi.Rectangle {
    private T obj;
    private int id;
    private static int totalcounter = 0;

    public RTreeRectangle(T o, float x1, float y1, float x2, float y2) {
        super(x1, y1, x2, y2);
        obj = o;
        id = ++totalcounter;
    }

    public T getObject() {
        return obj;
    }

    public int getID() {
        return id;
    }
}
