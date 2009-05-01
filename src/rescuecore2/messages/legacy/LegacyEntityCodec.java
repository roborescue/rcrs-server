package rescuecore2.messages.legacy;

import static rescuecore2.connection.EncodingTools.readInt32;
import static rescuecore2.connection.EncodingTools.writeInt32;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import rescuecore2.worldmodel.entities.legacy.EntityType;
import rescuecore2.worldmodel.entities.legacy.PropertyType;
import rescuecore2.worldmodel.entities.legacy.LegacyEntity;
import rescuecore2.worldmodel.entities.legacy.World;
import rescuecore2.worldmodel.entities.legacy.Road;
import rescuecore2.worldmodel.entities.legacy.Node;
import rescuecore2.worldmodel.entities.legacy.Building;
import rescuecore2.worldmodel.entities.legacy.Refuge;
import rescuecore2.worldmodel.entities.legacy.FireStation;
import rescuecore2.worldmodel.entities.legacy.AmbulanceCentre;
import rescuecore2.worldmodel.entities.legacy.PoliceOffice;
import rescuecore2.worldmodel.entities.legacy.Civilian;
import rescuecore2.worldmodel.entities.legacy.FireBrigade;
import rescuecore2.worldmodel.entities.legacy.AmbulanceTeam;
import rescuecore2.worldmodel.entities.legacy.PoliceForce;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.properties.IntProperty;
import rescuecore2.worldmodel.properties.IntArrayProperty;
import rescuecore2.worldmodel.properties.BooleanProperty;

/**
   The standard Entity codec
 */
public class LegacyEntityCodec {
    /**
       Write an entity to an output stream.
       @param e The LegacyEntity to encode.
       @param out The stream to write it to.
       @throws IOException If writing to the stream fails.
     */
    public void encode(LegacyEntity e, OutputStream out) throws IOException {
	encodeEntity(e, out);
        // Add the end-of-entity-list entry.
	writeInt32(EntityType.NULL.getID(), out);
    }

    /**
       Write a set of entities to an output stream.
       @param e The LegacyEntities to encode.
       @param out The stream to write them to.
       @throws IOException If writing to the stream fails.
     */
    public void encode(Collection<LegacyEntity> e, OutputStream out) throws IOException {
	for (LegacyEntity next : e) {
	    encodeEntity(next, out);
	}
        // Add the end-of-entity-list entry.
	writeInt32(EntityType.NULL.getID(), out);
    }

    /**
       Read an entity from an input stream.
       @param in The stream to read.
       @return A new LegacyEntity object, or null if the stream contains no further entities.
       @throws IOException If reading from the stream fails.
     */
    public LegacyEntity decode(InputStream in) throws IOException {
	EntityType type = EntityType.fromID(readInt32(in));
	if (type == EntityType.NULL) {
	    return null;
	}
        int size = readInt32(in);
	//        System.out.println("Decoding entity type " + type + " of size " + size);
        byte[] buffer = new byte[size];
        int total = 0;
        while (total < size) {
            int read = in.read(buffer, total, size - total);
            if (read == -1) {
                throw new EOFException("Broken input pipe. Read " + total + " bytes of " + size + ".");
            }
            total += read;
        }
	//        System.out.println("Finished reading data");
	InputStream entityInput = new ByteArrayInputStream(buffer);
	EntityID id = new EntityID(readInt32(entityInput));
	LegacyEntity result = instantiateEntity(type, id);
	populateEntity(result, entityInput);
	return result;
    }

    /**
       Read a set of entities from an input stream.
       @param in The stream to read.
       @return A list of new LegacyEntity objects in the order they were read from the stream, or an empty list if the stream contains no entities.
       @throws IOException If reading from the stream fails.
     */
    public List<LegacyEntity> decodeEntities(InputStream in) throws IOException {
	List<LegacyEntity> result = new ArrayList<LegacyEntity>();
	LegacyEntity next = null;
	do {
	    next = decode(in);
	    if (next != null) {
		result.add(next);
	    }
	} while (next != null);
        return result;
    }

