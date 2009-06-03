package kernel.legacy;

import kernel.DefaultSimulator;

import rescuecore2.connection.Connection;

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