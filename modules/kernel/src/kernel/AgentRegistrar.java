package kernel;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.config.Config;

/**
   Implementations of this decide which entities are controlled by agents and what each agent can see on startup.
 */
public interface AgentRegistrar {
    /**
       Process a WorldModel and Config and tell the ComponentManager which entities are agent-controlled and what they can see on connection.
       @param world The WorldModel.
       @param config The Config.
       @param manager The ComponentManager.
       @throws KernelException If there is a problem registering agents.
    */
    void registerAgents(WorldModel<? extends Entity> world, Config config, ComponentManager manager) throws KernelException;
}
