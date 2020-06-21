package kernel;

import rescuecore2.connection.Connection;
import rescuecore2.messages.control.KVTimestep;
import rescuecore2.Timestep;

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
        send(new KVTimestep(id, time.getTime(), time.getCommands(), time.getChangeSet()));
    }

    @Override
    public String toString() {
        return getName() + " (" + id + "): " + getConnection().toString();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
