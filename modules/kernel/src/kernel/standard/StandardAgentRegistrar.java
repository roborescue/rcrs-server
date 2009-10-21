package kernel.standard;

import java.util.Set;
import java.util.HashSet;

import kernel.AgentRegistrar;
import kernel.ComponentManager;

import rescuecore2.config.Config;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.Property;
import rescuecore2.Constants;

import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.FireStation;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.AmbulanceCentre;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.PoliceOffice;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Node;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardPropertyURN;

/**
   Class that registers standard agents.
 */
public class StandardAgentRegistrar implements AgentRegistrar {
    private static final Set<String> VISIBLE_CONFIG_OPTIONS = new HashSet<String>();

    static {
        VISIBLE_CONFIG_OPTIONS.add("kernel.agents.think-time");
        VISIBLE_CONFIG_OPTIONS.add("kernel.startup.connect-time");
        VISIBLE_CONFIG_OPTIONS.add("fire.tank.maximum");
        VISIBLE_CONFIG_OPTIONS.add("fire.tank.refill-rate");
        VISIBLE_CONFIG_OPTIONS.add("fire.extinguish.max-sum");
        VISIBLE_CONFIG_OPTIONS.add("fire.extinguish.max-distance");
        VISIBLE_CONFIG_OPTIONS.add(Constants.COMMUNICATION_MODEL_KEY);
        VISIBLE_CONFIG_OPTIONS.add(Constants.PERCEPTION_KEY);
    }

    @Override
    public void registerAgents(WorldModel<? extends Entity> world, Config config, ComponentManager manager) {
        Config agentConfig = new Config(config);
        agentConfig.removeExcept(VISIBLE_CONFIG_OPTIONS);
        Set<Entity> initialEntities = new HashSet<Entity>();
        for (Entity e : world) {
            maybeAddInitialEntity(e, initialEntities);
        }
        for (Entity e : world) {
            if (e instanceof FireBrigade
                    || e instanceof FireStation
                    || e instanceof AmbulanceTeam
                    || e instanceof AmbulanceCentre
                    || e instanceof PoliceForce
                    || e instanceof PoliceOffice
                    || e instanceof Civilian) {
                Set<Entity> s = new HashSet<Entity>(initialEntities);
                s.add(e);
                manager.registerAgentControlledEntity(e, s, agentConfig);
            }
        }
    }

    private void maybeAddInitialEntity(Entity e, Set<Entity> initialEntities) {
        if (e instanceof Road) {
            Road r = (Road)e.copy();
            filterRoadProperties(r);
            initialEntities.add(r);
        }
        if (e instanceof Node) {
            Node n = (Node)e.copy();
            filterNodeProperties(n);
            initialEntities.add(n);
        }
        if (e instanceof Building) {
            Building b = (Building)e.copy();
            filterBuildingProperties(b);
            initialEntities.add(b);
        }
    }

    private void filterRoadProperties(Road r) {
        for (Property next : r.getProperties()) {
            // Road properties: ROAD_KIND, WIDTH, MEDIAN_STRIP, LINES_TO_HEAD, LINES_TO_TAIL and WIDTH_FOR_WALKERS
            // Edge properties: HEAD, TAIL, LENGTH
            // Everything else should be undefined
            StandardPropertyURN urn = StandardPropertyURN.valueOf(next.getURN());
            switch (urn) {
            case ROAD_KIND:
            case WIDTH:
            case MEDIAN_STRIP:
            case LINES_TO_HEAD:
            case LINES_TO_TAIL:
            case WIDTH_FOR_WALKERS:
            case HEAD:
            case TAIL:
            case LENGTH:
                break;
            default:
                next.undefine();
            }
        }
    }

    private void filterNodeProperties(Node n) {
        for (Property next : n.getProperties()) {
            // Node properties: SIGNAL, SHORTCUT_TO_TURN, POCKET_TO_TURN_ACROSS, SIGNAL_TIMING
            // Vertex properties: X, Y, EDGES
            // Everything else should be undefined
            StandardPropertyURN urn = StandardPropertyURN.valueOf(next.getURN());
            switch (urn) {
            case SIGNAL:
            case SHORTCUT_TO_TURN:
            case POCKET_TO_TURN_ACROSS:
            case SIGNAL_TIMING:
            case X:
            case Y:
            case EDGES:
                break;
            default:
                next.undefine();
            }
        }
    }

    private void filterBuildingProperties(Building b) {
        for (Property next : b.getProperties()) {
            // Building properties: X, Y, FLOORS, BUILDING_CODE, BUILDING_ATTRIBUTES, BUILDING_AREA_GROUND, BUILDING_AREA_TOTAL, IMPORTANCE, ENTRANCES
            // Everything else should be undefined
            StandardPropertyURN urn = StandardPropertyURN.valueOf(next.getURN());
            switch (urn) {
            case X:
            case Y:
            case FLOORS:
            case BUILDING_CODE:
            case BUILDING_ATTRIBUTES:
            case BUILDING_AREA_GROUND:
            case BUILDING_AREA_TOTAL:
            case IMPORTANCE:
            case ENTRANCES:
                break;
            default:
                next.undefine();
            }
        }
    }
}