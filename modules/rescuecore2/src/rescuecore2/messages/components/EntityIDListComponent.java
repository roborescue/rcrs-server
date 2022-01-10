package rescuecore2.messages.components;

import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.writeInt32;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rescuecore2.messages.AbstractMessageComponent;import rescuecore2.URN;
import rescuecore2.messages.protobuf.RCRSProto.IntListProto;
import rescuecore2.messages.protobuf.RCRSProto.MessageComponentProto;
import rescuecore2.worldmodel.EntityID;

/**
   A message component that is a list of entity IDs.
 */
public class EntityIDListComponent extends AbstractMessageComponent {
    private List<EntityID> ids;

    /**
       Construct an EntityIDListComponent with no data.
       @param name The name of the component.
     */
    public EntityIDListComponent(URN name) {
        super(name);
        ids = new ArrayList<EntityID>();
    }

    /**
       Construct an EntityIDListComponent with a list of entity IDs.
       @param name The name of the component.
       @param ids The data.
     */
    public EntityIDListComponent(URN name, List<EntityID> ids) {
        super(name);
        this.ids = new ArrayList<EntityID>(ids);
    }

    /**
       Get the list of entity IDs in this component.
       @return An immutable view of the list of entity IDs.
     */
    public List<EntityID> getIDs() {
        return Collections.unmodifiableList(ids);
    }

    /**
       Set the list of entity IDs in this component.
       @param newIDs The new set of entity IDs.
     */
    public void setIDs(List<EntityID> newIDs) {
        this.ids = new ArrayList<EntityID>(newIDs);
    }

    @Override
    public void write(OutputStream out) throws IOException {
        writeInt32(ids.size(), out);
        for (EntityID next : ids) {
            writeInt32(next.getValue(), out);
        }
    }

    @Override
    public void read(InputStream in) throws IOException {
        ids.clear();
        int count = readInt32(in);
        for (int i = 0; i < count; ++i) {
            ids.add(new EntityID(readInt32(in)));
        }
    }

    @Override
    public String toString() {
        return getName() + " = " + ids.toString();
    }
	@Override
	public void fromMessageComponentProto(MessageComponentProto proto) {
		ids.clear();
		for (Integer val : proto.getEntityIDList().getValuesList()) {
			ids.add(new EntityID(val));			
        }
	}

	@Override
	public MessageComponentProto toMessageComponentProto() {
		IntListProto.Builder builder=IntListProto.newBuilder();
		for (EntityID next : ids) {
            builder.addValues(next.getValue());
        }
		return MessageComponentProto.newBuilder().setEntityIDList(builder).build();
	}
}
