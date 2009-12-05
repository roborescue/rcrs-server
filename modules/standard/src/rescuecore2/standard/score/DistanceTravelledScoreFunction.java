package rescuecore2.standard.score;

import rescuecore2.score.AbstractScoreFunction;
import rescuecore2.config.Config;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.Command;
import rescuecore2.Timestep;

import java.util.Map;
import java.util.HashMap;

import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.messages.AKMove;
import rescuecore2.standard.misc.AgentPath;

/**
   A score function that measures the distance travelled by all agents.
*/
public class DistanceTravelledScoreFunction extends AbstractScoreFunction {
    private Map<EntityID, EntityID> lastPosition;
    private Map<EntityID, Integer> lastPositionExtra;

    /**
       Construct a DistanceTravelledScoreFunction.
    */
    public DistanceTravelledScoreFunction() {
        super("Distance travelled");
    }

    @Override
    public void initialise(WorldModel<? extends Entity> world, Config config) {
        lastPosition = new HashMap<EntityID, EntityID>();
        lastPositionExtra = new HashMap<EntityID, Integer>();
        storePositions(world);
    }

    @Override
    public double score(WorldModel<? extends Entity> world, Timestep timestep) {
        StandardWorldModel model = StandardWorldModel.createStandardWorldModel(world);
        // Find out how far each agent moved
        double sum = 0;
        for (Entity next : model) {
            if (next instanceof FireBrigade
                || next instanceof PoliceForce
                || next instanceof AmbulanceTeam) {
                Human h = (Human)next;
                AKMove move = null;
                for (Command command : timestep.getCommands(h.getID())) {
                    if (command instanceof AKMove) {
                        move = (AKMove)command;
                    }
                }
                if (move == null) {
                    continue;
                }
                EntityID start = lastPosition.get(h.getPosition());
                if (start == null) {
                    continue;
                }
                int startExtra = lastPositionExtra.get(h.getPosition());
                AgentPath path = AgentPath.computePath(h, start, startExtra, move, model);
                if (path != null) {
                    sum += path.getLength();
                }
            }
        }
        storePositions(world);
        return sum;
    }

    private void storePositions(WorldModel<? extends Entity> world) {
        lastPosition.clear();
        lastPositionExtra.clear();
        for (Entity next : world) {
            if (next instanceof Human) {
                Human h = (Human)next;
                if (h.isPositionDefined()) {
                    lastPosition.put(h.getID(), h.getPosition());
                    if (h.isPositionExtraDefined()) {
                        lastPositionExtra.put(h.getID(), h.getPositionExtra());
                    }
                    else {
                        lastPositionExtra.put(h.getID(), 0);
                    }
                }
            }
        }
    }
}