package rescuecore2.standard.view;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Polygon;

import java.util.List;
import java.util.Iterator;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.FireStation;
import rescuecore2.standard.entities.AmbulanceCentre;
import rescuecore2.standard.entities.PoliceOffice;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Edge;
import rescuecore2.misc.gui.ScreenTransform;

/**
   A view layer that renders areas.
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
        int count = edges.size() + 1;
        int[] xs = new int[count];
        int[] ys = new int[count];
        Iterator<Edge> it = edges.iterator();
        Edge e = it.next();
        paintEdge(e, g, t);
        xs[0] = t.xToScreen(e.getStartX());
        ys[0] = t.yToScreen(e.getStartY());
        int i = 1;
        while (it.hasNext()) {
            e = it.next();
            xs[i] = t.xToScreen(e.getStartX());
            ys[i] = t.yToScreen(e.getStartY());
            ++i;
        }
        xs[i] = t.xToScreen(e.getEndX());
        ys[i] = t.yToScreen(e.getEndY());
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