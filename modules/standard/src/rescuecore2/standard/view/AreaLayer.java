package rescuecore2.standard.view;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Polygon;

import java.util.List;
import java.util.Iterator;

import rescuecore2.misc.gui.ScreenTransform;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Edge;

/**
   A view layer that renders areas.
   @param <E> The subclass of Area that this layer knows how to draw.
 */
public abstract class AreaLayer<E extends Area> extends StandardEntityViewLayer<E> {
    /**
       Construct an area view layer.
       @param clazz The subclass of Area this can render.
     */
    protected AreaLayer(Class<E> clazz) {
        super(clazz);
    }

    @Override
    public Shape render(E area, Graphics2D g, ScreenTransform t) {
        List<Edge> edges = area.getEdges();
        if (edges.isEmpty()) {
            return null;
        }
        int count = edges.size();
        int[] xs = new int[count];
        int[] ys = new int[count];
        int i = 0;
        for (Iterator<Edge> it = edges.iterator(); it.hasNext();) {
            Edge e = it.next();
            xs[i] = t.xToScreen(e.getStartX());
            ys[i] = t.yToScreen(e.getStartY());
            ++i;
        }
        Polygon shape = new Polygon(xs, ys, count);
        paintShape(area, shape, g);
        for (Edge edge : edges) {
            paintEdge(edge, g, t);
        }
        return shape;
    }

    /**
       Paint an individual edge.
       @param e The edge to paint.
       @param g The graphics to paint on.
       @param t The screen transform.
    */
    protected void paintEdge(Edge e, Graphics2D g, ScreenTransform t) {
    }

    /**
       Paint the overall shape.
       @param area The area.
       @param p The overall polygon.
       @param g The graphics to paint on.
    */
    protected void paintShape(E area, Polygon p, Graphics2D g) {
    }
}
