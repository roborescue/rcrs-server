package kernel.legacy;

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;

import kernel.AbstractSimulator;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.messages.Message;
import rescuecore2.messages.Command;
import rescuecore2.messages.control.SKUpdate;
import rescuecore2.version0.entities.RescueEntity;
import rescuecore2.version0.messages.Update;
import rescuecore2.version0.messages.Commands;
import rescuecore2.version0.messages.AgentCommand;

/**
   Version0 simulator implementation.
 */
public class LegacySimulator extends AbstractSimulator<RescueEntity> {
    private int id;

    /**
       Construct a legacy simulator.
       @param c The connection to the simulator.
       @param id The ID of the simulator.
     */
    public LegacySimulator(Connection c, int id) {
        super(c);
        c.addConnectionListener(new SimulatorConnectionListener());
        this.id = id;
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

    @Override
    public String toString() {
        return "Simulator " + id + ": " + super.toString();
    }

    /**
       Get the ID of this simulator.
       @return The id of this simulator.
     */
    public int getID() {
        return id;
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