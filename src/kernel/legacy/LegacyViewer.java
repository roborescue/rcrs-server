package kernel.legacy;

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;

import kernel.AbstractViewer;

import rescuecore2.connection.Connection;
import rescuecore2.messages.Command;
import rescuecore2.version0.entities.RescueEntity;
import rescuecore2.version0.messages.Update;
import rescuecore2.version0.messages.Commands;
import rescuecore2.version0.messages.AgentCommand;

/**
   Legacy viewer implementation.
 */
public class LegacyViewer extends AbstractViewer<RescueEntity> {
    /**
       Construct a legacy viewer.
       @param c The connection to the viewer.
     */
    public LegacyViewer(Connection c) {
        super(c);
    }

    @Override
    public void sendUpdate(int time, Collection<? extends RescueEntity> updates) {
        send(Collections.singleton(new Update(time, updates)));
    }

    @Override
    public void sendAgentCommands(int time, Collection<? extends Command> commands) {
        Collection<AgentCommand> cmd = new ArrayList<AgentCommand>();
        for (Command next : commands) {
            if (next instanceof AgentCommand) {
                cmd.add((AgentCommand)next);
            }
        }
        send(Collections.singleton(new Commands(time, cmd)));
    }
}