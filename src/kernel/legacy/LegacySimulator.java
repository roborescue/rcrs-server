package kernel.legacy;

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;

import kernel.DefaultSimulator;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.messages.Message;
import rescuecore2.messages.Command;
import rescuecore2.messages.control.SKUpdate;
import rescuecore2.messages.control.Update;
import rescuecore2.messages.control.Commands;

/**
   Version0 simulator implementation.
 */
public class LegacySimulator extends DefaultSimulator {
    private int id;

    /**
       Construct a legacy simulator.
       @param c The connection to the simulator.
       @param id The ID of the simulator.
     */
    public LegacySimulator(Connection c, int id) {
        super(c);
        this.id = id;
    }

    @Override
    public String toString() {
        return "Simulator " + id + ": " + getConnection().toString();
    }

    /**
       Get the ID of this simulator.
       @return The id of this simulator.
     */
    public int getID() {
        return id;
    }
}