    private void encodeEntity(LegacyEntity e, OutputStream out) throws IOException {
        ByteArrayOutputStream gather = new ByteArrayOutputStream();
	// Gather the data
	// ID then properties list
	writeInt32(e.getID().getValue(), gather);
	// Write the properties
	for (Property next : e.getProperties()) {
	    encodeProperty(next, gather);
	}
	// Write the end-of-properties entry
	writeInt32(PropertyType.NULL.getID(), gather);

        byte[] temp = gather.toByteArray();
        writeInt32(e.getType().getID(), out);
        writeInt32(temp.length, out);
        out.write(temp);
    }

    private void encodeProperty(Property p, OutputStream out) throws IOException {
	ByteArrayOutputStream gather = new ByteArrayOutputStream();
	// Gather the data
	if (p instanceof IntProperty) {
	    writeInt32(((IntProperty)p).getValue(), gather);
	}
	else if (p instanceof BooleanProperty) {
	    boolean b = ((BooleanProperty)p).getValue();
	    writeInt32(b ? 1 : 0, gather);
	}
	else if (p instanceof IntArrayProperty) {
	    IntArrayProperty a = (IntArrayProperty)p;
	    int[] values = a.getValues();
	    writeInt32(values.length, gather);
	    for (int next : values) {
		writeInt32(next, gather);
	    }
	}
	else {
	    throw new IOException("Don't know how to write properties of type " + p.getClass());
	}
	byte[] temp = gather.toByteArray();
	int type = PropertyType.fromName(p.getName()).getID();
	writeInt32(type, out);
	writeInt32(temp.length, out);
	out.write(temp);
    }

    private LegacyEntity instantiateEntity(EntityType type, EntityID id) throws IOException {
        switch (type) {
	case WORLD:
	    return new World(id);
	case ROAD:
	    return new Road(id);
	case NODE:
	    return new Node(id);
	case BUILDING:
	    return new Building(id);
	case REFUGE:
	    return new Refuge(id);
	case FIRE_STATION:
	    return new FireStation(id);
	case AMBULANCE_CENTRE:
	    return new AmbulanceCentre(id);
	case POLICE_OFFICE:
	    return new PoliceOffice(id);
	case CIVILIAN:
	    return new Civilian(id);
	case FIRE_BRIGADE:
	    return new FireBrigade(id);
	case AMBULANCE_TEAM:
	    return new AmbulanceTeam(id);
	case POLICE_FORCE:
	    return new PoliceForce(id);
	default:
	    throw new IOException("Unrecognised entity type: " + type);
	}
    }

    private void populateEntity(LegacyEntity entity, InputStream in) throws IOException {
	PropertyType type;
	do {
	    type = PropertyType.fromID(readInt32(in));
	    //	    System.out.println("Decoding property: " + type);
	    if (type != PropertyType.NULL) {
		Property prop = entity.getProperty(type.getName());
		if (prop instanceof IntProperty) {
		    int size = readInt32(in);
		    // Size should be 4
		    if (size != 4) {
			throw new IOException("Unexpected size: " + size);
		    }
		    int value = readInt32(in);
		    //		    System.out.println("Value: " + value);
		    ((IntProperty)prop).setValue(value);
		}
		else if (prop instanceof BooleanProperty) {
		    int size = readInt32(in);
		    // Size should be 4
		    if (size != 4) {
			throw new IOException("Unexpected size: " + size);
		    }
		    int value = readInt32(in);
		    //		    System.out.println("Value: " + value);
		    ((BooleanProperty)prop).setValue(value != 0);
		}
		else if (prop instanceof IntArrayProperty) {
		    int size = readInt32(in) / 4;
		    int[] values = new int[size];
		    //		    System.out.println(size + " values: ");
		    for (int i = 0; i < size; ++i) {
			values[i] = readInt32(in);
			//			System.out.println((i + 1) + ": " + values[i]);
		    }
		    ((IntArrayProperty)prop).setValues(values);
		}
	    }
	} while (type != PropertyType.NULL);
    }
}