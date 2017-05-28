package rescuecore2.standard.view;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.Action;
import javax.swing.AbstractAction;

import java.util.Comparator;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;

import rescuecore2.misc.Pair;
import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.config.Config;
import rescuecore2.view.Icons;
import rescuecore2.log.Logger;

import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardWorldModel;

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

    private static HumanSorter HUMAN_SORTER=null;

    private static final Color CIVILIAN_COLOUR = Color.GREEN;
    private static final Color FIRE_BRIGADE_COLOUR = Color.RED;
    private static final Color POLICE_FORCE_COLOUR = Color.BLUE;
    private static final Color AMBULANCE_TEAM_COLOUR = Color.WHITE;
    private static final Color DEAD_COLOUR = Color.BLACK;

    private int iconSize;
    private Map<String, Map<State, Icon>> icons;
    private boolean useIcons;
    private Action useIconsAction;

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
        useIcons = config.getBooleanValue(USE_ICONS_KEY, false);
        icons.put(StandardEntityURN.FIRE_BRIGADE.toString(), generateIconMap("FireBrigade"));
        icons.put(StandardEntityURN.AMBULANCE_TEAM.toString(), generateIconMap("AmbulanceTeam"));
        icons.put(StandardEntityURN.POLICE_FORCE.toString(), generateIconMap("PoliceForce"));
        icons.put(StandardEntityURN.CIVILIAN.toString() + "-Male", generateIconMap("Civilian-Male"));
        icons.put(StandardEntityURN.CIVILIAN.toString() + "-Female", generateIconMap("Civilian-Female"));
        useIconsAction = new UseIconsAction();
    }

    @Override
    public String getName() {
        return "Humans";
    }

    @Override
    public Shape render(Human h, Graphics2D g, ScreenTransform t) {
        Pair<Integer, Integer> location = getLocation(h);
        if (location == null) {
            return null;
        }
        int x = t.xToScreen(location.first());
        int y = t.yToScreen(location.second());
        Shape shape;
        Icon icon = useIcons ? getIcon(h) : null;
        if (icon == null) {
            if (h.isPositionDefined() && (world.getEntity(h.getPosition()) instanceof AmbulanceTeam))
                // draw humans smaller in ambulances
           	 	shape = new Ellipse2D.Double(x - SIZE / 3, y - SIZE / 3, SIZE/3*2, SIZE/3*2);
            else
            	shape = new Ellipse2D.Double(x - SIZE / 2, y - SIZE / 2, SIZE, SIZE);
            
            g.setColor(adjustColour(getColour(h), h.isHPDefined()? h.getHP():10000));
            g.fill(shape);
            g.setColor(getColour(h));
            g.draw(shape);
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
    public List<JMenuItem> getPopupMenuItems() {
        List<JMenuItem> result = new ArrayList<JMenuItem>();
        result.add(new JMenuItem(useIconsAction));
        return result;
    }

    @Override
    protected void postView() {
    	if(world==null)
    		return;
    	if(HUMAN_SORTER==null)
        		HUMAN_SORTER= new HumanSorter(world);
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
                Logger.warn("Couldn't find resource: " + resourceName);
            }
            else {
                result.put(state, new ImageIcon(resource));
            }
        }
        return result;
    }

    private Color getColour(Human h) {
        switch (h.getStandardURN()) {
        case CIVILIAN:
            return CIVILIAN_COLOUR;
        case FIRE_BRIGADE:
            return FIRE_BRIGADE_COLOUR;
        case AMBULANCE_TEAM:
            return AMBULANCE_TEAM_COLOUR;
        case POLICE_FORCE:
            return POLICE_FORCE_COLOUR;
        default:
            throw new IllegalArgumentException("Don't know how to draw humans of type " + h.getStandardURN());
        }
    }

    private Color adjustColour(Color c, int hp) {
        if (hp == 0) {
            return DEAD_COLOUR;
        }
        if (hp < HP_CRITICAL) {
            c = c.darker();
        }
        if (hp < HP_INJURED) {
            c = c.darker();
        }
        if (hp < HP_MAX) {
            c = c.darker();
        }
        return c;
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
        private final StandardWorldModel world;

		public HumanSorter(StandardWorldModel world) {
			this.world = world;
		}

		@Override
        public int compare(Human h1, Human h2) {
			if(h1.isPositionDefined()&&h2.isPositionDefined()){
        	if(world.getEntity(h1.getPosition())instanceof AmbulanceTeam && !(world.getEntity(h2.getPosition())instanceof AmbulanceTeam))
        		return 1;
        	if(!(world.getEntity(h1.getPosition())instanceof AmbulanceTeam) && (world.getEntity(h2.getPosition())instanceof AmbulanceTeam))
        		return -1;
			}
        	if (h1 instanceof Civilian && !(h2 instanceof Civilian)) {
                return -1;
            }
        	
            if (h2 instanceof Civilian && !(h1 instanceof Civilian)) {
                return 1;
            }
            
            if(h1.isHPDefined()&&h2.isHPDefined())
            	return h2.getHP()-h1.getHP();
            return h1.getID().getValue() - h2.getID().getValue();
        }
    }

    private final class UseIconsAction extends AbstractAction {
        public UseIconsAction() {
            super("Use icons");
            putValue(Action.SELECTED_KEY, Boolean.valueOf(useIcons));
            putValue(Action.SMALL_ICON, useIcons ? Icons.TICK : Icons.CROSS);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            useIcons = !useIcons;
            putValue(Action.SELECTED_KEY, Boolean.valueOf(useIcons));
            putValue(Action.SMALL_ICON, useIcons ? Icons.TICK : Icons.CROSS);
            component.repaint();
        }
    }
}
