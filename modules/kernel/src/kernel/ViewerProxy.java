package kernel;

import rescuecore2.connection.Connection;
import rescuecore2.messages.Message;
import rescuecore2.messages.control.KVTimestep;
//import rescuecore2.messages.control.KASense;
import rescuecore2.Timestep;

import java.util.List;
import java.util.ArrayList;

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
       Send a Timestep structure to this viewer.
       @param time The Timestep to send.
    */
    public void sendTimestep(Timestep time) {
        List<Message> messages = new ArrayList<Message>();
        /*
        for (EntityID next : time.getAgentsWithUpdates()) {
            ChangeSet changes = time.getAgentPerception(next);
            Collection<Command> hearing = time.getAgentHearing(next);
            messages.add(new KASense(next, time.getTime(), changes, hearing));
        }
        */
        //        messages.add(new Commands(id, time.getTime(), time.getCommands()));
        //        messages.add(new Update(id, time.getTime(), time.getChangeSet()));
        messages.add(new KVTimestep(id, time.getTime(), time.getCommands(), time.getChangeSet()));
        send(messages);
    }

    @Override
    public String toString() {
        return getName() + " (" + id + "): " + getConnection().toString();
    }
}