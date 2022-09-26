package rescuecore2.log;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import rescuecore2.config.Config;
import rescuecore2.messages.protobuf.RCRSLogProto.LogProto;
import rescuecore2.registry.Registry;
import rescuecore2.worldmodel.DefaultWorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;

public class ZipLogReader extends AbstractLogReader {

	private SevenZFile sevenZFile;
	private Map<String, SevenZArchiveEntry> entries = new HashMap<>();
	private Map<Integer, WorldModel<? extends Entity>> worldModels = new HashMap<>();
	private Map<Integer, Set<EntityID>> entitiesWithUpdates = new HashMap<>();
	private int maxCycle = 0;

	public ZipLogReader(String path, Registry registry) throws LogException {
		this(new File(path), registry);
	}

	public ZipLogReader(File file, Registry registry) throws LogException {
		super(registry);
		try {
			sevenZFile = new SevenZFile(file);
		} catch (IOException e) {
			throw new LogException(e);
		}

		for (SevenZArchiveEntry entry : sevenZFile.getEntries()) {
			if (!entry.isDirectory()) {
				entries.put(entry.getName(), entry);
				if (entry.getName().contains(RecordType.UPDATES.name())) {
					String[] splt = entry.getName().split("/");
					int c = Integer.parseInt(splt[0]);
					maxCycle = Math.max(maxCycle, c);
				} else if (entry.getName()
						.contains(RecordType.PERCEPTION.name())) {
					String[] splt = entry.getName().split("/");
					int time = Integer.parseInt(splt[0]);
					if (!entitiesWithUpdates.containsKey(time))
						entitiesWithUpdates.put(time, new HashSet<>());
					int id = Integer.parseInt(splt[splt.length - 1]);
					entitiesWithUpdates.get(time).add(new EntityID(id));
				}

			}
		}
		Logger.info("Found " + maxCycle + " cycles.");
//		System.out.println(entitiesWithUpdates);
//		System.out.println(entries);
		buildWorldModels();
	}

	private void buildWorldModels() throws LogException {
		worldModels.put(0, getInitialConditions().getWorldModel());
		for (int i = 1; i <= getMaxTimestep(); i++) {
			buildWorldModelForTime(i);
		}
	}

	private void buildWorldModelForTime(int time) throws LogException {
		UpdatesRecord record = getUpdates(time);

		if (time % 10 == 0)
			System.out.println(time);
		Logger.info("Building worldmodel of " + time + ".");

		// Make the world model for this timestep
		WorldModel<? extends Entity> newWorld = new DefaultWorldModel<Entity>(
				Entity.class);
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
	}

	private LogProto readFromFile(String path) throws LogException {
		SevenZArchiveEntry entry = entries.get(path);
		if (entry == null)
			return null;
		byte[] content = new byte[(int) entry.getSize()];
		try {
			sevenZFile.getInputStream(entry).read(content, 0, content.length);
			return LogProto.parseFrom(content);
		} catch (IOException e) {
			throw new LogException(e);
		}

	}

	@Override
	public Config getConfig() throws LogException {
		return new ConfigRecord(readFromFile(RecordType.CONFIG.name()))
				.getConfig();
	}

	private InitialConditionsRecord getInitialConditions() throws LogException {
		return new InitialConditionsRecord(
				readFromFile(RecordType.INITIAL_CONDITIONS.name()));
	}

	@Override
	public int getMaxTimestep() throws LogException {
		return maxCycle;
	}

	@Override
	public WorldModel<? extends Entity> getWorldModel(int time)
			throws LogException {

		return worldModels.get(time);
	}

	@Override
	public Set<EntityID> getEntitiesWithUpdates(int time) throws LogException {
		return entitiesWithUpdates.get(time);
	}

	@Override
	public PerceptionRecord getPerception(int time, EntityID entity)
			throws LogException {
		LogProto log = readFromFile(time + "/" + RecordType.PERCEPTION.name()
				+ "/" + entity.getValue());
		if (log != null)
			return new PerceptionRecord(log);
		return null;
	}

	@Override
	public CommandsRecord getCommands(int time) throws LogException {
		LogProto log = readFromFile(time + "/" + RecordType.COMMANDS.name());
		if (log != null)
			return new CommandsRecord(log);
		return null;
	}

	@Override
	public UpdatesRecord getUpdates(int time) throws LogException {
		LogProto log = readFromFile(time + "/" + RecordType.UPDATES.name());
		if (log != null)
			return new UpdatesRecord(log);
		return null;
	}

}
