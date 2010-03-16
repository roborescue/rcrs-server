package rescuecore2.standard.score;

import rescuecore2.score.AbstractScoreFunction;
import rescuecore2.config.Config;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.Timestep;

import java.util.Set;
import java.util.HashSet;

import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.PoliceForce;

/**
   Score function that measures how quickly civilians are discovered by agents.
 */
public class DiscoveryScoreFunction extends AbstractScoreFunction {
    private Set<EntityID> found;

    /**
       Construct a DiscoveryScoreFunction.
    */
    public DiscoveryScoreFunction() {
        super("Civilian discovery time");
    }

    @Override
    public void initialise(WorldModel<? extends Entity> world, Config config) {
        found = new HashSet<EntityID>();
    }

    @Override
    public double score(WorldModel<? extends Entity> world, Timestep timestep) {
        double sum = 0;
        // Look for agents that observed a civilian
        for (EntityID next : timestep.getAgentsWithUpdates()) {
            Entity agent = world.getEntity(next);
            // Only platoon agents can discover civilians
            if (!isPlatoonAgent(agent)) {
                continue;
            }
            ChangeSet perception = timestep.getAgentPerception(next);
            for (EntityID observedID : perception.getChangedEntities()) {
                // Is it already seen?
                if (found.contains(observedID)) {
                    continue;
                }
                Entity e = world.getEntity(observedID);
                if (e instanceof Civilian && !perception.getChangedProperties(observedID).isEmpty()) {
                    // Seen a new civilian with at least one updated property.
                    found.add(observedID);
                    sum += timestep.getTime();
                }
            }
        }
        return sum;
    }

    private boolean isPlatoonAgent(Entity e) {
        return e instanceof FireBrigade
            || e instanceof PoliceForce
            || e instanceof AmbulanceTeam;
    }
}
