package rescuecore.debug;

import java.io.Serializable;
import rescuecore.RescueObject;
import java.util.*;

public abstract class DebugEntry implements Serializable {
	private int time;

	protected DebugEntry(int time) {
		this.time = time;
	}

	public int getTimestep() {
		return time;
	}

	public static class RescueObjectEntry extends DebugEntry {
		private RescueObject object;

		public RescueObjectEntry(RescueObject object, int time) {
			super(time);
			this.object = object;
		}

		public RescueObject getObject() {
			return object;
		}

		public String toString() {
			return "Object added: "+object;
		}
	}

	public static class RescueObjectCollectionEntry extends DebugEntry {
		private Collection<RescueObject> objects;

		public RescueObjectCollectionEntry(Collection<RescueObject> objects, int time) {
			super(time);
			this.objects = new HashSet<RescueObject>(objects);
		}

		public Collection<RescueObject> getObjects() {
			return objects;
		}

		public String toString() {
			return "Object collection: "+objects.size()+" objects";
		}
	}

	public static abstract class PropertyUpdateEntry extends DebugEntry {
		protected int property;
		protected int objectID;

		protected PropertyUpdateEntry(int objectID, int property, int time) {
			super(time);
			this.property = property;
			this.objectID = objectID;
		}

		public int getProperty() {
			return property;
		}

		public int getObjectID() {
			return objectID;
		}
	}

	public static class IntPropertyUpdateEntry extends PropertyUpdateEntry {
		private int newValue;

		public IntPropertyUpdateEntry(int id, int property, int value, int time) {
			super(id,property,time);
			newValue = value;
		}

		public int getNewValue() {
			return newValue;
		}

		public String toString() {
			return "Integer update: Object: "+objectID+" Property: "+property;
		}
	}

	public static class ArrayPropertyUpdateEntry extends PropertyUpdateEntry {
		private int[] newValue;

		public ArrayPropertyUpdateEntry(int id, int property, int[] value, int time) {
			super(id,property,time);
			newValue = value;
		}

		public int[] getNewValue() {
			return newValue;
		}

		public String toString() {
			return "Array update: Object: "+objectID+" Property: "+property;
		}
	}

	public static class ObjectDebugEntry extends DebugEntry {
		private final static long serialVersionUID = -7542261088352191771l;

		private Object object;

		public ObjectDebugEntry(Object object, int time) {
			super(time);
			this.object = object;
		}

		public Object getObject() {
			return object;
		}

		public String toString() {
			return "User object: "+object;
		}
	}
}
