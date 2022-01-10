package rescuecore2.log;

import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.readBytes;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.DefaultWorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.config.Config;
import rescuecore2.messages.protobuf.RCRSLogProto.LogProto;
import rescuecore2.messages.protobuf.RCRSLogProto.LogProto.LogCase;
import rescuecore2.registry.Registry;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.tukaani.xz.LZMAInputStream;
import org.tukaani.xz.XZInputStream;

import java.util.HashSet;

import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;

/**
   An class for reading kernel logs from a stream.
 */
public class StreamLogReader extends AbstractLogReader {
    private int maxTime;
    private Map<Integer, CommandsRecord> commands;
    private Map<Integer, UpdatesRecord> updates;
    private Map<Integer, WorldModel<? extends Entity>> worldModels;
    private Map<Integer, Map<EntityID, PerceptionRecord>> perception;
    private Config config;

    /**
       Construct a StreamLogReader.
       @param in The InputStream to read.
       @param registry The registry to use for reading log entries.
       @throws LogException If there is a problem reading the log.
     */
    public StreamLogReader(InputStream in, Registry registry) throws LogException {
        super(registry);
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
        Registry.setCurrentRegistry(registry);
		InputStream gin = new LZMAInputStream(in);
        readLogProto(gin);
//        readLogV1(in);
    }
    
    private void readLogProto(InputStream in) throws IOException, LogException {
        LogCase type;
        boolean startFound = false;
        do {
            int size = readInt32(in);
            byte[] bytes = in.readNBytes(size);
            LogProto log = LogProto.parseFrom(bytes);
            type = log.getLogCase();
            if (!startFound) {
                if (type != LogCase.START) {
                    throw new LogException("Log does not start with correct magic number");
                }
                startFound = true;
            }
            readRecord(type, log);
        }
        while (type != LogCase.END);

	}

	private void readRecord(LogCase type, LogProto log) throws LogException {
		Registry.setCurrentRegistry(registry);
        switch (type) {
        case INITIALCONDITION:
            readInitialConditions(log);
            break;
        case PERCEPTION:
            readPerception(log);
            break;
        case COMMAND:
            readCommands(log);
            break;
        case UPDATE:
            readUpdates(log);
            break;
        case CONFIG:
            readConfig(log);
            break;
        case END:
            return;
        case START:
            return;
        default:
            throw new LogException("Unexpected record type: " + type);
        }
		
	}

	private void readLogV1(InputStream in) throws IOException, LogException {
        Registry.setCurrentRegistry(registry);
        
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
        case CONFIG:
            readConfig(d);
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
	private void readInitialConditions(LogProto log) throws LogException {
		InitialConditionsRecord record =new InitialConditionsRecord(log);
		worldModels.put(0, record.getWorldModel());
	}

    private void readPerception(InputStream in) throws IOException, LogException {
        PerceptionRecord record = new PerceptionRecord(in);
        int time = record.getTime();
        Map<EntityID, PerceptionRecord> agentData = perception.get(time);
        if (agentData == null) {
            agentData = new HashMap<EntityID, PerceptionRecord>();
            perception.put(time, agentData);
        }
        agentData.put(record.getEntityID(), record);
        maxTime = Math.max(time, maxTime);
    }
    private void readPerception(LogProto log) {
        PerceptionRecord record = new PerceptionRecord(log);
        int time = record.getTime();
        Map<EntityID, PerceptionRecord> agentData = perception.get(time);
        if (agentData == null) {
            agentData = new HashMap<EntityID, PerceptionRecord>();
            perception.put(time, agentData);
        }
        agentData.put(record.getEntityID(), record);
        maxTime = Math.max(time, maxTime);
    }

    private void readCommands(InputStream in) throws IOException, LogException {
        CommandsRecord record = new CommandsRecord(in);
        commands.put(record.getTime(), record);
        maxTime = Math.max(record.getTime(), maxTime);
    }

    private void readCommands(LogProto log) {
        CommandsRecord record = new CommandsRecord(log);
        commands.put(record.getTime(), record);
        maxTime = Math.max(record.getTime(), maxTime);
    }
    private void readUpdates(InputStream in) throws IOException, LogException {
        UpdatesRecord record = new UpdatesRecord(in);
        int time = record.getTime();
        updates.put(time, record);
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
        newWorld.merge(record.getChangeSet());
        worldModels.put(time, newWorld);
        maxTime = Math.max(time, maxTime);
    }
    private void readUpdates(LogProto log) throws LogException {
        UpdatesRecord record = new UpdatesRecord(log);
        int time = record.getTime();
        updates.put(time, record);
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
        newWorld.merge(record.getChangeSet());
        worldModels.put(time, newWorld);
        maxTime = Math.max(time, maxTime);
    }

    private void readConfig(InputStream in) throws IOException, LogException {
        ConfigRecord record = new ConfigRecord(in);
        config = record.getConfig();
    }
    private void readConfig(LogProto log)  {
        ConfigRecord record = new ConfigRecord(log);
        config = record.getConfig();
    }

}
