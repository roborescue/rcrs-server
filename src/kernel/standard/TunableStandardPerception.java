package kernel.standard;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Dictionary;

import kernel.Perception;
import kernel.Agent;
import kernel.Kernel;
import kernel.ui.KernelGUIComponent;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.properties.IntProperty;
import rescuecore2.config.Config;
import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardPropertyType;

import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

/**
   Legacy implementation of perception with a GUI.
 */
public class TunableStandardPerception implements Perception, KernelGUIComponent {
    private static final int DEFAULT_HP_PRECISION = 1000;
    private static final int DEFAULT_DAMAGE_PRECISION = 100;

    private static final int PRECISION_STEP_SIZE = 1000;
    private static final int PRECISION_MAX = 10000;
    private static final int VIEW_DISTANCE_MAX = 1000000;
    private static final int VIEW_DISTANCE_MAJOR_TICK = 100000;
    private static final int VIEW_DISTANCE_MINOR_TICK = 10000;

    private int viewDistance;
    private int farFireDistance;
    private boolean useFarFires;
    private int hpPrecision;
    private int damagePrecision;
    private StandardWorldModel world;
    private int time;
    private Set<Building> unburntBuildings;
    private Map<Building, Integer> ignitionTimes;

    // Lock object for updating via the GUI.
    private final Object lock = new Object();

