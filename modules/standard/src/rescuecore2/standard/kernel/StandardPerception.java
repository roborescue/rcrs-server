package rescuecore2.standard.kernel;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import kernel.AgentProxy;
import kernel.Perception;

import rescuecore2.GUIComponent;
import rescuecore2.config.Config;
import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.AmbulanceCentre;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.misc.SliderComponent;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.properties.IntProperty;

/**
 * Legacy implementation of perception with a GUI.
 */
/*
 * Implementation of Refuge Bed Capacity
 *
 * @author Farshid Faraji May 2020 During Covid-19 :-)))
 */
public class StandardPerception implements Perception, GUIComponent {
  private static final boolean DEFAULT_USE_FAR_FIRES = true;
  private static final int DEFAULT_HP_PRECISION = 1000;
  private static final int DEFAULT_DAMAGE_PRECISION = 100;

  private static final String VIEW_DISTANCE_KEY = "perception.standard.view-distance";
  private static final String FAR_FIRE_DISTANCE_KEY = "perception.standard.far-fire-distance";
  private static final String USE_FAR_FIRES_KEY = "perception.standard.use-far-fires";
  private static final String HP_PRECISION_KEY = "perception.standard.hp-precision";
  private static final String DAMAGE_PRECISION_KEY = "perception.standard.damage-precision";

  private static final int PRECISION_STEP_SIZE = 1000;
  private static final int PRECISION_MAX = 10000;
  private static final int VIEW_DISTANCE_MAX = 1000000;
  private static final int VIEW_DISTANCE_MAJOR_TICK = 100000;
  private static final int VIEW_DISTANCE_MINOR_TICK = 10000;
  private static final int PRECISION_MAJOR_TICK = 1000;
  private static final int PRECISION_MINOR_TICK = 100;

  private int viewDistance;
  private int farFireDistance;
  private boolean useFarFires;
  private int hpPrecision;
  private int damagePrecision;
  private StandardWorldModel world;
  private int time;
  private Set<Building> unburntBuildings;
  private Map<Building, Integer> ignitionTimes;
  private Config config;

  // Lock object for updating via the GUI.
  private final Object lock = new Object();

  /**
   * Create a StandardPerception object.
   */
  public StandardPerception() {
  }

  @Override
  public void initialise(Config newConfig, WorldModel<? extends Entity> model) {
    world = StandardWorldModel.createStandardWorldModel(model);
    this.config = newConfig;
    viewDistance = config.getIntValue(VIEW_DISTANCE_KEY);
    farFireDistance = config.getIntValue(FAR_FIRE_DISTANCE_KEY);
    useFarFires = config.getBooleanValue(USE_FAR_FIRES_KEY, DEFAULT_USE_FAR_FIRES);
    hpPrecision = config.getIntValue(HP_PRECISION_KEY, DEFAULT_HP_PRECISION);
    damagePrecision = config.getIntValue(DAMAGE_PRECISION_KEY, DEFAULT_DAMAGE_PRECISION);
    ignitionTimes = new HashMap<Building, Integer>();
    unburntBuildings = new HashSet<Building>();
    time = 0;
    for (StandardEntity next : world) {
      if (next instanceof Building) {
        Building b = (Building) next;
        if (!b.isFierynessDefined() || b.getFieryness() == 0) {
          unburntBuildings.add(b);
        } else {
          ignitionTimes.put(b, time);
        }
      }
    }
  }

  @Override
  public String toString() {
    return "Standard perception";
  }

