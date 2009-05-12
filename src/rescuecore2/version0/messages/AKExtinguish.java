package rescuecore2.version0.messages;

import static rescuecore2.misc.EncodingTools.writeInt32;
import static rescuecore2.misc.EncodingTools.readInt32;

import rescuecore2.worldmodel.EntityID;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
   An agent Extinguish command.
 */
public class AKExtinguish extends AgentCommand {
    private EntityID target;
    private int water;

    /**
       Create an empty AKExtinguish command.
     */
    AKExtinguish() {
        super("AK_EXTINGUISH", MessageConstants.AK_EXTINGUISH);
        init();
    }

    /**
       Construct an AKExtinguish command.
       @param agent The ID of the agent issuing the command.
       @param target The id of the entity to extinguish.
       @param water The amount of water to use.
     */
    public AKExtinguish(EntityID agent, EntityID target, int water) {
        super("AK_EXTINGUISH", MessageConstants.AK_EXTINGUISH, agent);
        init();
        this.target = target;
        this.water = water;
    }

    /**
       Get the desired target.
       @return The target ID.
     */
    public EntityID getTarget() {
        return target;
    }

    /**
       Get the amount of water.
       @return The amount of water to use.
    */
    public int getWater() {
        return water;
    }

    @Override
    public void write(OutputStream out) throws IOException {
        writeInt32(getAgentID().getValue(), out);
        writeInt32(target.getValue(), out);
        writeInt32(0, out); // Direction
        writeInt32(0, out); // X
        writeInt32(0, out); // Y
        writeInt32(water, out);
        writeInt32(0, out); // End-of-nozzles marker
    }

    @Override
    public void read(InputStream in) throws IOException {
        setAgentID(new EntityID(readInt32(in)));
        target = new EntityID(readInt32(in));
        readInt32(in); // Direction
        readInt32(in); // X
        readInt32(in); // Y
        water = readInt32(in);
        // Check end-of-nozzles marker
        while (readInt32(in) != 0) {
            // More nozzles
            // Skip direction, x, y, water
            readInt32(in);
            readInt32(in);
            readInt32(in);
            readInt32(in);
        }
    }

    private void init() {
    }
}