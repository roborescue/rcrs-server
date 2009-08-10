package kernel.log;

import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.readEntity;
import static rescuecore2.misc.EncodingTools.readMessage;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.NavigableMap;
import java.util.TreeMap;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.DefaultWorldModel;
import rescuecore2.messages.Message;
import rescuecore2.messages.Command;
import rescuecore2.misc.Pair;

/**
   A log reader that reads from a file.
 */
public class FileLogReader implements LogReader {
    private static final int KEY_FRAME_BUFFER_MAX_SIZE = 10;

    private RandomAccessFile file;
    private int maxTime;
    private NavigableMap<Integer, WorldModel<? extends Entity>> keyFrames;
    private Map<Integer, Map<EntityID, Long>> perceptionIndices;
    private Map<Integer, Long> updatesIndices;
    private Map<Integer, Long> commandsIndices;

    /**
       Construct a new FileLogReader.
       @param name The name of the file to read.
       @throws IOException If the file cannot be read.
       @throws KernelLogException If there is a problem reading the log.
    */
    public FileLogReader(String name) throws IOException, KernelLogException {
        this(new File(name));
    }

    /**
       Construct a new FileLogReader.
       @param file The file object to read.
       @throws IOException If the file cannot be read.
       @throws KernelLogException If there is a problem reading the log.
    */
    public FileLogReader(File file) throws IOException, KernelLogException {
        this.file = new RandomAccessFile(file, "r");
        index();
    }

    @Override
    public int getMaxTimestep() throws KernelLogException {
        return maxTime;
    }

    @Override
    public WorldModel<? extends Entity> getWorldModel(int time) throws KernelLogException {
        System.out.println("Getting world model at time " + time);
        WorldModel<? extends Entity> result = new DefaultWorldModel<Entity>(Entity.class);
        // Look for a key frame
        Map.Entry<Integer, WorldModel<? extends Entity>> entry = keyFrames.floorEntry(time);
        int startTime = entry.getKey();
        System.out.println("Found key frame " + startTime);
        // Copy the initial conditions
        System.out.println("Cloning initial conditions");
        for (Entity next : entry.getValue()) {
            result.addEntity(next.copy());
        }
        // Go through updates and apply them all
        for (int i = startTime + 1; i <= time; ++i) {
            Collection<Entity> updates = getUpdates(time);
            System.out.println("Merging " + updates.size() + " updates for timestep " + i);
            result.merge(updates);
        }
        System.out.println("Done");
        // Remove stale key frames
        removeStaleKeyFrames();
        // Store this as a key frame - it's quite likely that the next timestep will be viewed soon.
        keyFrames.put(time, result);
        return result;
    }

    @Override
    public Set<EntityID> getEntitiesWithUpdates(int time) throws KernelLogException {
        Map<EntityID, Long> timestepMap = perceptionIndices.get(time);
        if (timestepMap == null) {
            return new HashSet<EntityID>();
        }
        return timestepMap.keySet();
    }

    @Override
    public Pair<Collection<Entity>, Collection<Message>> getEntityUpdates(int time, EntityID entity) throws KernelLogException {
        try {
            System.out.print("Reading perception for agent " + entity + " at time " + time + "...");
            Map<EntityID, Long> timestepMap = perceptionIndices.get(time);
            if (timestepMap == null) {
                return null;
            }
            Long l = timestepMap.get(entity);
            if (l == null) {
                return null;
            }
            file.seek(l);
            int visibleSize = readInt32(file);
            Set<Entity> visible = readEntities(visibleSize);
            int commsSize = readInt32(file);
            Set<Message> messages = readMessages(commsSize);
            System.out.println("done. Saw " + visibleSize + " entities and heard " + commsSize + " messages.");
            return new Pair<Collection<Entity>, Collection<Message>>(visible, messages);
        }
        catch (IOException e) {
            throw new KernelLogException(e);
        }
    }

    @Override
    public Collection<Command> getCommands(int time) throws KernelLogException {
        try {
            Long l = commandsIndices.get(time);
            if (l == null) {
                return new HashSet<Command>();
            }
            file.seek(l);
            int size = readInt32(file);
            System.out.print("Reading commands for time " + time + ". " + size + " commands to read...");
            Set<Command> c = readCommands(size);
            System.out.println("done");
            return c;
        }
        catch (IOException e) {
            throw new KernelLogException(e);
        }

    }

    @Override
    public Collection<Entity> getUpdates(int time) throws KernelLogException {
        try {
            Long l = updatesIndices.get(time);
            if (l == null) {
                return new HashSet<Entity>();
            }
            file.seek(l);
            int size = readInt32(file);
            System.out.print("Reading updates for time " + time + ". " + size + " updates to read...");
            Set<Entity> e = readEntities(size);
            System.out.println("done");
            return e;
        }
        catch (IOException e) {
            throw new KernelLogException(e);
        }
    }

