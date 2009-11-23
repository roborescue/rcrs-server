package sample;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.EnumSet;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.components.AbstractAgent;
import rescuecore2.Constants;

import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;

/**
   Abstract base class for sample agents.
 */
public abstract class AbstractSampleAgent extends AbstractAgent<StandardEntity> {
    private static final int MESH_SIZE = 10000;
    private static final int RANDOM_WALK_LENGTH = 50;

    private static final String SAY_COMMUNICATION_MODEL = "kernel.standard.StandardCommunicationModel";
    private static final String SPEAK_COMMUNICATION_MODEL = "kernel.standard.ChannelCommunicationModel";

    /**
       The world model referenced as a StandardWorldModel. Note that this will reference the same object as {@link AbstractAgent#model}.
     */
    protected StandardWorldModel world;

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
    public String[] getRequestedEntityURNs() {
        EnumSet<StandardEntityURN> set = getRequestedEntityURNsEnum();
        String[] result = new String[set.size()];
        int i = 0;
        for (StandardEntityURN next : set) {
            result[i++] = next.name();
        }
        return result;
    }

    @Override
    protected WorldModel<StandardEntity> createWorldModel() {
        world = new StandardWorldModel();
        return world;
    }

    @Override
    protected void postConnect() {
        super.postConnect();
        world.index(MESH_SIZE);
        search = new SampleSearch(world, true);
        useSpeak = config.getValue(Constants.COMMUNICATION_MODEL_KEY).equals(SPEAK_COMMUNICATION_MODEL);
    }

    /**
       Get the location of the entity controlled by this agent.
       @return The location of the entity controlled by this agent.
     */
    protected StandardEntity location() {
        return me().getPosition(world);
    }

    @Override
    protected Human me() {
        return (Human)super.me();
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
                if (next instanceof Building && i < RANDOM_WALK_LENGTH - 1) {
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

    /**
       Get an EnumSet containing requested entity URNs.
       @return An EnumSet containing requested entity URNs.
     */
    protected abstract EnumSet<StandardEntityURN> getRequestedEntityURNsEnum();
}