  @Override
  public JComponent getGUIComponent() {
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
      if (next.isFierynessDefined()) {
        switch (next.getFierynessEnum()) {
          case HEATING:
          case BURNING:
          case INFERNO:
            ignitionTimes.put(next, time);
            it.remove();
            break;
          default:
            // Ignore
        }
      }
    }
    time = timestep;
    // Look for scripting elements in the config file
    checkForScript();
  }

  @Override
  public ChangeSet getVisibleEntities(AgentProxy agent) {
    synchronized (lock) {
      StandardEntity agentEntity = (StandardEntity) agent.getControlledEntity();
      ChangeSet result = new ChangeSet();
      // Look for roads/nodes/buildings/humans within range
      Pair<Integer, Integer> location = agentEntity.getLocation(world);
      if (location != null) {
        int x = location.first().intValue();
        int y = location.second().intValue();
        Collection<StandardEntity> nearby = world.getObjectsInRange(x, y, viewDistance);
        // Copy entities and set property values
        for (StandardEntity next : nearby) {
          StandardEntityURN urn = next.getStandardURN();
          switch (urn) {
            case ROAD:
            case HYDRANT:
              addRoadProperties((Road) next, result);
              break;

            case REFUGE:
              if (agentEntity instanceof Human)
                if (((Human) agentEntity).getPosition(world) == next) {
                  addRefugeProperties((Refuge) next, result);
                }
              addBuildingProperties((Building) next, result);
              break;
            case BUILDING:
            case GAS_STATION:
            case FIRE_STATION:
            case AMBULANCE_CENTRE:
            case POLICE_OFFICE:
              addBuildingProperties((Building) next, result);
              break;
            case CIVILIAN:
            case FIRE_BRIGADE:
            case AMBULANCE_TEAM:
            case POLICE_FORCE:
              // Always send all properties of the agent-controlled object
              if (next == agentEntity) {
                addSelfProperties((Human) next, result);
              } else {
                addHumanProperties((Human) next, result);
              }
              break;
            case BLOCKADE:
              addBlockadeProperties((Blockade) next, result);
              break;
            default:
              // Ignore other types
              break;
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
              addFarBuildingProperties(b, result);
            }
          }
        }

        if (agentEntity instanceof AmbulanceCentre) {
          Collection<StandardEntity> refuges = world.getEntitiesOfType(StandardEntityURN.REFUGE);
          for (StandardEntity next : refuges) {
            addRefugeProperties((Refuge) next, result);
          }
        }
      }
      return result;
    }
  }

  private void addRoadProperties(Road road, ChangeSet result) {
    addAreaProperties(road, result);
    // Only update blockades
    result.addChange(road, road.getBlockadesProperty());
    // Also update each blockade
    if (road.isBlockadesDefined()) {
      for (EntityID id : road.getBlockades()) {
        Blockade blockade = (Blockade) world.getEntity(id);
        addBlockadeProperties(blockade, result);
      }
    }
  }

  private void addBuildingProperties(Building building, ChangeSet result) {
    addAreaProperties(building, result);
    // Update TEMPERATURE, FIERYNESS and BROKENNESS
    result.addChange(building, building.getTemperatureProperty());
    result.addChange(building, building.getFierynessProperty());
    result.addChange(building, building.getBrokennessProperty());
  }

  public void addRefugeProperties(Refuge refuge, ChangeSet result) {
    result.addChange(refuge, refuge.getBedCapacityProperty());
    result.addChange(refuge, refuge.getOccupiedBedsProperty());
    result.addChange(refuge, refuge.getWaitingListSizeProperty());
    // TODO send other information e.g civilians info in the refuge
  }

  private void addAreaProperties(Area area, ChangeSet result) {
  }

  private void addFarBuildingProperties(Building building, ChangeSet result) {
    // Update FIERYNESS only
    result.addChange(building, building.getFierynessProperty());
  }

  private void addHumanProperties(Human human, ChangeSet result) {
    // Update POSITION, POSITION_EXTRA, DIRECTION, STAMINA, HP, DAMAGE, BURIEDNESS
    result.addChange(human, human.getPositionProperty());
    // result.addChange(human, human.getPositionExtraProperty());
    result.addChange(human, human.getXProperty());
    result.addChange(human, human.getYProperty());
    result.addChange(human, human.getDirectionProperty());
    result.addChange(human, human.getStaminaProperty());
    result.addChange(human, human.getBuriednessProperty());
    // Round HP and damage
    IntProperty hp = (IntProperty) human.getHPProperty().copy();
    roundProperty(hp, hpPrecision);
    result.addChange(human, hp);
    IntProperty damage = (IntProperty) human.getDamageProperty().copy();
    roundProperty(damage, damagePrecision);
    result.addChange(human, damage);
  }

  private void addSelfProperties(Human human, ChangeSet result) {
    // Update human properties and POSITION_HISTORY
    addHumanProperties(human, result);
    result.addChange(human, human.getPositionHistoryProperty());
    // Un-round hp and damage
    result.addChange(human, human.getHPProperty());
    result.addChange(human, human.getDamageProperty());
    if (human instanceof FireBrigade)
      result.addChange(human, ((FireBrigade) human).getWaterProperty());
  }

  private void addBlockadeProperties(Blockade blockade, ChangeSet result) {
    result.addChange(blockade, blockade.getXProperty());
    result.addChange(blockade, blockade.getYProperty());
    result.addChange(blockade, blockade.getPositionProperty());
    result.addChange(blockade, blockade.getApexesProperty());
    result.addChange(blockade, blockade.getRepairCostProperty());
  }

  private void roundProperty(IntProperty p, int precision) {
    if (precision != 1 && p.isDefined()) {
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
      if (value == 0) {
        value = 1;
      }
      hpPrecision = value;
    }
  }

  private void updateDamagePrecision(int value) {
    synchronized (lock) {
      if (value == 0) {
        value = 1;
      }
      damagePrecision = value;
    }
  }

  private void updateUseFarFires(boolean value) {
    synchronized (lock) {
      useFarFires = value;
    }
  }

  private void checkForScript() {
    viewDistance = config.getIntValue(VIEW_DISTANCE_KEY + ".script." + time, viewDistance);
    farFireDistance = config.getIntValue(FAR_FIRE_DISTANCE_KEY + ".script." + time, farFireDistance);
    useFarFires = config.getBooleanValue(USE_FAR_FIRES_KEY + ".script." + time, useFarFires);
    hpPrecision = config.getIntValue(HP_PRECISION_KEY + ".script." + time, hpPrecision);
    damagePrecision = config.getIntValue(DAMAGE_PRECISION_KEY + ".script." + time, damagePrecision);
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
      hpPrecisionSlider = new JSlider(SwingConstants.VERTICAL, 0, PRECISION_MAX, hpPrecision);
      damagePrecisionSlider = new JSlider(SwingConstants.VERTICAL, 0, PRECISION_MAX, damagePrecision);
      farFiresBox = new JCheckBox("Use far fires?", useFarFires);
      SliderComponent s = new SliderComponent(viewDistanceSlider);
      s.setBorder(BorderFactory.createTitledBorder("View distance"));
      add(s);
      s = new SliderComponent(hpPrecisionSlider);
      s.setBorder(BorderFactory.createTitledBorder("HP precision"));
      add(s);
      s = new SliderComponent(damagePrecisionSlider);
      s.setBorder(BorderFactory.createTitledBorder("Damage precision"));
      add(s);
      add(farFiresBox);
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
      hpPrecisionSlider.setMajorTickSpacing(PRECISION_MAJOR_TICK);
      hpPrecisionSlider.setMinorTickSpacing(PRECISION_MINOR_TICK);
      damagePrecisionSlider.setPaintLabels(true);
      damagePrecisionSlider.setPaintTicks(true);
      damagePrecisionSlider.setMajorTickSpacing(PRECISION_MAJOR_TICK);
      damagePrecisionSlider.setMinorTickSpacing(PRECISION_MINOR_TICK);

      Dictionary<Integer, JComponent> labels = new Hashtable<Integer, JComponent>();
      labels.put(1, new JLabel("Accurate"));
      for (int i = PRECISION_STEP_SIZE; i <= PRECISION_MAX; i += PRECISION_STEP_SIZE) {
        labels.put(i, new JLabel(String.valueOf(i)));
      }
      hpPrecisionSlider.setLabelTable(labels);
      damagePrecisionSlider.setLabelTable(labels);
    }
  }
}