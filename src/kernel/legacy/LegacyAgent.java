package kernel.legacy;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import kernel.RemoteAgent;

import rescuecore2.connection.Connection;
import rescuecore2.messages.Command;
import rescuecore2.version0.entities.RescueEntity;
import rescuecore2.version0.messages.KASense;
import rescuecore2.version0.messages.AgentCommand;
import rescuecore2.version0.messages.AKSay;
import rescuecore2.version0.messages.AKTell;

public class LegacyAgent extends RemoteAgent<RescueEntity> {
    private int freezeTime;

    public LegacyAgent(RescueEntity e, Connection c, int freezeTime) {
        super(e, c);
        this.freezeTime = freezeTime;
    }

    @Override
    public void sendPerceptionUpdate(int time, Collection<? extends RescueEntity> visible) {
        KASense sense = new KASense(getControlledEntity().getID().getValue(), time, visible);
        sendMessages(Collections.singleton(sense));
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