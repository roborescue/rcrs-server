package maps.convert.osm2gml.debug;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import lombok.Getter;
import rescuecore2.misc.gui.DrawingTools;
import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.misc.gui.ShapeDebugFrame;

public class LineShapeInfo extends ShapeDebugFrame.ShapeInfo {
    private static final int THIN_WIDTH = 2;
    private static final int THICK_WIDTH = 6;
    private static final float DASH_LENGTH = 6.0f;

    private final rescuecore2.misc.geometry.Line2D line;
    private final Color color;
    private final boolean thick;
    private final boolean arrow;
    private final boolean dashed;
    @Getter private final Shape boundsShape;

    public LineShapeInfo(final Lineal line, final String name, final Color color,
                         final boolean thick, final boolean arrow, final boolean dashed)  {
        super(line, name);
        this.line = line.getLine();
        this.color = color;
        this.thick = thick;
        this.arrow = arrow;
        this.dashed = dashed;
        this.boundsShape = computeBounds(line.getLine());
    }

    public LineShapeInfo(final rescuecore2.misc.geometry.Line2D line, final String name, final Color color,
                         final boolean thick, final boolean arrow, final boolean dashed)  {
        super(line, name);
        this.line = line;
        this.color = color;
        this.thick = thick;
        this.arrow = arrow;
        this.dashed = dashed;
        this.boundsShape = computeBounds(line);
    }

    private static Shape computeBounds(final rescuecore2.misc.geometry.Line2D line) {
        final rescuecore2.misc.geometry.Point2D start = line.getOrigin();
        final rescuecore2.misc.geometry.Point2D end = line.getEndPoint();
        return new Line2D.Double(start.getX(), start.getY(), end.getX(), end.getY());
    }

    @Override
    public Shape paint(final Graphics2D g, final ScreenTransform transform) {
        g.setColor(color);

        final rescuecore2.misc.geometry.Point2D start = line.getOrigin();
        final rescuecore2.misc.geometry.Point2D end = line.getEndPoint();
        final int x1 = transform.xToScreen(start.getX());
        final int y1 = transform.yToScreen(start.getY());
        final int x2 = transform.xToScreen(end.getX());
        final int y2 = transform.yToScreen(end.getY());

        g.setStroke(createStroke());
        g.setColor(color);
        g.drawLine(x1, y1, x2, y2);
        if (arrow) DrawingTools.drawArrowHeads(x1, y1, x2, y2, g);

        final Path2D result = new Path2D.Double();
        result.moveTo(x1, y1);
        result.lineTo(x2, y2);
        return g.getStroke().createStrokedShape(result);
    }

    @Override
    public void paintLegend(final Graphics2D g, final int width, final int height) {
        g.setStroke(createStroke());
        g.setColor(color);
        g.drawLine(0, height / 2, width, height / 2);
        if (arrow) DrawingTools.drawArrowHeads(0, height / 2, width, height / 2, g);
    }

    private Stroke createStroke() {
        final int width = thick ? THICK_WIDTH : THIN_WIDTH;

        if (dashed) return new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL,
                width, new float[] {DASH_LENGTH, DASH_LENGTH}, 0);

        return new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
    }

    @Override
    public Point2D getBoundsPoint() {
        return null;
    }
}
