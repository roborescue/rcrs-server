package rescuecore2.log;

import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.readEntity;
import static rescuecore2.misc.EncodingTools.readMessage;
import static rescuecore2.misc.EncodingTools.readBytes;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.DefaultWorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.Message;
import rescuecore2.messages.Command;
import rescuecore2.misc.Pair;
import rescuecore2.config.Config;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;

/**
   An class for reading kernel logs from a stream.
 */
public class StreamLogReader implements LogReader {
    private int maxTime;
    private Map<Integer, CommandsRecord> commands;
    private Map<Integer, UpdatesRecord> updates;
    private Map<Integer, WorldModel<? extends Entity>> worldModels;
    private Map<Integer, Map<EntityID, PerceptionRecord>> perception;
    private Config config;

    /**
       Construct a StreamLogReader.
       @param in The InputStream to read.
       @throws LogException If there is a problem reading the log.
     */
    public StreamLogReader(InputStream in) throws LogException {
        commands = new HashMap<Integer, CommandsRecord>();
        updates = new HashMap<Integer, UpdatesRecord>();
        worldModels = new HashMap<Integer, WorldModel<? extends Entity>>();
        perception = new HashMap<Integer, Map<EntityID, PerceptionRecord>>();
        try {
            readLog(in);
        }
        catch (IOException e) {
            throw new LogException(e);
        }
    }

    @Override
    public Config getConfig() throws LogException {
        if (config == null) {
            throw new LogException("No config record found");
        }
        return config;
    }

    @Override
    public int getMaxTimestep() throws LogException {
        return maxTime;
    }

    @Override
    public WorldModel<? extends Entity> getWorldModel(int time) throws LogException {
        checkTime(time);
        WorldModel<? extends Entity> result = worldModels.get(time);
        if (result == null) {
            result = DefaultWorldModel.create();
        }
        return result;
    }

    @Override
    public Set<EntityID> getEntitiesWithUpdates(int time) throws LogException {
        checkTime(time);
        Map<EntityID, PerceptionRecord> agentData = perception.get(time);
        Set<EntityID> result = new HashSet<EntityID>();
        if (agentData != null) {
            result.addAll(agentData.keySet());
        }
        return result;
    }

    @Override
    public PerceptionRecord getPerception(int time, EntityID entity) throws LogException {
        checkTime(time);
        Map<EntityID, PerceptionRecord> agentData = perception.get(time);
        if (agentData == null) {
            return null;
        }
        PerceptionRecord result = agentData.get(entity);
        return result;
    }

    @Override
    public CommandsRecord getCommands(int time) throws LogException {
        checkTime(time);
        return commands.get(time);
    }

    @Override
    public UpdatesRecord getUpdates(int time) throws LogException {
        checkTime(time);
        return updates.get(time);
    }

    private void checkTime(int time) {
        if (time < 0 || time > maxTime) {
            throw new IllegalArgumentException("Time is out of range: " + time + " should be between 0 and " + maxTime);
        }
    }

    private void readLog(InputStream in) throws IOException, LogException {
        int id;
        RecordType type;
        boolean startFound = false;
        do {
            id = readInt32(in);
            type = RecordType.fromID(id);
            if (!startFound) {
                if (type != RecordType.START_OF_LOG) {
                    throw new LogException("Log does not start with correct magic number");
                }
                startFound = true;
            }
            readRecord(type, in);
        }
        while (type != RecordType.END_OF_LOG);
    }

    private void readRecord(RecordType type, InputStream in) throws IOException, LogException {
        int size = readInt32(in);
        byte[] data = readBytes(size, in);
        InputStream d = new ByteArrayInputStream(data);
        switch (type) {
        case INITIAL_CONDITIONS:
            readInitialConditions(d);
            break;
        case PERCEPTION:
            readPerception(d);
            break;
        case COMMANDS:
            readCommands(d);
            break;
        case UPDATES:
            readUpdates(d);
            break;
        case END_OF_LOG:
            return;
        default:
            throw new LogException("Unexpected record type: " + type);
        }
    }

    private void readInitialConditions(InputStream in) throws IOException, LogException {
        InitialConditionsRecord record = new InitialConditionsRecord(in);
        worldModels.put(0, record.getWorldModel());
    }

    private void readPerception(InputStream in) throws IOException, LogException {
        /*
        int agentID = readInt32(in);
        int time = readInt32(in);
        System.out.print("Reading perception for agent " + agentID + " at time " + time + "...");
        int visibleSize = readInt32(in);
        Set<Entity> visible = readEntities(visibleSize, in);
        int commsSize = readInt32(in);
        Set<Message> messages = readMessages(commsSize, in);
        System.out.println("done. Saw " + visibleSize + " entities and heard " + commsSize + " messages.");
        maxTime = Math.max(time, maxTime);
        Pair<Collection<Entity>, Collection<Message>> data = new Pair<Collection<Entity>, Collection<Message>>(visible, messages);
        Map<EntityID, Pair<Collection<Entity>, Collection<Message>>> agentData = perception.get(time);
        if (agentData == null) {
            agentData = new HashMap<EntityID, Pair<Collection<Entity>, Collection<Message>>>();
            perception.put(time, agentData);
        }
        agentData.put(new EntityID(agentID), data);
        */
    }

    private void readCommands(InputStream in) throws IOException, LogException {
        /*
        int time = readInt32(in);
        int size = readInt32(in);
        System.out.print("Reading commands for time " + time + ". " + size + " commands to read...");
        Set<Command> c = readCommands(size, in);
        commands.put(time, c);
        System.out.println("done");
        maxTime = Math.max(time, maxTime);
        */
    }

    private void readUpdates(InputStream in) throws IOException, LogException {
        /*
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
            Set<Entity> copy = new HashSet<Entity>();
            for (Entity next : oldWorld) {
                copy.add(next.copy());
            }
            newWorld.merge(copy);
        }
        newWorld.merge(u);
        worldModels.put(time, newWorld);
        maxTime = Math.max(time, maxTime);
        */
    }

    private Set<Entity> readEntities(int size, InputStream in) throws IOException, LogException {
        /*
        Set<Entity> result = new HashSet<Entity>(size);
        for (int i = 0; i < size; ++i) {
            Entity e = readEntity(in);
            if (e == null) {
                throw new LogException("Could not read entity from stream");
            }
            result.add(e);
        }
        return result;
        */
        return null;
    }

    private Set<Message> readMessages(int size, InputStream in) throws IOException, LogException {
        /*
        Set<Message> result = new HashSet<Message>(size);
        for (int i = 0; i < size; ++i) {
            Message m = readMessage(in);
            if (m == null) {
                throw new LogException("Could not read message from stream");
            }
            result.add(m);
        }
        return result;
        */
        return null;
    }

    private Set<Command> readCommands(int size, InputStream in) throws IOException, LogException {
        /*
        Set<Command> result = new HashSet<Command>(size);
        for (int i = 0; i < size; ++i) {
            Message m = readMessage(in);
            if (m == null) {
                throw new LogException("Could not read message from stream");
            }
            if (m instanceof Command) {
                result.add((Command)m);
            }
        }
        return result;
        */
        return null;
    }
}