package rescuecore2.standard.view;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import rescuecore2.worldmodel.Entity;
import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.StandardWorldModel;

/**
   A class that knows how to render humans.
 */
public class HumanRenderer extends AbstractEntityRenderer {
    private static final int SIZE = 10;

    private static final int HP_MAX = 10000;
    private static final int HP_MEDIUM = 5000;
    private static final int HP_CRITICAL = 1000;

    private StandardWorldModel model;

    /**
       Construct a human renderer.
       @param model The world model.
     */
    public HumanRenderer(StandardWorldModel model) {
        super(Human.class);
        this.model = model;
    }

    @Override
    public Shape render(Entity e, Graphics2D g, ScreenTransform t) {
        Human h = (Human)e;
        Pair<Integer, Integer> location = h.getLocation(model);
        int x = t.scaleX(location.first()) - SIZE / 2;
        int y = t.scaleY(location.second()) - SIZE / 2;
        Shape shape = new Ellipse2D.Double(x, y, SIZE, SIZE);
        Color c = null;
        if (h instanceof Civilian) {
            c = Color.GREEN;
        }
        if (h instanceof FireBrigade) {
            c = Color.RED;
        }
        if (h instanceof PoliceForce) {
            c = Color.BLUE;
        }
        if (h instanceof AmbulanceTeam) {
            c = Color.WHITE;
        }
        if (c == null) {
            System.err.println("Can't handle " + h);
        }
        int hp = h.getHP();
        if (hp < HP_MAX) {
            c = c.darker();
        }
        if (hp < HP_MEDIUM) {
            c = c.darker();
        }
        if (hp < HP_CRITICAL) {
            c = c.darker();
        }
        if (hp <= 0) {
            c = Color.BLACK;
        }
        g.setColor(c);
        g.fill(shape);
        g.setColor(Color.WHITE);
        g.draw(shape);
        return shape;
    }
}