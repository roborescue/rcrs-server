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
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.net.URL;

import rescuecore2.misc.Pair;
import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.config.Config;
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

    private static final String ICON_SIZE_KEY = "view.standard.human.icons.size";
    private static final String USE_ICONS_KEY = "view.standard.human.icons.use";
    private static final int DEFAULT_ICON_SIZE = 32;

    private static final HumanSorter HUMAN_SORTER = new HumanSorter();

    private int iconSize;
    private Map<String, Map<State, Icon>> icons;

    /**
       Construct a human view layer.
    */
    public HumanLayer() {
        super(Human.class);
        iconSize = DEFAULT_ICON_SIZE;
    }

    @Override
    public void initialise(Config config) {
        iconSize = config.getIntValue(ICON_SIZE_KEY, DEFAULT_ICON_SIZE);
        icons = new HashMap<String, Map<State, Icon>>();
        if (config.getBooleanValue(USE_ICONS_KEY, true)) {
            icons.put(StandardEntityURN.FIRE_BRIGADE.toString(), generateIconMap("FireBrigade"));
            icons.put(StandardEntityURN.AMBULANCE_TEAM.toString(), generateIconMap("AmbulanceTeam"));
            icons.put(StandardEntityURN.POLICE_FORCE.toString(), generateIconMap("PoliceForce"));
            icons.put(StandardEntityURN.CIVILIAN.toString() + "-Male", generateIconMap("Civilian-Male"));
            icons.put(StandardEntityURN.CIVILIAN.toString() + "-Female", generateIconMap("Civilian-Female"));
        }
    }

    @Override
    public String getName() {
        return "Humans";
    }

    @Override
    public Shape render(Human h, Graphics2D g, ScreenTransform t) {
        Pair<Integer, Integer> location = getLocation(h);
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
        Collections.sort(entities, HUMAN_SORTER);
    }

    /**
       Get the location of a human.
       @param h The human to look up.
       @return The location of the human.
    */
    protected Pair<Integer, Integer> getLocation(Human h) {
        return h.getLocation(world);
    }

    private Map<State, Icon> generateIconMap(String type) {
        Map<State, Icon> result = new EnumMap<State, Icon>(State.class);
        for (State state : State.values()) {
            String resourceName = "rescuecore2/standard/view/" + type + "-" + state.toString() + "-" + iconSize + "x" + iconSize + ".png";
            URL resource = HumanLayer.class.getClassLoader().getResource(resourceName);
            if (resource == null) {
                System.out.println("Couldn't find resource: " + resourceName);
            }
            else {
                result.put(state, new ImageIcon(resource));
            }
        }
        return result;
    }

    private Icon getIcon(Human h) {
        State state = getState(h);
        Map<State, Icon> iconMap = null;
        switch (h.getStandardURN()) {
        case CIVILIAN:
            boolean male = h.getID().getValue() % 2 == 0;
            if (male) {
                iconMap = icons.get(StandardEntityURN.CIVILIAN.toString() + "-Male");
            }
            else {
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
            }
        },
        INJURED {
            @Override
            public String toString() {
                return "Injured";
            }
        },
        CRITICAL {
            @Override
            public String toString() {
                return "Critical";
            }
        },
        DEAD {
            @Override
            public String toString() {
                return "Dead";
            }
        };
    }

    private static final class HumanSorter implements Comparator<Human>, java.io.Serializable {
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