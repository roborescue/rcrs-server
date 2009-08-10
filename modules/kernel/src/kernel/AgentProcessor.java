package kernel;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;

/**
   Implementations of this decide which entities are controlled by agents and what each agent can see on startup.
 */
public interface AgentProcessor {
    /**
       Process a world model and tell the ComponentManager about agent-controlled entities.
       @param manager The ComponentManager.
       @param world The WorldModel.
    */
    void process(ComponentManager manager, WorldModel<? extends Entity> world);
}