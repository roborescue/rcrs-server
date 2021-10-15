package rescuecore2.connection;

import static rescuecore2.misc.EncodingTools.writeInt32;
import static rescuecore2.misc.EncodingTools.readInt32;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import rescuecore2.messages.Message;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;
import rescuecore2.registry.Registry;

public class TestMessage implements Message {
    private int urn;
    private int[] data;
    private String description;

    public TestMessage(int urn) {
        this(urn, "", new int[0]);
    }

    public TestMessage(int urn, String description) {
	this(urn, description, new int[0]);
    }

    public TestMessage(int urn, String description, int... data) {
	this.urn = urn;
        this.description = description;
	this.data = data;
    }

    @Override
    public int hashCode() {
	int result = urn ^ data.length;
	for (int i = 0; i < data.length; ++i) {
	    result = result ^ data[i];
	}
	return result;
    }

    @Override
    public boolean equals(Object other) {
	if (other instanceof TestMessage) {
	    TestMessage m = (TestMessage)other;
	    if (!(this.urn==m.urn)) return false;
	    if (this.data.length != m.data.length) return false;
	    for (int i = 0; i < data.length; ++i) {
		if (this.data[i] != m.data[i]) {
		    return false;
		}
	    }
	    return true;
	}
	return false;
    }

    @Override
    public int getURN() {
	return (urn);
    }

    public String getDescription() {
        return description;
    }

    @Override
    public void write(OutputStream out) throws IOException {
	writeInt32(data.length, out);
	for (int next : data) {
	    writeInt32(next, out);
	}
    }

    @Override
    public void read(InputStream in) throws IOException {
	data = new int[readInt32(in)];
	for (int i = 0; i < data.length; ++i) {
	    data[i] = readInt32(in);
	}
    }

    @Override
    public String toString() {
        return "Test message (" + urn + "): " + description + ": " + Arrays.asList(data);
    }

	@Override
	public MessageProto toMessageProto() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fromMessageProto(MessageProto proto) {
		// TODO Auto-generated method stub
		
	}
}
