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
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.misc.Pair;
import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.StandardEntityURN;

/**
   A view layer that renders humans.
 */
public class HumanLayer extends StandardEntityViewLayer<Human> {
    private static final int SIZE = 10;

    private static final int HP_MAX = 10000;
    private static final int HP_INJURED = 7500;
    private static final int HP_CRITICAL = 1000;

    private static final int ICON_SIZE = 32;

    /*
      private static final ImageIcon FIRE_BRIGADE_HEALTHY = new ImageIcon(HumanLayer.class.getClassLoader().getResource("rescuecore2/standard/view/FireBrigade-Healthy-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
      private static final ImageIcon FIRE_BRIGADE_INJURED = new ImageIcon(HumanLayer.class.getClassLoader().getResource("rescuecore2/standard/view/FireBrigade-Injured-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
      private static final ImageIcon FIRE_BRIGADE_CRITICAL = new ImageIcon(HumanLayer.class.getClassLoader().getResource("rescuecore2/standard/view/FireBrigade-Critical-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
      private static final ImageIcon FIRE_BRIGADE_DEAD = new ImageIcon(HumanLayer.class.getClassLoader().getResource("rescuecore2/standard/view/FireBrigade-Dead-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
      private static final ImageIcon POLICE_FORCE_HEALTHY = new ImageIcon(HumanLayer.class.getClassLoader().getResource("rescuecore2/standard/view/PoliceForce-Healthy-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
      private static final ImageIcon POLICE_FORCE_INJURED = new ImageIcon(HumanLayer.class.getClassLoader().getResource("rescuecore2/standard/view/PoliceForce-Injured-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
      private static final ImageIcon POLICE_FORCE_CRITICAL = new ImageIcon(HumanLayer.class.getClassLoader().getResource("rescuecore2/standard/view/PoliceForce-Critical-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
      private static final ImageIcon POLICE_FORCE_DEAD = new ImageIcon(HumanLayer.class.getClassLoader().getResource("rescuecore2/standard/view/PoliceForce-Dead-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
      private static final ImageIcon AMBULANCE_TEAM_HEALTHY = new ImageIcon(HumanLayer.class.getClassLoader().getResource("rescuecore2/standard/view/AmbulanceTeam-Healthy-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
      private static final ImageIcon AMBULANCE_TEAM_INJURED = new ImageIcon(HumanLayer.class.getClassLoader().getResource("rescuecore2/standard/view/AmbulanceTeam-Injured-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
      private static final ImageIcon AMBULANCE_TEAM_CRITICAL = new ImageIcon(HumanLayer.class.getClassLoader().getResource("rescuecore2/standard/view/AmbulanceTeam-Critical-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
      private static final ImageIcon AMBULANCE_TEAM_DEAD = new ImageIcon(HumanLayer.class.getClassLoader().getResource("rescuecore2/standard/view/AmbulanceTeam-Dead-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
      private static final ImageIcon CIVILIAN_MALE_HEALTHY = new ImageIcon(HumanLayer.class.getClassLoader().getResource("rescuecore2/standard/view/Civilian-Male-Healthy-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
      private static final ImageIcon CIVILIAN_MALE_CRITICAL = new ImageIcon(HumanLayer.class.getClassLoader().getResource("rescuecore2/standard/view/Civilian-Male-Injured-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
      private static final ImageIcon CIVILIAN_MALE_CRITICAL = new ImageIcon(HumanLayer.class.getClassLoader().getResource("rescuecore2/standard/view/Civilian-Male-Critical-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
      private static final ImageIcon CIVILIAN_MALE_DEAD = new ImageIcon(HumanLayer.class.getClassLoader().getResource("rescuecore2/standard/view/Civilian-Male-Dead-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
      private static final ImageIcon CIVILIAN_FEMALE_HEALTHY = new ImageIcon(HumanLayer.class.getClassLoader().getResource("rescuecore2/standard/view/Civilian-Female-Healthy-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
      private static final ImageIcon CIVILIAN_FEMALE_INJURED = new ImageIcon(HumanLayer.class.getClassLoader().getResource("rescuecore2/standard/view/Civilian-Female-Injured-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
      private static final ImageIcon CIVILIAN_FEMALE_CRITICAL = new ImageIcon(HumanLayer.class.getClassLoader().getResource("rescuecore2/standard/view/Civilian-Female-Critical-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
      private static final ImageIcon CIVILIAN_FEMALE_DEAD = new ImageIcon(HumanLayer.class.getClassLoader().getResource("rescuecore2/standard/view/Civilian-Female-Dead-" + ICON_SIZE + "x" + ICON_SIZE + ".png"));
    */

