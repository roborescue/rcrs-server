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
    private int id;
    private int[] data;
    private String description;

    public TestMessage(int id) {
        this(id, "", new int[0]);
    }

    public TestMessage(int id, String description) {
	this(id, description, new int[0]);
    }

    public TestMessage(int id, String description, int... data) {
	this.id = id;
        this.description = description;
	this.data = data;
    }

    @Override
    public int hashCode() {
	int result = id ^ data.length;
	for (int i = 0; i < data.length; ++i) {
	    result = result ^ data[i];
	}
	return result;
    }

    @Override
    public boolean equals(Object other) {
	if (other instanceof TestMessage) {
	    TestMessage m = (TestMessage)other;
	    if (this.id != m.id) return false;
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

    public String getName() {
	return "Test message";
    }

    public int getMessageTypeID() {
	return id;
    }

    public String getDescription() {
        return description;
    }

    public void write(OutputStream out) throws IOException {
	writeInt32(data.length, out);
	for (int next : data) {
	    writeInt32(next, out);
	}
    }

    public void read(InputStream in) throws IOException {
	data = new int[readInt32(in)];
	for (int i = 0; i < data.length; ++i) {
	    data[i] = readInt32(in);
	}
    }

    public String toString() {
        return "Test message (" + id + "): " + description + ": " + Arrays.asList(data);
    }
}