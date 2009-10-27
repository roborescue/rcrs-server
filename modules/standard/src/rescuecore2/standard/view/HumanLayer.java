package rescuecore2.standard.view;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import java.util.Comparator;
import java.util.Collections;

import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.misc.gui.ScreenTransform;

/**
   A view layer that renders humans.
 */
public class HumanLayer extends StandardEntityViewLayer<Human> {
    private static final int SIZE = 10;

    private static final int HP_MAX = 10000;
    private static final int HP_MEDIUM = 5000;
    private static final int HP_CRITICAL = 1000;

    /**
       Construct a human view layer.
     */
    public HumanLayer() {
        super(Human.class);
    }

    @Override
    public Shape render(Human h, Graphics2D g, ScreenTransform t) {
        Pair<Integer, Integer> location = h.getLocation(world);
        int x = t.xToScreen(location.first()) - SIZE / 2;
        int y = t.yToScreen(location.second()) - SIZE / 2;
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

    @Override
    protected void postView() {
        Collections.sort(entities, new HumanSorter());
    }

    private static class HumanSorter implements Comparator<Human>, java.io.Serializable {
        @Override
        public int compare(Human h1, Human h2) {
            if (h1 instanceof Civilian && !(h2 instanceof Civilian)) {
                return -1;
            }
            if (h2 instanceof Civilian && !(h1 instanceof Civilian)) {
                return 1;
            }
            return h1.getID().getValue() - h2.getID().getValue();
        }
    }
}