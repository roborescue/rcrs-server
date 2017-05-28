package rescuecore2.standard.view;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.BasicStroke;

import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Edge;
import rescuecore2.misc.gui.ScreenTransform;

/**
   A view layer that renders buildings.
 */
public class BuildingLayer extends AreaLayer<Building> {
    private static final Color HEATING = new Color(176, 176,  56, 128);
    private static final Color BURNING = new Color(204, 122,  50, 128);
    private static final Color INFERNO = new Color(160,  52,  52, 128);
    private static final Color WATER_DAMAGE = new Color(50, 120, 130, 128);
    private static final Color MINOR_DAMAGE = new Color(100, 140, 210, 128);
    private static final Color MODERATE_DAMAGE = new Color(100, 70, 190, 128);
    private static final Color SEVERE_DAMAGE = new Color(80, 60, 140, 128);
    private static final Color BURNT_OUT = new Color(0, 0, 0, 255);

    private static final Color OUTLINE_COLOUR = Color.GRAY.darker();
    private static final Color ENTRANCE = new Color(120, 120, 120);

    private static final Stroke WALL_STROKE = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    private static final Stroke ENTRANCE_STROKE = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

    /**
       Construct a building view layer.
     */
    public BuildingLayer() {
        super(Building.class);
    }

    @Override
    public String getName() {
        return "Building shapes";
    }

    @Override
    protected void paintEdge(Edge e, Graphics2D g, ScreenTransform t) {
        g.setColor(OUTLINE_COLOUR);
        g.setStroke(e.isPassable() ? ENTRANCE_STROKE : WALL_STROKE);
        g.drawLine(t.xToScreen(e.getStartX()),
                   t.yToScreen(e.getStartY()),
                   t.xToScreen(e.getEndX()),
                   t.yToScreen(e.getEndY()));
    }

    @Override
    protected void paintShape(Building b, Polygon shape, Graphics2D g) {
        drawBrokenness(b, shape, g);
        drawFieryness(b, shape, g);
    }

    private void drawFieryness(Building b, Polygon shape, Graphics2D g) {
        if (!b.isFierynessDefined()) {
            return;
        }
        switch (b.getFierynessEnum()) {
        case UNBURNT:
            return;
        case HEATING:
            g.setColor(HEATING);
            break;
        case BURNING:
            g.setColor(BURNING);
            break;
        case INFERNO:
            g.setColor(INFERNO);
            break;
        case WATER_DAMAGE:
            g.setColor(WATER_DAMAGE);
            break;
        case MINOR_DAMAGE:
            g.setColor(MINOR_DAMAGE);
            break;
        case MODERATE_DAMAGE:
            g.setColor(MODERATE_DAMAGE);
            break;
        case SEVERE_DAMAGE:
            g.setColor(SEVERE_DAMAGE);
            break;
        case BURNT_OUT:
            g.setColor(BURNT_OUT);
            break;
        default:
            throw new IllegalArgumentException("Don't know how to render fieryness " + b.getFierynessEnum());
        }
        g.fill(shape);
    }

    private void drawBrokenness(Building b, Shape shape, Graphics2D g) {
    	if (!b.isBrokennessDefined()) {
            return;
        }
        int brokenness = b.getBrokenness();
        // CHECKSTYLE:OFF:MagicNumber
        int colour = Math.max(0, 135 - brokenness / 2);
        // CHECKSTYLE:ON:MagicNumber
        g.setColor(new Color(colour, colour, colour));
        g.fill(shape);
    }
}
