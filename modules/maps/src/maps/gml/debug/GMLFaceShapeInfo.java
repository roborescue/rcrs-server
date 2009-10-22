package maps.gml.debug;

import java.awt.Color;
import java.awt.Shape;
import java.awt.Polygon;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import java.util.List;

import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.misc.gui.ShapeDebugFrame;
import rescuecore2.misc.gui.DrawingTools;

import maps.gml.GMLFace;
import maps.gml.GMLCoordinates;
import maps.gml.GMLTools;

/**
   A ShapeInfo that knows how to draw GMLFaces.
*/
public class GMLFaceShapeInfo extends ShapeDebugFrame.ShapeInfo {
    private GMLFace face;
    private Color outlineColour;
    private Color fillColour;
    private boolean drawEdgeDirections;
    private Rectangle2D bounds;

    /**
       Create a new GMLFaceShapeInfo.
       @param face The face to draw.
       @param name The name of the face.
       @param outlineColour The colour to draw the outline of the face. This may be null to indicate that the outline should not be painted.
       @param fillColour The colour to draw the interior of the face. This may be null to indicate that the interior should not be painted.
       @param drawEdgeDirections Whether to draw edge directions or not.
     */
    public GMLFaceShapeInfo(GMLFace face, String name, Color outlineColour, Color fillColour, boolean drawEdgeDirections)  {
        super(face, name);
        this.face = face;
        this.outlineColour = outlineColour;
        this.fillColour = fillColour;
        this.drawEdgeDirections = drawEdgeDirections;
        if (face != null) {
            bounds = GMLTools.getBounds(face.getPoints());
        }
    }

    @Override
    public Shape paint(Graphics2D g, ScreenTransform transform) {
        if (face == null) {
            return null;
        }
        List<GMLCoordinates> coordinates = face.getPoints();
        int n = coordinates.size();
        int[] xs = new int[n];
        int[] ys = new int[n];
        int i = 0;
        for (GMLCoordinates next : coordinates) {
            xs[i] = transform.xToScreen(next.getX());
            ys[i] = transform.yToScreen(next.getY());
            ++i;
        }
        Polygon p = new Polygon(xs, ys, n);
        if (outlineColour != null) {
            g.setColor(outlineColour);
            g.draw(p);
            if (drawEdgeDirections) {
                for (i = 1; i < n; ++i) {
                    DrawingTools.drawArrowHeads(xs[i - 1], ys[i - 1], xs[i], ys[i], g);
                }
            }
        }
        if (fillColour != null) {
            g.setColor(fillColour);
            g.fill(p);
        }
        return p;
    }

    @Override
    public void paintLegend(Graphics2D g, int width, int height) {
        if (outlineColour != null) {
            g.setColor(outlineColour);
            g.drawRect(0, 0, width - 1, height - 1);
        }
        if (fillColour != null) {
            g.setColor(fillColour);
            g.fillRect(0, 0, width, height);
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
}