package sample;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.Constants;
import rescuecore2.log.Logger;

import rescuecore2.standard.components.StandardAgent;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.kernel.comms.ChannelCommunicationModel;
import rescuecore2.standard.kernel.comms.StandardCommunicationModel;

/**
   Abstract base class for sample agents.
   @param <E> The subclass of StandardEntity this agent wants to control.
 */
public abstract class AbstractSampleAgent<E extends StandardEntity> extends StandardAgent<E> {
    private static final int RANDOM_WALK_LENGTH = 50;

    private static final String SAY_COMMUNICATION_MODEL = StandardCommunicationModel.class.getName();
    private static final String SPEAK_COMMUNICATION_MODEL = ChannelCommunicationModel.class.getName();

    /**
       The search algorithm.
     */
    protected SampleSearch search;

    /**
       Whether to use AKSpeak messages or not.
    */
    protected boolean useSpeak;

    /**
       Construct an AbstractSampleAgent.
     */
    protected AbstractSampleAgent() {
    }

    @Override
    protected void postConnect() {
        super.postConnect();
        search = new SampleSearch(model);
        useSpeak = config.getValue(Constants.COMMUNICATION_MODEL_KEY).equals(SPEAK_COMMUNICATION_MODEL);
        Logger.debug("Communcation model: " + config.getValue(Constants.COMMUNICATION_MODEL_KEY));
        Logger.debug(useSpeak ? "Using speak model" : "Using say model");
    }

    /**
       Construct a random walk starting from this agent's current location. Buildings will only be entered at the end of the walk.
       @return A random walk.
     */
    protected List<EntityID> randomWalk() {
        List<EntityID> result = new ArrayList<EntityID>(RANDOM_WALK_LENGTH);
        Set<StandardEntity> seen = new HashSet<StandardEntity>();
        StandardEntity current = location();
        for (int i = 0; i < RANDOM_WALK_LENGTH; ++i) {
            result.add(current.getID());
            seen.add(current);
            List<StandardEntity> neighbours = new ArrayList<StandardEntity>(search.findNeighbours(current));
            Collections.shuffle(neighbours, random);
            boolean found = false;
            for (StandardEntity next : neighbours) {
                if (seen.contains(next)) {
                    continue;
                }
                current = next;
                found = true;
                break;
            }
            if (!found) {
                // We reached a dead-end.
                break;
            }
        }
        return result;
    }
}
