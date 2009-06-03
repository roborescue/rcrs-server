package kernel;

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.messages.Message;
import rescuecore2.messages.Command;
import rescuecore2.messages.control.SKUpdate;
import rescuecore2.messages.control.Update;
import rescuecore2.messages.control.Commands;
import rescuecore2.worldmodel.Entity;

/**
   Version0 simulator implementation.
 */
public class DefaultSimulator extends AbstractSimulator {
    /**
       Construct a default simulator.
       @param c The connection to the simulator.
     */
    public DefaultSimulator(Connection c) {
        super(c);
        c.addConnectionListener(new SimulatorConnectionListener());
    }

    @Override
    public void sendUpdate(int time, Collection<? extends Entity> updates) {
        send(Collections.singleton(new Update(time, updates)));
    }

    @Override
    public void sendAgentCommands(int time, Collection<? extends Command> commands) {
        send(Collections.singleton(new Commands(time, commands)));
    }

    @Override
    public String toString() {
        return "Simulator: " + getConnection().toString();
    }

    private class SimulatorConnectionListener implements ConnectionListener {
        @Override
        public void messageReceived(Connection connection, Message msg) {
            if (msg instanceof SKUpdate) {
                System.out.println("Received simulator update: " + msg);
                SKUpdate update = (SKUpdate)msg;
                updateReceived(update.getTime(), update.getUpdatedEntities());
            }
        }
    }
}