    private void index() throws KernelLogException {
        try {
            keyFrames = new TreeMap<Integer, WorldModel<? extends Entity>>();
            perceptionIndices = new HashMap<Integer, Map<EntityID, Long>>();
            updatesIndices = new HashMap<Integer, Long>();
            commandsIndices = new HashMap<Integer, Long>();
            file.seek(0);
            int id = readInt32(file);
            RecordType type = RecordType.fromID(id);
            if (!RecordType.START_OF_LOG.equals(type)) {
                throw new KernelLogException("Log does not start with correct magic number");
            }
            do {
                id = readInt32(file);
                if (id != -1) {
                    type = RecordType.fromID(id);
                    indexRecord(type);
                }
            } while (id != -1 && !RecordType.END_OF_LOG.equals(type));
        }
        catch (IOException e) {
            throw new KernelLogException(e);
        }
    }

    private void indexRecord(RecordType type) throws IOException, KernelLogException {
        switch (type) {
        case INITIAL_CONDITIONS:
            indexInitialConditions();
            break;
        case PERCEPTION:
            indexPerception();
            break;
        case COMMANDS:
            indexCommands();
            break;
        case UPDATES:
            indexUpdates();
            break;
        case END_OF_LOG:
            return;
        default:
            throw new KernelLogException("Unexpected record type: " + type);
        }
    }

    private void indexInitialConditions() throws IOException, KernelLogException {
        int size = readInt32(file);
        if (size < 0) {
            throw new KernelLogException("Invalid initial conditions size: " + size);
        }
        System.out.print("Reading initial conditions. " + size + " objects to read...");
        WorldModel<? extends Entity> initialConditions = new DefaultWorldModel<Entity>(Entity.class);
        for (Entity next : readEntities(size)) {
            initialConditions.addEntity(next);
        }
        System.out.println("done");
        keyFrames.put(0, initialConditions);
    }

    private void indexPerception() throws IOException, KernelLogException {
        int agentID = readInt32(file);
        int time = readInt32(file);
        long position = file.getFilePointer();
        System.out.println("Found perception for agent " + agentID + " at time " + time + " at position " + position);
        Map<EntityID, Long> timestepMap = perceptionIndices.get(time);
        if (timestepMap == null) {
            timestepMap = new HashMap<EntityID, Long>();
            perceptionIndices.put(time, timestepMap);
        }
        timestepMap.put(new EntityID(agentID), position);
        // Skip over the content
        int visibleSize = readInt32(file);
        readEntities(visibleSize);
        int commsSize = readInt32(file);
        readMessages(commsSize);
        System.out.println("Skipped " + visibleSize + " entities and " + commsSize + " messages");
    }

    private void indexCommands() throws IOException, KernelLogException {
        int time = readInt32(file);
        long position = file.getFilePointer();
        System.out.println("Found commands for time " + time + " at position " + position);
        commandsIndices.put(time, position);
        maxTime = Math.max(time, maxTime);
        // Skip the content
        int size = readInt32(file);
        readCommands(size);
        System.out.println("Skipped " + size + " commands");
    }

    private void indexUpdates() throws IOException, KernelLogException {
        int time = readInt32(file);
        long position = file.getFilePointer();
        System.out.println("Found updates for time " + time + " at position " + position);
        updatesIndices.put(time, position);
        maxTime = Math.max(time, maxTime);
        int size = readInt32(file);
        readEntities(size);
        System.out.println("Skipped " + size + " updates.");
    }

    private Set<Entity> readEntities(int size) throws IOException, KernelLogException {
        Set<Entity> result = new HashSet<Entity>(size);
        for (int i = 0; i < size; ++i) {
            Entity e = readEntity(file);
            if (e == null) {
                throw new KernelLogException("Could not read entity from stream");
            }
            result.add(e);
        }
        return result;
    }

    private Set<Message> readMessages(int size) throws IOException, KernelLogException {
        Set<Message> result = new HashSet<Message>(size);
        for (int i = 0; i < size; ++i) {
            Message m = readMessage(file);
            if (m == null) {
                throw new KernelLogException("Could not read message from stream");
            }
            result.add(m);
        }
        return result;
    }

    private Set<Command> readCommands(int size) throws IOException, KernelLogException {
        Set<Command> result = new HashSet<Command>(size);
        for (int i = 0; i < size; ++i) {
            Message m = readMessage(file);
            if (m == null) {
                throw new KernelLogException("Could not read message from stream");
            }
            if (m instanceof Command) {
                result.add((Command)m);
            }
        }
        return result;
    }

    private void removeStaleKeyFrames() {
        System.out.println("Removing stale key frames");
        if (keyFrames.size() < KEY_FRAME_BUFFER_MAX_SIZE) {
            System.out.println("Key frame buffer is not full: " + keyFrames.size() + " entries");
            return;
        }
        // Try to balance the number of key frames.
        int window = maxTime / KEY_FRAME_BUFFER_MAX_SIZE;
        for (int i = 0; i < maxTime; i += window) {
            NavigableMap<Integer, WorldModel<? extends Entity>> next = keyFrames.subMap(i, false, i + window, true);
            System.out.println("Window " + i + " -> " + (i + window) + " has " + next.size() + " entries");
            if (next.size() > 1) {
                // Remove all but the last entry in this window
                Map.Entry<Integer, WorldModel<? extends Entity>> last = next.lastEntry();
                next.clear();
                next.put(last.getKey(), last.getValue());
                System.out.println("Retained entry " + last);
            }
        }
        System.out.println("New key frame set: " + keyFrames);
    }
}