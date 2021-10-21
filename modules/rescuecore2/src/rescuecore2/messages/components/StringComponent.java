package rescuecore2.messages.components;

import static rescuecore2.misc.EncodingTools.readString;
import static rescuecore2.misc.EncodingTools.writeString;

import rescuecore2.messages.AbstractMessageComponent;import rescuecore2.URN;
import rescuecore2.messages.protobuf.RCRSProto.MessageComponentProto;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
   A string component to a message.
 */
public class StringComponent extends AbstractMessageComponent {
    private String value;

    /**
       Construct a StringComponent with no content.
       @param name The name of the component.
     */
    public StringComponent(URN name) {
        super(name);
        value = "";
    }

    /**
       Construct a StringComponent with a specific value.
       @param name The name of the component.
       @param value The value of this component.
     */
    public StringComponent(URN name, String value) {
        super(name);
        this.value = value;
    }

    /**
       Get the value of this message component.
       @return The value of the component.
     */
    public String getValue() {
        return value;
    }

    /**
       Set the value of this message component.
       @param value The value of the component.
     */
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void write(OutputStream out) throws IOException {
        writeString(value, out);
    }

    @Override
    public void read(InputStream in) throws IOException {
        value = readString(in);
    }

    @Override
    public String toString() {
        return getName() + " = " + value;
    }
    
	@Override
	public void fromMessageComponentProto(MessageComponentProto proto) {
		value = proto.getStringValue();
	}

	@Override
	public MessageComponentProto toMessageComponentProto() {
		return MessageComponentProto.newBuilder().setStringValue(value).build();
	}
}
