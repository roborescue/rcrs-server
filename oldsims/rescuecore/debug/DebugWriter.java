package rescuecore.debug;

import java.util.*;
import java.io.*;
import rescuecore.*;

public class DebugWriter {
	//	private static Map<RescueComponent,List<DebugEntry>> componentToEntries = new HashMap<RescueComponent,List<DebugEntry>>();
	private static Map<RescueComponent,DebugTarget> componentTargets = new HashMap<RescueComponent,DebugTarget>();
	private static Map<RescueComponent,String> componentNames = new HashMap<RescueComponent,String>();
	private static Map<String,DebugTarget> targets = new HashMap<String,DebugTarget>();

	public static void register(RescueComponent component,String name, String targetName) {
		DebugTarget target = createTarget(targetName);
		if (target!=null) {
			componentTargets.put(component,target);
			//			componentToEntries.put(component,new ArrayList<DebugEntry>());
			componentNames.put(component,name);
		}
	}

	public static void logInitialObjects(RescueComponent component, Collection<RescueObject> objects) {
		log(component,new DebugEntry.RescueObjectCollectionEntry(objects,0));
	}

	public static void logObjectAdded(RescueComponent component, RescueObject o, int time) {
		log(component,new DebugEntry.RescueObjectEntry(o,time));
	}

	public static void logObjectChanged(RescueComponent component, RescueObject o, Property p, int time) {
		if (p instanceof IntProperty) {
			log(component,new DebugEntry.IntPropertyUpdateEntry(o.getID(),p.getType(),((IntProperty)p).getValue(),time));
		}
		if (p instanceof ArrayProperty) {
			log(component,new DebugEntry.ArrayPropertyUpdateEntry(o.getID(),p.getType(),((ArrayProperty)p).getValues(),time));
		}
	}

	public static void logUserObject(RescueComponent component, Object o, int time) {
		log(component,new DebugEntry.ObjectDebugEntry(o,time));
	}

	private static void log(RescueComponent component, DebugEntry entry) {
		DebugTarget target = componentTargets.get(component);
		String name = componentNames.get(component);
		if (target==null) System.err.println("WARNING: Unregistered component tried to log something: "+component);
		else {
			synchronized(target) {
				try {
					target.write(name,entry);
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void flush(RescueComponent component) {
		DebugTarget target = componentTargets.get(component);
		if (target!=null) {
			synchronized(target) {
				try {
					target.flush();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void flushAll() {
		for (RescueComponent next : componentTargets.keySet()) {
			flush(next);
		}
	}

	private static DebugTarget createTarget(String target) {
		DebugTarget result = targets.get(target);
		if (result==null) {
			try {
				result = new FileDebugTarget(target);
				targets.put(target,result);
			}
			catch (IOException e) {
				System.err.println("ERROR: Could not create debug target: "+e);
				return null;
			}
		}
		return result;
	}

	private abstract static class DebugTarget {
		public abstract void write(String owner, DebugEntry entry) throws IOException;
		public abstract void flush() throws IOException;
		public abstract void close() throws IOException;
	}

	private static class FileDebugTarget extends DebugTarget {
		private ObjectOutputStream out;

		public FileDebugTarget(String fileName) throws IOException {
			out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
		}

		public void write(String owner, DebugEntry entry) throws IOException {
			out.writeUTF(owner);
			out.writeObject(entry);
		}

		public void flush() throws IOException {
			out.flush();
		}

		public void close() throws IOException {
			out.close();
			out = null;
		}
	}
}
