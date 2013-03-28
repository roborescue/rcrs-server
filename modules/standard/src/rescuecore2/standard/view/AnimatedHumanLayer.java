package rescuecore2.standard.view;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;

import rescuecore2.misc.Pair;
import rescuecore2.config.Config;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.misc.AgentPath;

/**
   A view layer that animates human movements.
 */
public class AnimatedHumanLayer extends HumanLayer {
    private Set<EntityID> humanIDs;

    private Map<EntityID, Queue<Pair<Integer, Integer>>> frames;
    private boolean animationDone;

    /**
       Construct an animated human view layer.
    */
    public AnimatedHumanLayer() {
        humanIDs = new HashSet<EntityID>();
        frames = new HashMap<EntityID, Queue<Pair<Integer, Integer>>>();
        animationDone = true;
    }

    @Override
    public void initialise(Config config) {
        super.initialise(config);
        humanIDs.clear();
        synchronized (this) {
            frames.clear();
            animationDone = true;
        }
    }

    @Override
    public String getName() {
        return "Humans (animated)";
    }

    /**
       Increase the frame number.
       @return True if a new frame is actually required.
    */
    public boolean nextFrame() {
        synchronized (this) {
            if (animationDone) {
                return false;
            }
            animationDone = true;
            for (Queue<Pair<Integer, Integer>> next : frames.values()) {
                if (next.size() > 0) {
                    next.remove();
                    animationDone = false;
                }
            }
            return !animationDone;
        }
    }

    @Override
    protected Pair<Integer, Integer> getLocation(Human h) {
        synchronized (this) {
            Queue<Pair<Integer, Integer>> agentFrames = frames.get(h.getID());
            if (agentFrames != null && !agentFrames.isEmpty()) {
                return agentFrames.peek();
            }
        }
        return h.getLocation(world);
    }

    @Override
    protected void preView() {
        super.preView();
        humanIDs.clear();
    }

    @Override
    protected void viewObject(Object o) {
        super.viewObject(o);
        if (o instanceof Human) {
            humanIDs.add(((Human)o).getID());
        }
    }

    /**
       Compute the animation frames.
       @param frameCount The number of animation frames to compute.
    */
    void computeAnimation(int frameCount) {
        synchronized (this) {
            frames.clear();
            // Compute animation
            double step = 1.0 / (frameCount - 1.0);
            for (EntityID next : humanIDs) {
                Queue<Pair<Integer, Integer>> result = new LinkedList<Pair<Integer, Integer>>();
                Human human = (Human)world.getEntity(next);
                if (human == null) {
                    continue;
                }
                AgentPath path;
                StandardEntity position = world.getEntity(human.getPosition());
                if(position instanceof AmbulanceTeam)
                	//to render the civilian in ambulance team movement
                	path=AgentPath.computePath((AmbulanceTeam)position,world);
                else
                	path= AgentPath.computePath(human, world);
                if (path == null) {
                    continue;
                }
                for (int i = 0; i < frameCount; ++i) {
                    Pair<Integer, Integer> nextPoint = path.getPointOnPath(i * step);
                    result.add(nextPoint);
                }
                frames.put(next, result);
            }
            animationDone = false;
        }
    }
}
