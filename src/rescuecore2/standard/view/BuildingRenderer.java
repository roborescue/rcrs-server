package rescuecore2.standard.view;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Polygon;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.FireStation;
import rescuecore2.standard.entities.AmbulanceCentre;
import rescuecore2.standard.entities.PoliceOffice;
import rescuecore2.standard.entities.Node;
import rescuecore2.standard.entities.StandardWorldModel;

/**
   A class that knows how to render buildings.
 */
public class BuildingRenderer extends AbstractEntityRenderer {
    private static final int SLIGHTLY_BROKEN = 25;
    private static final int QUITE_BROKEN = 50;
    private static final int VERY_BROKEN = 75;

    private StandardWorldModel model;

    /**
       Construct a building renderer.
       @param model The world model.
     */
    public BuildingRenderer(StandardWorldModel model) {
        super(Building.class);
        this.model = model;
    }

    @Override
    public Shape render(Entity e, Graphics2D g, ScreenTransform t) {
        Building b = (Building)e;
        int[] apexes = b.getApexes();
        int count = apexes.length / 2;
        int[] xs = new int[count];
        int[] ys = new int[count];
        for (int i = 0; i < count; ++i) {
            xs[i] = t.scaleX(apexes[i * 2]);
            ys[i] = t.scaleY(apexes[(i * 2) + 1]);
        }
        Polygon shape = new Polygon(xs, ys, count);
        if (b instanceof Refuge) {
            g.setColor(Color.GREEN);
        }
        else if (b instanceof FireStation) {
            g.setColor(Color.PINK);
        }
        else if (b instanceof AmbulanceCentre) {
            g.setColor(Color.WHITE);
        }
        else if (b instanceof PoliceOffice) {
            g.setColor(Color.BLUE);
        }
        else {
            g.setColor(Color.GRAY);
        }
        switch (b.getFierynessEnum()) {
        case UNBURNT:
            break;
        case HEATING:
            g.setColor(Color.YELLOW);
            break;
        case BURNING:
            g.setColor(Color.ORANGE);
            break;
        case INFERNO:
            g.setColor(Color.RED);
            break;
        case WATER_DAMAGE:
            g.setColor(Color.CYAN);
            break;
        case MINOR_DAMAGE:
            g.setColor(Color.BLUE.brighter());
            break;
        case MODERATE_DAMAGE:
            g.setColor(Color.BLUE);
            break;
        case SEVERE_DAMAGE:
            g.setColor(Color.BLUE.darker());
            break;
        case BURNT_OUT:
            g.setColor(Color.BLACK);
            break;
        default:
            throw new IllegalArgumentException("Don't know how to render fieryness " + b.getFierynessEnum());
        }
        /*
        int brokenness = b.getBrokenness();
        if (brokenness > SLIGHTLY_BROKEN) {
            g.setColor(g.getColor().darker());
        }
        if (brokenness > QUITE_BROKEN) {
            g.setColor(g.getColor().darker());
        }
        if (brokenness > VERY_BROKEN) {
            g.setColor(g.getColor().darker());
        }
        */
        g.fill(shape);
        g.setColor(Color.BLACK);
        g.draw(shape);
        // Draw a line to each entrance
        int x = t.scaleX(b.getX());
        int y = t.scaleY(b.getY());
        g.setColor(Color.LIGHT_GRAY);
        for (EntityID next : b.getEntrances()) {
            Node n = (Node)model.getEntity(next);
            int nx = t.scaleX(n.getX());
            int ny = t.scaleY(n.getY());
            g.drawLine(x, y, nx, ny);
        }
        return shape;
    }
}