package kernel.standard;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import kernel.Perception;
import kernel.AgentProxy;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.properties.IntProperty;
import rescuecore2.config.Config;
import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardPropertyURN;

/**
   Legacy implementation of perception.
 */
public class StandardPerception implements Perception {
    private static final String VIEW_DISTANCE_KEY = "perception.standard.view-distance";
    private static final String FAR_FIRE_DISTANCE_KEY = "perception.standard.far-fire-distance";
    private static final String USE_FAR_FIRES_KEY = "perception.standard.use-far-fires";
    private static final String HP_PRECISION_KEY = "perception.standard.hp-precision";
    private static final String DAMAGE_PRECISION_KEY = "perception.standard.damage-precision";

    private static final int DEFAULT_HP_PRECISION = 1000;
    private static final int DEFAULT_DAMAGE_PRECISION = 100;

    private int viewDistance;
    private int farFireDistance;
    private boolean useFarFires;
    private int hpPrecision;
    private int damagePrecision;
    private StandardWorldModel world;
    private int time;
    private Set<Building> unburntBuildings;
    private Map<Building, Integer> ignitionTimes;

    /**
       Create a StandardPerception object.
    */
    public StandardPerception() {
    }

    @Override
    public void initialise(Config config, WorldModel<? extends Entity> model) {
        world = StandardWorldModel.createStandardWorldModel(model);
        viewDistance = config.getIntValue(VIEW_DISTANCE_KEY);
        farFireDistance = config.getIntValue(FAR_FIRE_DISTANCE_KEY, 0);
        useFarFires = config.getBooleanValue(USE_FAR_FIRES_KEY, true);
        hpPrecision = config.getIntValue(HP_PRECISION_KEY, DEFAULT_HP_PRECISION);
        damagePrecision = config.getIntValue(DAMAGE_PRECISION_KEY, DEFAULT_DAMAGE_PRECISION);
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
        return "Standard perception";
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
    public ChangeSet getVisibleEntities(AgentProxy agent) {
        StandardEntity agentEntity = (StandardEntity)agent.getControlledEntity();
        ChangeSet result = new ChangeSet();
        // Look for roads/nodes/buildings/humans within range
        Pair<Integer, Integer> location = agentEntity.getLocation(world);
        if (location != null) {
            int x = location.first().intValue();
            int y = location.second().intValue();
            Collection<StandardEntity> nearby = world.getObjectsInRange(x, y, viewDistance);
            // Copy entities and set property values
            for (StandardEntity next : nearby) {
                StandardEntityURN urn = StandardEntityURN.valueOf(next.getURN());
                switch (urn) {
                case ROAD:
                    addRoadProperties((Road)next, result);
                    break;
                case BUILDING:
                case REFUGE:
                case FIRE_STATION:
                case AMBULANCE_CENTRE:
                case POLICE_OFFICE:
                    addBuildingProperties((Building)next, result);
                    break;
                case CIVILIAN:
                case FIRE_BRIGADE:
                case AMBULANCE_TEAM:
                case POLICE_FORCE:
                    // Always send all properties of the agent-controlled object
		    if (next == agentEntity) {
			addSelfProperties((Human)next, result);
		    }
		    else {
                        addHumanProperties((Human)next, result);
                    }
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
        }
        return result;
    }

    private void addRoadProperties(Road road, ChangeSet result) {
	// Only update BLOCK
	result.addChange(road, road.getBlockProperty());
    }

    private void addBuildingProperties(Building building, ChangeSet result) {
        // Update TEMPERATURE, FIERYNESS and BROKENNESS
	result.addChange(building, building.getTemperatureProperty());
	result.addChange(building, building.getFierynessProperty());
	result.addChange(building, building.getBrokennessProperty());
    }

    private void addFarBuildingProperties(Building building, ChangeSet result) {
        // Update FIERYNESS only
	result.addChange(building, building.getFierynessProperty());
    }

    private void addHumanProperties(Human human, ChangeSet result) {
        // Update POSITION, POSITION_EXTRA, DIRECTION, STAMINA, HP, DAMAGE, BURIEDNESS
	result.addChange(human, human.getPositionProperty());
	result.addChange(human, human.getPositionExtraProperty());
	result.addChange(human, human.getDirectionProperty());
	result.addChange(human, human.getStaminaProperty());
	result.addChange(human, human.getBuriednessProperty());
	// Round HP and damage
	IntProperty hp = (IntProperty)human.getHPProperty().copy();
	roundProperty(hp, hpPrecision);
	result.addChange(human, hp);
	IntProperty damage = (IntProperty)human.getDamageProperty().copy();
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
}