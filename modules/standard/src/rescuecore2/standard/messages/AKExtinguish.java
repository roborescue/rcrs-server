package rescuecore2.standard.messages;

import static rescuecore2.misc.EncodingTools.writeInt32;
import static rescuecore2.misc.EncodingTools.readInt32;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.AbstractCommand;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
   An agent Extinguish command.
 */
public class AKExtinguish extends AbstractCommand {
    private EntityID target;
    private int water;

    /**
       An AKExtinguish message that populates its data from a stream.
       @param in The InputStream to read.
       @throws IOException If there is a problem reading the stream.
     */
    public AKExtinguish(InputStream in) throws IOException {
        this();
        read(in);
    }

    /**
       Construct an AKExtinguish command.
       @param agent The ID of the agent issuing the command.
       @param time The time the command was issued.
       @param target The id of the entity to extinguish.
       @param water The amount of water to use.
     */
    public AKExtinguish(EntityID agent, int time, EntityID target, int water) {
        this();
        setAgentID(agent);
        setTime(time);
        this.target = target;
        this.water = water;
    }

    private AKExtinguish() {
        super("AK_EXTINGUISH", MessageConstants.AK_EXTINGUISH);
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
        writeInt32(getTime(), out);
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
        setTime(readInt32(in));
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
}