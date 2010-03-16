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

public class TestMessage implements Message {
    private String urn;
    private int[] data;
    private String description;

    public TestMessage(String urn) {
        this(urn, "", new int[0]);
    }

    public TestMessage(String urn, String description) {
	this(urn, description, new int[0]);
    }

    public TestMessage(String urn, String description, int... data) {
	this.urn = urn;
        this.description = description;
	this.data = data;
    }

    @Override
    public int hashCode() {
	int result = urn.hashCode() ^ data.length;
	for (int i = 0; i < data.length; ++i) {
	    result = result ^ data[i];
	}
	return result;
    }

    @Override
    public boolean equals(Object other) {
	if (other instanceof TestMessage) {
	    TestMessage m = (TestMessage)other;
	    if (!this.urn.equals(m.urn)) return false;
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
    public String getURN() {
	return urn;
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
}
