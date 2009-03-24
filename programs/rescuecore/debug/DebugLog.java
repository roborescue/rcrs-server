package rescuecore.debug;

import java.io.*;
import java.util.*;
import rescuecore.*;

public class DebugLog {
	private final static Collection<DebugEntry> NO_ENTRIES = Collections.emptySet();

	private Map<String,List<List<DebugEntry>>> nameToEntries;
	private int maxTimestep;

	public DebugLog(String fileName) throws IOException, ClassNotFoundException {
		this(new File(fileName));
	}

	public DebugLog(File file) throws IOException, ClassNotFoundException {
		nameToEntries = new HashMap<String,List<List<DebugEntry>>>();
		maxTimestep = 0;
		try {
			ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
			while (true) {
				String name = in.readUTF();
				DebugEntry next = (DebugEntry)in.readObject();
				System.out.println(next);
				addEntry(name,next);
				maxTimestep = Math.max(maxTimestep,next.getTimestep());
			}
		}
		catch (EOFException e) {
		}
	}

	public Collection<String> getAllNames() {
		return nameToEntries.keySet();
	}

	public int getMaxTimestep(String name) {
		List<List<DebugEntry>> listOfLists = nameToEntries.get(name);
		if (listOfLists==null) return -1;
		return listOfLists.size()-1;
	}

	public int getMaxTimestep() {
		return maxTimestep;
	}

	public Collection<DebugEntry> getEntriesForTime(String name, int time) {
		List<List<DebugEntry>> listOfLists = nameToEntries.get(name);
		if (listOfLists==null) return NO_ENTRIES;
		if (time >= listOfLists.size()) return NO_ENTRIES;
		List<DebugEntry> result = listOfLists.get(time);
		if (result==null) return NO_ENTRIES;
		return Collections.unmodifiableCollection(result);
	}

	private void addEntry(String name, DebugEntry next) {
		List<List<DebugEntry>> listOfLists = nameToEntries.get(name);
		if (listOfLists==null) {
			listOfLists = new ArrayList<List<DebugEntry>>();
			nameToEntries.put(name,listOfLists);
		}
		int time = next.getTimestep();
		while (time >= listOfLists.size()) listOfLists.add(new ArrayList<DebugEntry>());
		List<DebugEntry> thisList = listOfLists.get(time);
		thisList.add(next);
	}
}
