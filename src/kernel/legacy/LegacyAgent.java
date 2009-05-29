package kernel.legacy;

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

import kernel.AbstractAgent;

import rescuecore2.connection.Connection;
import rescuecore2.messages.Command;
import rescuecore2.messages.Message;
import rescuecore2.version0.entities.RescueEntity;
import rescuecore2.version0.messages.KASense;
import rescuecore2.version0.messages.AgentCommand;
import rescuecore2.version0.messages.AKSay;
import rescuecore2.version0.messages.AKTell;

/**
   Legacy agent implementation.
 */
public class LegacyAgent extends AbstractAgent<RescueEntity> {
    private int freezeTime;

    /**
       Construct a legacy agent.
       @param e The entity controlled by the agent.
       @param c The connection to the agent.
       @param freezeTime The number of timesteps before which movement commands will be ignored. Say and tell commands will work at all times.
     */
    public LegacyAgent(RescueEntity e, Connection c, int freezeTime) {
        super(e, c);
        this.freezeTime = freezeTime;
    }

    @Override
    public void sendPerceptionUpdate(int time, Collection<? extends RescueEntity> visible, Collection<? extends Message> comms) {
        KASense sense = new KASense(getControlledEntity().getID().getValue(), time, visible);
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
            else {
                // Only allow say and tell commands if it's too early
                if (timestep < freezeTime && !(next instanceof AKSay || next instanceof AKTell)) {
                    it.remove();
                }
            }
        }
        return result;
    }
}