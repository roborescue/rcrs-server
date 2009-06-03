package kernel;

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;

import rescuecore2.connection.Connection;
import rescuecore2.messages.Command;
import rescuecore2.messages.control.Update;
import rescuecore2.messages.control.Commands;
import rescuecore2.worldmodel.Entity;

/**
   Default viewer implementation.
 */
public class DefaultViewer extends AbstractViewer {
    /**
       Construct a default viewer.
       @param c The connection to the viewer.
     */
    public DefaultViewer(Connection c) {
        super(c);
    }

    @Override
    public void sendUpdate(int time, Collection<? extends Entity> updates) {
        send(Collections.singleton(new Update(time, updates)));
    }

    @Override
    public void sendAgentCommands(int time, Collection<? extends Command> commands) {
        send(Collections.singleton(new Commands(time, commands)));
    }
}