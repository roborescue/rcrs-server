package rescuecore2.standard.view;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Polygon;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Node;
import rescuecore2.misc.gui.ScreenTransform;

/**
   A view layer that renders buildings.
 */
public class BuildingLayer extends StandardEntityViewLayer<Building> {
    private static final Color HEATING = new Color(176, 176,  56, 128);
    private static final Color BURNING = new Color(204, 122,  50, 128);
    private static final Color INFERNO = new Color(160,  52,  52, 128);
    private static final Color WATER_DAMAGE = new Color(50, 120, 130, 128);
    private static final Color MINOR_DAMAGE = new Color(100, 140, 210, 128);
    private static final Color MODERATE_DAMAGE = new Color(100, 70, 190, 128);
    private static final Color SEVERE_DAMAGE = new Color(80, 60, 140, 128);
    private static final Color BURNT_OUT = new Color(60, 50, 90, 128);

    private static final Color OUTLINE = Color.GRAY.darker();
    private static final Color ENTRANCE = new Color(120, 120, 120);

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
    public Shape render(Building b, Graphics2D g, ScreenTransform t) {
        int[] apexes = b.getApexes();
        int count = apexes.length / 2;
        int[] xs = new int[count];
        int[] ys = new int[count];
        for (int i = 0; i < count; ++i) {
            xs[i] = t.xToScreen(apexes[i * 2]);
            ys[i] = t.yToScreen(apexes[(i * 2) + 1]);
        }
        Polygon shape = new Polygon(xs, ys, count);
        drawBrokenness(b, shape, g);
        drawFieryness(b, shape, g);
        g.setColor(OUTLINE);
        g.draw(shape);
        // Draw a line to each entrance
        int x = t.xToScreen(b.getX());
        int y = t.yToScreen(b.getY());
        g.setColor(ENTRANCE);
        for (EntityID next : b.getEntrances()) {
            Node n = (Node)world.getEntity(next);
            int nx = t.xToScreen(n.getX());
            int ny = t.yToScreen(n.getY());
            g.drawLine(x, y, nx, ny);
        }
        return shape;
    }

    private void drawFieryness(Building b, Polygon shape, Graphics2D g) {
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
        int brokenness = b.getBrokenness();
        // CHECKSTYLE:OFF:MagicNumber
        int colour = Math.max(0, 135 - brokenness / 4);
        // CHECKSTYLE:ON:MagicNumber
        g.setColor(new Color(colour, colour, colour));
        g.fill(shape);
    }
}