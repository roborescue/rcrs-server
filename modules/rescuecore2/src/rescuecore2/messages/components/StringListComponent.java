package rescuecore2.messages.components;

import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.readString;
import static rescuecore2.misc.EncodingTools.writeInt32;
import static rescuecore2.misc.EncodingTools.writeString;

import rescuecore2.messages.AbstractMessageComponent;import rescuecore2.URN;
import rescuecore2.messages.protobuf.RCRSProto.MessageComponentProto;
import rescuecore2.messages.protobuf.RCRSProto.StrListProto;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
   A message component that is a list of strings.
 */
public class StringListComponent extends AbstractMessageComponent {
    private List<String> data;

    /**
       Construct an StringListComponent with no data.
       @param name The name of the component.
     */
    public StringListComponent(URN name) {
        super(name);
        data = new ArrayList<String>();
    }

    /**
       Construct an StringListComponent with a list of strings.
       @param name The name of the component.
       @param data The data.
     */
    public StringListComponent(URN name, List<String> data) {
        super(name);
        this.data = new ArrayList<String>(data);
    }

    /**
       Get the list of Strings in this component.
       @return An immutable view of the list of Strings.
     */
    public List<String> getValues() {
        return Collections.unmodifiableList(data);
    }

    /**
       Set the list of values in this component.
       @param newData The new set of values.
     */
    public void setValues(List<String> newData) {
        this.data = new ArrayList<String>(newData);
    }

    /**
       Set the list of values in this component.
       @param newData The new set of values.
     */
    public void setValues(String... newData) {
        this.data = new ArrayList<String>();
        for (String s : newData) {
            data.add(s);
        }
    }

    @Override
    public void write(OutputStream out) throws IOException {
        writeInt32(data.size(), out);
        for (String next : data) {
            writeString(next, out);
        }
    }

    @Override
    public void read(InputStream in) throws IOException {
        data.clear();
        int count = readInt32(in);
        for (int i = 0; i < count; ++i) {
            data.add(readString(in));
        }
    }

    @Override
    public String toString() {
        return getName() + " = " + data.toString();
    }
    
    @Override
	public void fromMessageComponentProto(MessageComponentProto proto) {
		data.clear();
		for (String val : proto.getStringList().getValuesList()) {
			data.add(val);
        }
	}

	@Override
	public MessageComponentProto toMessageComponentProto() {
		StrListProto.Builder builder=StrListProto.newBuilder();
		for (String next : data) {
            builder.addValues(next);
        }
		return MessageComponentProto.newBuilder().setStringList(builder).build();
	}
}
