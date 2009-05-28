package kernel.legacy;

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;

import kernel.AbstractSimulator;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.messages.Message;
import rescuecore2.messages.Command;
import rescuecore2.version0.entities.RescueEntity;
import rescuecore2.version0.messages.Update;
import rescuecore2.version0.messages.Commands;
import rescuecore2.version0.messages.SKUpdate;
import rescuecore2.version0.messages.AgentCommand;

public class LegacySimulator extends AbstractSimulator<RescueEntity, IndexedWorldModel> {
    private int id;

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
        System.out.println("Sending commands at time " + time + ": " + commands);
        Collection<AgentCommand> cmd = new ArrayList<AgentCommand>();
        for (Command next : commands) {
            if (next instanceof AgentCommand) {
                cmd.add((AgentCommand)next);
            }
        }
        send(Collections.singleton(new Commands(time, cmd)));
    }

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