    private Set<EntityID> male;
    private Set<EntityID> female;

    private Map<String, Map<State, Icon>> icons;

    /**
       Construct a human view layer.
    */
    public HumanLayer() {
        super(Human.class);
        male = new HashSet<EntityID>();
        female = new HashSet<EntityID>();
        icons = new HashMap<String, Map<State, Icon>>();
        icons.put(StandardEntityURN.FIRE_BRIGADE.toString(), generateIconMap("FireBrigade"));
        icons.put(StandardEntityURN.AMBULANCE_TEAM.toString(), generateIconMap("AmbulanceTeam"));
        icons.put(StandardEntityURN.POLICE_FORCE.toString(), generateIconMap("PoliceForce"));
        icons.put(StandardEntityURN.CIVILIAN.toString() + "-Male", generateIconMap("Civilian-Male"));
        icons.put(StandardEntityURN.CIVILIAN.toString() + "-Female", generateIconMap("Civilian-Female"));
    }

    @Override
    public String getName() {
        return "Humans";
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

    private Map<State, Icon> generateIconMap(String type) {
        Map<State, Icon> result = new EnumMap<State, Icon>(State.class);
        for (State state : State.values()) {
            result.put(state, new ImageIcon(HumanLayer.class.getClassLoader().getResource("rescuecore2/standard/view/" + type + "-" + state.toString() + "-" + ICON_SIZE + "x" + ICON_SIZE + ".png")));
        }
        return result;
    }

    private Icon getIcon(Human h) {
        State state = getState(h);
        Map<State, Icon> iconMap = null;
        switch (h.getStandardURN()) {
        case CIVILIAN:
            if (!male.contains(h.getID()) && !female.contains(h.getID())) {
                // CHECKSTYLE:OFF:MagicNumber
                if (Math.random() < 0.5) {
                    // CHECKSTYLE:ON:MagicNumber
                    male.add(h.getID());
                }
                else {
                    female.add(h.getID());
                }
            }
            if (male.contains(h.getID())) {
                iconMap = icons.get(StandardEntityURN.CIVILIAN.toString() + "-Male");
            }
            if (female.contains(h.getID())) {
                iconMap = icons.get(StandardEntityURN.CIVILIAN.toString() + "-Female");
            }
            break;
        default:
            iconMap = icons.get(h.getStandardURN().toString());
        }
        if (iconMap == null) {
            return null;
        }
        return iconMap.get(state);
    }

    private State getState(Human h) {
        int hp = h.getHP();
        if (hp <= 0) {
            return State.DEAD;
        }
        if (hp <= HP_CRITICAL) {
            return State.CRITICAL;
        }
        if (hp <= HP_INJURED) {
            return State.INJURED;
        }
        return State.HEALTHY;
    }

    private enum State {
        HEALTHY {
            @Override
            public String toString() {
                return "Healthy";
            }},
        INJURED {
            @Override
            public String toString() {
                return "Injured";
            }},
        CRITICAL {
            @Override
            public String toString() {
                return "Critical";
            }},
        DEAD {
            @Override
            public String toString() {
                return "Dead";
            }};
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