package maps.osm.debug;

import java.awt.Color;
import java.awt.Shape;
import java.awt.Polygon;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import java.util.List;

import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.misc.gui.ShapeDebugFrame;
import rescuecore2.misc.gui.DrawingTools;

import maps.osm.OSMWay;
import maps.osm.OSMMap;
import maps.osm.OSMNode;

/**
   A ShapeInfo that knows how to draw OSMWays.
*/
public class OSMWayShapeInfo extends ShapeDebugFrame.ShapeInfo {
    private OSMWay way;
    private OSMMap map;
    private Color colour;
    private boolean drawEdgeDirections;
    private Rectangle2D bounds;

    /**
       Create a new OSMWayShapeInfo.
       @param way The way to draw.
       @param map The map the way is part of.
       @param name The name of the way.
       @param colour The colour to draw the way.
       @param drawEdgeDirections Whether to draw edge directions or not.
     */
    public OSMWayShapeInfo(OSMWay way, OSMMap map, String name, Color colour, boolean drawEdgeDirections)  {
        super(way, name);
        this.way = way;
        this.map = map;
        this.colour = colour;
        this.drawEdgeDirections = drawEdgeDirections;
        if (way != null) {
            bounds = findBounds();
        }
    }

    @Override
    public Shape paint(Graphics2D g, ScreenTransform transform) {
        if (way == null) {
            return null;
        }
        List<Long> points = way.getNodeIDs();
        int n = points.size();
        int[] xs = new int[n];
        int[] ys = new int[n];
        int i = 0;
        for (long next : points) {
            xs[i] = transform.xToScreen(map.getNode(next).getLongitude());
            ys[i] = transform.yToScreen(map.getNode(next).getLatitude());
            ++i;
        }
        Polygon p = new Polygon(xs, ys, n);
        if (colour != null) {
            g.setColor(colour);
            g.draw(p);
            if (drawEdgeDirections) {
                for (i = 1; i < n; ++i) {
                    DrawingTools.drawArrowHeads(xs[i - 1], ys[i - 1], xs[i], ys[i], g);
                }
            }
        }
        return p;
    }

    @Override
    public void paintLegend(Graphics2D g, int width, int height) {
        if (colour != null) {
            g.setColor(colour);
            g.drawRect(0, 0, width - 1, height - 1);
        }
    }

    @Override
    public Rectangle2D getBoundsShape() {
        return bounds;
    }

    @Override
    public java.awt.geom.Point2D getBoundsPoint() {
        return null;
    }

    private Rectangle2D findBounds() {
        double xMin = Double.POSITIVE_INFINITY;
        double xMax = Double.NEGATIVE_INFINITY;
        double yMin = Double.POSITIVE_INFINITY;
        double yMax = Double.NEGATIVE_INFINITY;
        for (long next : way.getNodeIDs()) {
            OSMNode n = map.getNode(next);
            xMin = Math.min(xMin, n.getLongitude());
            xMax = Math.max(xMax, n.getLongitude());
            yMin = Math.min(yMin, n.getLatitude());
            yMax = Math.max(yMax, n.getLatitude());
        }
        return new Rectangle2D.Double(xMin, yMin, xMax - xMin, yMax - yMin);
    }
}
