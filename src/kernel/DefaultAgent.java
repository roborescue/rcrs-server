package kernel;

import java.util.Collection;
import java.util.ArrayList;

import rescuecore2.connection.Connection;
import rescuecore2.messages.Message;
import rescuecore2.messages.control.KASense;
import rescuecore2.worldmodel.Entity;

/**
   Default agent implementation.
 */
public class DefaultAgent extends AbstractAgent {
    /**
       Construct a default agent.
       @param e The entity controlled by the agent.
       @param c The connection to the agent.
     */
    public DefaultAgent(Entity e, Connection c) {
        super(e, c);
    }

    @Override
    public void sendPerceptionUpdate(int time, Collection<? extends Entity> visible, Collection<? extends Message> comms) {
        KASense sense = new KASense(getControlledEntity().getID(), time, visible);
        Collection<Message> all = new ArrayList<Message>();
        all.add(sense);
        all.addAll(comms);
        send(all);
    }
}