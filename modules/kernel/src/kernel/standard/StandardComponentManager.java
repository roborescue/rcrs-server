package kernel.standard;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;

import kernel.Kernel;
import kernel.Agent;
import kernel.ComponentManager;

import rescuecore2.config.Config;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.Property;

import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.FireStation;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.AmbulanceCentre;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.PoliceOffice;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Node;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardPropertyType;

/**
   Class that manages connecting standard components. This extends the basic ComponentManager by filtering the entities/properties that agents see on connection.
 */
public class StandardComponentManager extends ComponentManager {
    private Set<Entity> initialEntities;

    /**
       Create a StandardComponentManager.
       @param kernel The kernel.
       @param world The world model.
       @param config The kernel configuration.
    */
    public StandardComponentManager(Kernel kernel, WorldModel<? extends Entity> world, Config config) {
        super(kernel, world, config);
        initialEntities = new HashSet<Entity>();
        for (Entity e : world.getAllEntities()) {
            if (e instanceof Civilian) {
                registerAgentControlledEntity(e);
            }
            else if (e instanceof FireBrigade) {
                registerAgentControlledEntity(e);
            }
            else if (e instanceof FireStation) {
                registerAgentControlledEntity(e);
            }
            else if (e instanceof AmbulanceTeam) {
                registerAgentControlledEntity(e);
            }
            else if (e instanceof AmbulanceCentre) {
                registerAgentControlledEntity(e);
            }
            else if (e instanceof PoliceForce) {
                registerAgentControlledEntity(e);
            }
            else if (e instanceof PoliceOffice) {
                registerAgentControlledEntity(e);
            }
            maybeAddInitialEntity(e);
        }
    }

    private void maybeAddInitialEntity(Entity e) {
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
            switch ((StandardPropertyType)next.getType()) {
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
            switch ((StandardPropertyType)next.getType()) {
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
            switch ((StandardPropertyType)next.getType()) {
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

    @Override
    protected Collection<Entity> getInitialEntitiesForAgent(Agent agent) {
        Collection<Entity> result = new HashSet<Entity>(initialEntities);
        result.add(agent.getControlledEntity());
        return result;
    }
}