package rescuecore2.messages.components;

import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.writeInt32;
import static rescuecore2.misc.EncodingTools.readBytes;

import rescuecore2.messages.AbstractMessageComponent;import rescuecore2.URN;
import rescuecore2.messages.protobuf.RCRSProto.MessageComponentProto;

import java.io.InputStream;
import java.io.OutputStream;

import com.google.protobuf.ByteString;

import java.io.IOException;

/**
   A raw data component to a message.
 */
public class RawDataComponent extends AbstractMessageComponent {
    private byte[] data;

    /**
       Construct a RawDataComponent with no content.
       @param name The name of the component.
     */
    public RawDataComponent(URN name) {
        super(name);
    }

    /**
       Construct a RawDataComponent with some data.
       @param name The name of the component.
       @param data The data of this component.
     */
    public RawDataComponent(URN name, byte[] data) {
        super(name);
        this.data = new byte[data.length];
        System.arraycopy(data, 0, this.data, 0, data.length);
    }

    /**
       Get the data in this message component.
       @return A copy of the data.
     */
    public byte[] getData() {
        byte[] result = new byte[data.length];
        System.arraycopy(data, 0, result, 0, data.length);
        return result;
    }

    /**
       Set the data for this message component.
       @param newData The new data.
     */
    public void setData(byte[] newData) {
        this.data = new byte[newData.length];
        System.arraycopy(newData, 0, this.data, 0, newData.length);
    }

    @Override
    public void write(OutputStream out) throws IOException {
        writeInt32(data.length, out);
        out.write(data);
    }

    @Override
    public void read(InputStream in) throws IOException {
        data = readBytes(readInt32(in), in);
    }

    @Override
    public String toString() {
        return getName() + " = " + data.length + " bytes of raw data";
    }
    
	@Override
	public void fromMessageComponentProto(MessageComponentProto proto) {
		data = proto.getRawData().toByteArray();
	}

	@Override
	public MessageComponentProto toMessageComponentProto() {
		return MessageComponentProto.newBuilder().setRawData(ByteString.copyFrom((byte[]) data)).build();
	}
}