    /**
       Create a TunableStandardPerception object.
       @param config The configuration of the kernel.
       @param world The world model.
    */
    public TunableStandardPerception(Config config, StandardWorldModel world) {
        this.world = world;
        this.viewDistance = config.getIntValue("vision");
        this.farFireDistance = config.getIntValue("fire_cognition_spreading_speed");
        useFarFires = true;
        hpPrecision = DEFAULT_HP_PRECISION;
        damagePrecision = DEFAULT_DAMAGE_PRECISION;
        ignitionTimes = new HashMap<Building, Integer>();
        unburntBuildings = new HashSet<Building>();
        time = 0;
        for (StandardEntity next : world) {
            if (next instanceof Building) {
                Building b = (Building)next;
                if (b.getFieryness() == 0) {
                    unburntBuildings.add(b);
                }
                else {
                    ignitionTimes.put(b, time);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Tunable standard perception";
    }

    @Override
    public JComponent getGUIComponent(Kernel kernel) {
        return new TunePanel();
    }

    @Override
    public String getGUIComponentName() {
        return "Perception parameters";
    }

    @Override
    public void setTime(int timestep) {
        // Look for buildings that caught fire last timestep
        for (Iterator<Building> it = unburntBuildings.iterator(); it.hasNext();) {
            Building next = it.next();
            int fieryness = next.getFieryness();
            // Fieryness 1, 2 and 3 mean on fire
            // CHECKSTYLE:OFF:MagicNumber
            if (fieryness > 0 && fieryness < 4) {
                // CHECKSTYLE:ON:MagicNumber
                ignitionTimes.put(next, time);
                it.remove();
            }
        }
        time = timestep;
    }

    @Override
    public Collection<Entity> getVisibleEntities(Agent agent) {
        synchronized (lock) {
            StandardEntity agentEntity = (StandardEntity)agent.getControlledEntity();
            Collection<Entity> result = new HashSet<Entity>();
            // Look for roads/nodes/buildings/humans within range
            Pair<Integer, Integer> location = agentEntity.getLocation(world);
            if (location != null) {
                int x = location.first().intValue();
                int y = location.second().intValue();
                Collection<StandardEntity> nearby = world.getObjectsInRange(x, y, viewDistance);
                // Copy entities and set property values
                for (StandardEntity next : nearby) {
                    StandardEntity copy = null;
                    switch (next.getType()) {
                    case ROAD:
                        copy = (StandardEntity)next.copy();
                        filterRoadProperties((Road)copy);
                        break;
                    case BUILDING:
                    case REFUGE:
                    case FIRE_STATION:
                    case AMBULANCE_CENTRE:
                    case POLICE_OFFICE:
                        copy = (StandardEntity)next.copy();
                        filterBuildingProperties((Building)copy);
                        break;
                    case CIVILIAN:
                    case FIRE_BRIGADE:
                    case AMBULANCE_TEAM:
                    case POLICE_FORCE:
                        copy = (StandardEntity)next.copy();
                        // Always send all properties of the agent-controlled object
                        if (next != agentEntity) {
                            filterHumanProperties((Human)copy);
                        }
                        break;
                    default:
                        // Ignore other types
                        break;
                    }
                    if (copy != null) {
                        result.add(copy);
                    }
                }
                // Now look for far fires
                if (useFarFires) {
                    for (Map.Entry<Building, Integer> next : ignitionTimes.entrySet()) {
                        Building b = next.getKey();
                        int ignitionTime = next.getValue();
                        int timeDelta = time - ignitionTime;
                        int visibleRange = timeDelta * farFireDistance;
                        int range = world.getDistance(agentEntity, b);
                        if (range <= visibleRange) {
                            Building copy = (Building)b.copy();
                            filterFarBuildingProperties(copy);
                            result.add(copy);
                        }
                    }
                }
            }
            return result;
        }
    }

    private void filterRoadProperties(Road road) {
        // Update BLOCK only
        for (Property next : road.getProperties()) {
            switch ((StandardPropertyType)next.getType()) {
            case BLOCK:
                break;
            default:
                next.undefine();
            }
        }
    }

    private void filterBuildingProperties(Building building) {
        // Update TEMPERATURE, FIERYNESS and BROKENNESS
        for (Property next : building.getProperties()) {
            switch ((StandardPropertyType)next.getType()) {
            case TEMPERATURE:
            case FIERYNESS:
            case BROKENNESS:
                break;
            default:
                next.undefine();
            }
        }
    }

    private void filterFarBuildingProperties(Building building) {
        // Update FIERYNESS only
        for (Property next : building.getProperties()) {
            switch ((StandardPropertyType)next.getType()) {
            case FIERYNESS:
                break;
            default:
                next.undefine();
            }
        }
    }

    private void filterHumanProperties(Human human) {
        // Update POSITION, POSITION_EXTRA, DIRECTION, STAMINA, HP, DAMAGE, BURIEDNESS
        for (Property next : human.getProperties()) {
            switch ((StandardPropertyType)next.getType()) {
            case POSITION:
            case POSITION_EXTRA:
            case DIRECTION:
            case STAMINA:
            case BURIEDNESS:
                break;
            case HP:
                roundProperty((IntProperty)next, hpPrecision);
                break;
            case DAMAGE:
                roundProperty((IntProperty)next, damagePrecision);
                break;
            default:
                next.undefine();
            }
        }
    }

    private void roundProperty(IntProperty p, int precision) {
        if (precision != 1) {
            p.setValue(round(p.getValue(), precision));
        }
    }

    private int round(int value, int precision) {
        int remainder = value % precision;
        value -= remainder;
        if (remainder >= precision / 2) {
            value += precision;
        }
        return value;
    }

    private void updateViewDistance(int value) {
        synchronized (lock) {
            viewDistance = value;
        }
    }

    private void updateHPPrecision(int value) {
        synchronized (lock) {
            hpPrecision = value;
        }
    }

    private void updateDamagePrecision(int value) {
        synchronized (lock) {
            damagePrecision = value;
        }
    }

    private void updateUseFarFires(boolean value) {
        synchronized (lock) {
            useFarFires = value;
        }
    }

    private class TunePanel extends JPanel {
        private JSlider viewDistanceSlider;
        private JSlider hpPrecisionSlider;
        private JSlider damagePrecisionSlider;
        private JCheckBox farFiresBox;

        public TunePanel() {
            // CHECKSTYLE:OFF:MagicNumber
            super(new GridLayout(1, 4));
            // CHECKSTYLE:ON:MagicNumber
            viewDistanceSlider = new JSlider(SwingConstants.VERTICAL, 0, VIEW_DISTANCE_MAX, viewDistance);
            hpPrecisionSlider = new JSlider(SwingConstants.VERTICAL, 1, PRECISION_MAX, hpPrecision);
            damagePrecisionSlider = new JSlider(SwingConstants.VERTICAL, 1, PRECISION_MAX, damagePrecision);
            farFiresBox = new JCheckBox("Use far fires?", useFarFires);
            add(new SliderComponent(viewDistanceSlider, "View distance"));
            add(new SliderComponent(hpPrecisionSlider, "HP precision"));
            add(new SliderComponent(damagePrecisionSlider, "Damage precision"));
            add(farFiresBox);


            /*
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            setLayout(layout);
            c.gridwidth = 1;
            c.gridheight = 1;
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 0;
            c.fill = GridBagConstraints.BOTH;
            c.anchor = GridBagConstraints.CENTER;


            addLabel("View distance", layout, c, 0);
            addLabel("HP precision", layout, c, 1);
            addLabel("Damage precision", layout, c, 2);
            c.weighty = 1;
            c.gridx = 0;
            c.gridy = 1;
            layout.setConstraints(viewDistanceSlider, c);
            add(viewDistanceSlider);
            c.gridx = 1;
            layout.setConstraints(hpPrecisionSlider, c);
            add(hpPrecisionSlider);
            c.gridx = 2;
            layout.setConstraints(damagePrecisionSlider, c);
            add(damagePrecisionSlider);
            c.gridx = 3;
            layout.setConstraints(farFiresBox, c);
            add(farFiresBox);
            */
            viewDistanceSlider.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        updateViewDistance(viewDistanceSlider.getValue());
                    }
                });
            hpPrecisionSlider.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        updateHPPrecision(hpPrecisionSlider.getValue());
                    }
                });
            damagePrecisionSlider.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        updateDamagePrecision(damagePrecisionSlider.getValue());
                    }
                });
            farFiresBox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        updateUseFarFires(farFiresBox.isSelected());
                    }
                });
            // Add tick marks to sliders
            viewDistanceSlider.setPaintLabels(true);
            viewDistanceSlider.setPaintTicks(true);
            viewDistanceSlider.setMajorTickSpacing(VIEW_DISTANCE_MAJOR_TICK);
            viewDistanceSlider.setMinorTickSpacing(VIEW_DISTANCE_MINOR_TICK);
            hpPrecisionSlider.setPaintLabels(true);
            hpPrecisionSlider.setPaintTicks(true);
            damagePrecisionSlider.setPaintLabels(true);
            damagePrecisionSlider.setPaintTicks(true);

            Dictionary<Integer, JComponent> labels = new Hashtable<Integer, JComponent>();
            labels.put(1, new JLabel("Accurate"));
            for (int i = PRECISION_STEP_SIZE; i <= PRECISION_MAX; i += PRECISION_STEP_SIZE) {
                labels.put(i, new JLabel(String.valueOf(i)));
            }
            hpPrecisionSlider.setLabelTable(labels);
            damagePrecisionSlider.setLabelTable(labels);
        }
    }

    private static class SliderComponent extends JPanel {
        public SliderComponent(final JSlider slider, String name) {
            super(new BorderLayout());
            setBorder(BorderFactory.createTitledBorder(name));
            add(slider, BorderLayout.CENTER);
            final JTextField text = new JTextField(String.valueOf(slider.getValue()));
            add(text, BorderLayout.SOUTH);
            text.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            int i = Integer.parseInt(text.getText());
                            slider.setValue(i);
                        }
                        catch (NumberFormatException ex) {
                            // Ignore
                            ex.printStackTrace();
                        }
                    }
                });
            slider.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        text.setText(String.valueOf(slider.getValue()));
                    }
                });
        }
    }
}