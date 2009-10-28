package rescuecore2.standard.view;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.ImageIcon;
import javax.swing.Icon;

import java.util.Comparator;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.misc.Pair;
import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Civilian;

/**
   A view layer that renders humans.
 */
public class HumanLayer extends StandardEntityViewLayer<Human> {
    private static final int SIZE = 10;

    private static final int HP_MAX = 10000;
    private static final int HP_MEDIUM = 5000;
    private static final int HP_CRITICAL = 1000;

    private static final int ICON_SIZE = 32;

    private static final ImageIcon FIRE_BRIGADE = new ImageIcon(HumanLayer.class.getClassLoader().getResource("rescuecore2/standard/view/FireBrigade-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
    private static final ImageIcon FIRE_BRIGADE_DEAD = new ImageIcon(HumanLayer.class.getClassLoader().getResource("rescuecore2/standard/view/FireBrigade-Dead-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
    private static final ImageIcon POLICE_FORCE = new ImageIcon(HumanLayer.class.getClassLoader().getResource("rescuecore2/standard/view/PoliceForce-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
    private static final ImageIcon POLICE_FORCE_DEAD = new ImageIcon(HumanLayer.class.getClassLoader().getResource("rescuecore2/standard/view/PoliceForce-Dead-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
    private static final ImageIcon AMBULANCE_TEAM = new ImageIcon(HumanLayer.class.getClassLoader().getResource("rescuecore2/standard/view/AmbulanceTeam-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
    private static final ImageIcon AMBULANCE_TEAM_DEAD = new ImageIcon(HumanLayer.class.getClassLoader().getResource("rescuecore2/standard/view/AmbulanceTeam-Dead-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
    private static final ImageIcon CIVILIAN_MALE = new ImageIcon(HumanLayer.class.getClassLoader().getResource("rescuecore2/standard/view/Civilian-Male-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
    private static final ImageIcon CIVILIAN_MALE_DEAD = new ImageIcon(HumanLayer.class.getClassLoader().getResource("rescuecore2/standard/view/Civilian-Male-Dead-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
    private static final ImageIcon CIVILIAN_FEMALE = new ImageIcon(HumanLayer.class.getClassLoader().getResource("rescuecore2/standard/view/Civilian-Female-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
    private static final ImageIcon CIVILIAN_FEMALE_DEAD = new ImageIcon(HumanLayer.class.getClassLoader().getResource("rescuecore2/standard/view/Civilian-Female-Dead-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));

    private Set<EntityID> male;
    private Set<EntityID> female;

    /**
       Construct a human view layer.
     */
    public HumanLayer() {
        super(Human.class);
        male = new HashSet<EntityID>();
        female = new HashSet<EntityID>();
    }

    @Override
    public Shape render(Human h, Graphics2D g, ScreenTransform t) {
        Pair<Integer, Integer> location = h.getLocation(world);
        int x = t.xToScreen(location.first());
        int y = t.yToScreen(location.second());
        Shape shape;
        Icon icon = getIcon(h);
        if (icon == null) {
            shape = new Ellipse2D.Double(x - SIZE / 2, y - SIZE / 2, SIZE, SIZE);
            g.setColor(Color.GREEN);
            g.fill(shape);
        }
        else {
            x -= icon.getIconWidth() / 2;
            y -= icon.getIconHeight() / 2;
            shape = new Rectangle2D.Double(x, y, icon.getIconWidth(), icon.getIconHeight());
            icon.paintIcon(null, g, x, y);
        }
        return shape;
    }

    @Override
    protected void postView() {
        Collections.sort(entities, new HumanSorter());
    }

    private Icon getIcon(Human h) {
        boolean alive = h.getHP() > 0;
        switch (h.getStandardURN()) {
        case CIVILIAN:
            if (male.contains(h.getID())) {
                return alive ? CIVILIAN_MALE : CIVILIAN_MALE_DEAD;
            }
            if (female.contains(h.getID())) {
                return alive ? CIVILIAN_FEMALE : CIVILIAN_FEMALE_DEAD;
            }
            // Unknown civilian
            // CHECKSTYLE:OFF:MagicNumber
            if (Math.random() < 0.5) {
                // CHECKSTYLE:ON:MagicNumber
                male.add(h.getID());
                return alive ? CIVILIAN_MALE : CIVILIAN_MALE_DEAD;
            }
            else {
                female.add(h.getID());
                return alive ? CIVILIAN_FEMALE : CIVILIAN_FEMALE_DEAD;
            }
        case FIRE_BRIGADE:
            return alive ? FIRE_BRIGADE : FIRE_BRIGADE_DEAD;
        case AMBULANCE_TEAM:
            return alive ? AMBULANCE_TEAM : AMBULANCE_TEAM_DEAD;
        case POLICE_FORCE:
            return alive ? POLICE_FORCE : POLICE_FORCE_DEAD;
        default:
            return null;
        }
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