package kernel;

import rescuecore2.connection.Connection;
import rescuecore2.messages.Command;
import rescuecore2.messages.control.Update;
import rescuecore2.messages.control.Commands;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.ChangeSet;

import java.util.Collection;
import java.util.Collections;

/**
   This class is the kernel interface to a viewer.
 */
public class ViewerProxy extends AbstractKernelComponent {
    private int id;

    /**
       Construct a viewer.
       @param name The name of the viewer.
       @param id The ID of the viewer.
       @param c The connection to the viewer.
     */
    public ViewerProxy(String name, int id, Connection c) {
        super(name, c);
        this.id = id;
    }

    /**
       Send an update message to this viewer.
       @param time The simulation time.
       @param updates The updated entities.
    */
    public void sendUpdate(int time, ChangeSet updates) {
        send(Collections.singleton(new Update(id, time, updates)));
    }

    /**
       Send a set of agent commands to this viewer.
       @param time The current time.
       @param commands The agent commands to send.
     */
    public void sendAgentCommands(int time, Collection<? extends Command> commands) {
        send(Collections.singleton(new Commands(id, time, commands)));
    }

    @Override
    public String toString() {
        return getName() + " (" + id + "): " + getConnection().toString();
    }
}