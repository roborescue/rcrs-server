package kernel.log;

import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.readEntity;
import static rescuecore2.misc.EncodingTools.readMessage;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.DefaultWorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.Message;
import rescuecore2.messages.Command;
import rescuecore2.misc.Pair;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import java.io.InputStream;
import java.io.IOException;

/**
   An class for reading kernel logs from a stream.
 */
public class StreamLogReader implements LogReader {
    private int maxTime;
    private Map<Integer, Collection<Command>> commands;
    private Map<Integer, Collection<Entity>> updates;
    private Map<Integer, WorldModel<? extends Entity>> worldModels;

    /**
       Construct a StreamLogReader.
       @param in The InputStream to read.
       @throws KernelLogException If there is a problem reading the log.
     */
    public StreamLogReader(InputStream in) throws KernelLogException {
        commands = new HashMap<Integer, Collection<Command>>();
        updates = new HashMap<Integer, Collection<Entity>>();
        worldModels = new HashMap<Integer, WorldModel<? extends Entity>>();
        try {
            readLog(in);
        }
        catch (IOException e) {
            throw new KernelLogException(e);
        }
    }

    @Override
    public int getMaxTimestep() throws KernelLogException {
        return maxTime;
    }

    @Override
    public WorldModel<? extends Entity> getWorldModel(int time) throws KernelLogException {
        checkTime(time);
        WorldModel<? extends Entity> result = worldModels.get(time);
        if (result == null) {
            result = new DefaultWorldModel<Entity>(Entity.class);
        }
        return result;
    }

    @Override
    public Set<EntityID> getEntitiesWithUpdates(int time) throws KernelLogException {
        checkTime(time);
        return new HashSet<EntityID>();
    }

    @Override
    public Pair<Collection<Entity>, Collection<Message>> getEntityUpdates(int time, EntityID entity) throws KernelLogException {
        checkTime(time);
        return new Pair<Collection<Entity>, Collection<Message>>(new HashSet<Entity>(), new HashSet<Message>());
    }

    @Override
    public Collection<Command> getCommands(int time) throws KernelLogException {
        checkTime(time);
        Collection<Command> result = commands.get(time);
        if (result == null) {
            result = new HashSet<Command>();
        }
        return result;
    }

    @Override
    public Collection<Entity> getUpdates(int time) throws KernelLogException {
        checkTime(time);
        Collection<Entity> result = updates.get(time);
        if (result == null) {
            result = new HashSet<Entity>();
        }
        return result;
    }

    private void checkTime(int time) {
        if (time < 0 || time > maxTime) {
            throw new IllegalArgumentException("Time is out of range: " + time + " should be between 0 and " + maxTime);
        }
    }

    private void readLog(InputStream in) throws IOException, KernelLogException {
        int id = readInt32(in);
        RecordType type = RecordType.fromID(id);
        if (!RecordType.START_OF_LOG.equals(type)) {
            throw new KernelLogException("Log does not start with correct magic number");
        }
        do {
            id = readInt32(in);
            if (id != -1) {
                type = RecordType.fromID(id);
                readRecord(type, in);
            }
        } while (id != -1 && !RecordType.END_OF_LOG.equals(type));
    }

    private void readRecord(RecordType type, InputStream in) throws IOException, KernelLogException {
        switch (type) {
        case INITIAL_CONDITIONS:
            readInitialConditions(in);
            break;
        case PERCEPTION:
            readPerception(in);
            break;
        case COMMANDS:
            readCommands(in);
            break;
        case UPDATES:
            readUpdates(in);
            break;
        case END_OF_LOG:
            return;
        }
    }

    private void readInitialConditions(InputStream in) throws IOException, KernelLogException {
        int size = readInt32(in);
        if (size < 0) {
            throw new KernelLogException("Invalid initial conditions size: " + size);
        }
        System.out.print("Reading initial conditions. " + size + " objects to read...");
        WorldModel<? extends Entity> world = new DefaultWorldModel<Entity>(Entity.class);
        for (Entity next : readEntities(size, in)) {
            world.addEntity(next);
        }
        System.out.println("done");
        worldModels.put(0, world);
    }

    private void readPerception(InputStream in) throws IOException, KernelLogException {
        int agentID = readInt32(in);
        int time = readInt32(in);
        System.out.print("Reading perception for agent " + agentID + " at time " + time + "...");
        int visibleSize = readInt32(in);
        Set<Entity> visible = readEntities(visibleSize, in);
        int commsSize = readInt32(in);
        Set<Message> messages = readMessages(commsSize, in);
        System.out.println("done. Saw " + visibleSize + " entities and heard " + commsSize + " messages.");
        maxTime = Math.max(time, maxTime);
    }

    private void readCommands(InputStream in) throws IOException, KernelLogException {
        int time = readInt32(in);
        int size = readInt32(in);
        System.out.print("Reading commands for time " + time + ". " + size + " commands to read...");
        Set<Command> c = readCommands(size, in);
        commands.put(time, c);
        System.out.println("done");
        maxTime = Math.max(time, maxTime);
    }

    private void readUpdates(InputStream in) throws IOException, KernelLogException {
        int time = readInt32(in);
        int size = readInt32(in);
        System.out.print("Reading updates for time " + time + ". " + size + " entities to read...");
        Set<Entity> u = readEntities(size, in);
        updates.put(time, u);
        System.out.println("done");
        // Make the world model for this timestep
        WorldModel<? extends Entity> newWorld = new DefaultWorldModel<Entity>(Entity.class);
        WorldModel<? extends Entity> oldWorld = getWorldModel(time - 1);
        if (oldWorld != null) {
            newWorld.merge(oldWorld.getAllEntities());
        }
        newWorld.merge(u);
        worldModels.put(time, newWorld);
        maxTime = Math.max(time, maxTime);
    }

    private Set<Entity> readEntities(int size, InputStream in) throws IOException, KernelLogException {
        Set<Entity> result = new HashSet<Entity>(size);
        for (int i = 0; i < size; ++i) {
            Entity e = readEntity(in);
            if (e == null) {
                throw new KernelLogException("Could not read entity from stream");
            }
            result.add(e);
        }
        return result;
    }

    private Set<Message> readMessages(int size, InputStream in) throws IOException, KernelLogException {
        Set<Message> result = new HashSet<Message>(size);
        for (int i = 0; i < size; ++i) {
            Message m = readMessage(in);
            if (m == null) {
                throw new KernelLogException("Could not read message from stream");
            }
            result.add(m);
        }
        return result;
    }

    private Set<Command> readCommands(int size, InputStream in) throws IOException, KernelLogException {
        Set<Command> result = new HashSet<Command>(size);
        for (int i = 0; i < size; ++i) {
            Message m = readMessage(in);
            if (m == null) {
                throw new KernelLogException("Could not read message from stream");
            }
            if (m instanceof Command) {
                result.add((Command)m);
            }
        }
        return result;
    }
}