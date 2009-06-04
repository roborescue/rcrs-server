package kernel.legacy;

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

import kernel.AbstractAgent;

import rescuecore2.connection.Connection;
import rescuecore2.messages.Command;
import rescuecore2.messages.Message;
import rescuecore2.worldmodel.Entity;

import rescuecore2.version0.entities.RescueEntity;
import rescuecore2.version0.messages.KASense;
import rescuecore2.version0.messages.AgentCommand;

/**
   Legacy agent implementation.
 */
public class LegacyAgent extends AbstractAgent {
    /**
       Construct a legacy agent.
       @param e The entity controlled by the agent.
       @param c The connection to the agent.
     */
    public LegacyAgent(Entity e, Connection c) {
        super(e, c);
    }

    @Override
    public void sendPerceptionUpdate(int time, Collection<? extends Entity> visible, Collection<? extends Message> comms) {
        Collection<RescueEntity> res = new ArrayList<RescueEntity>();
        for (Entity next : visible) {
            if (next instanceof RescueEntity) {
                res.add((RescueEntity)next);
            }
        }
        KASense sense = new KASense(getControlledEntity().getID().getValue(), time, res);
        Collection<Message> all = new ArrayList<Message>();
        all.add(sense);
        all.addAll(comms);
        send(all);
    }

    @Override
    public Collection<Command> getAgentCommands(int timestep) {
        Collection<Command> result = super.getAgentCommands(timestep);
        for (Iterator<Command> it = result.iterator(); it.hasNext();) {
            Command next = it.next();
            // If the message is not a version0 AgentCommand then remove it
            if (!(next instanceof AgentCommand)) {
                it.remove();
            }
        }
        return result;
    }
}