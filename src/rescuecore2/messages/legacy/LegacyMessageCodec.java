package rescuecore2.messages.legacy;

import static rescuecore2.connection.EncodingTools.readInt32;
import static rescuecore2.connection.EncodingTools.writeInt32;
import static rescuecore2.connection.EncodingTools.readString;
import static rescuecore2.connection.EncodingTools.writeString;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.EOFException;
import java.util.Collection;
import java.util.List;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.entities.legacy.LegacyEntity;
import rescuecore2.messages.Message;
import rescuecore2.messages.MessageCodec;

/**
   The old (versions 0.50 and earlier) Robocup Rescue message codec.
 */
public class LegacyMessageCodec implements MessageCodec {
    private final LegacyEntityCodec entityCodec;

    public LegacyMessageCodec() {
	entityCodec = new LegacyEntityCodec();
    }

    @Override
    public void encode(Message m, OutputStream out) throws IOException {
        encodeImpl((LegacyMessage)m, out);
        // Add the end-of-message-list entry.
        writeInt32(MessageType.NULL.getID(), out);
    }

    @Override
    public void encode(Collection<Message> messages, OutputStream out) throws IOException {
        for (Message next : messages) {
            encodeImpl((LegacyMessage)next, out);
        }
        // Add the end-of-message-list entry.
        writeInt32(MessageType.NULL.getID(), out);
    }

    @Override
    public Message decode(InputStream in) throws IOException {
        MessageType type = MessageType.fromID(readInt32(in));
        if (type == MessageType.NULL) {
            return null;
        }
        int size = readInt32(in);
        System.out.println("Decoding message type " + type + " of size " + size);
        byte[] buffer = new byte[size];
        int total = 0;
        while (total < size) {
            int read = in.read(buffer, total, size - total);
            if (read == -1) {
                throw new EOFException("Broken input pipe. Read " + total + " bytes of " + size + ".");
            }
            total += read;
        }
        System.out.println("Finished reading data");
        switch (type) {
        case KG_CONNECT:
            return readKGConnect(buffer);
        case KG_ACKNOWLEDGE:
            return readKGAcknowledge(buffer);
        case GK_CONNECT_OK:
            return readGKConnectOK(buffer);
        case GK_CONNECT_ERROR:
            return readGKConnectError(buffer);
        default:
            throw new IOException("Unrecognised message type: " + type);
        }
    }

    private void encodeImpl(LegacyMessage m, OutputStream out) throws IOException {
        ByteArrayOutputStream gather = new ByteArrayOutputStream();
        if (m instanceof KGConnect) {
            writeKGConnect((KGConnect)m, gather);
        }
        if (m instanceof KGAcknowledge) {
            writeKGAcknowledge((KGAcknowledge)m, gather);
        }
        if (m instanceof GKConnectOK) {
            writeGKConnectOK((GKConnectOK)m, gather);
        }
        if (m instanceof GKConnectError) {
            writeGKConnectError((GKConnectError)m, gather);
        }
        byte[] temp = gather.toByteArray();
        writeInt32(m.getType().getID(), out);
        writeInt32(temp.length, out);
        out.write(temp);
    }

    private void writeKGConnect(KGConnect m, OutputStream out) throws IOException {
        writeInt32(m.getVersion(), out);
    }

    private KGConnect readKGConnect(byte[] buffer) {
        return new KGConnect(readInt32(buffer));
    }

    private void writeKGAcknowledge(KGAcknowledge m, OutputStream out) throws IOException {
    }

    private KGAcknowledge readKGAcknowledge(byte[] buffer) throws IOException {
        return new KGAcknowledge();
    }

    private void writeGKConnectOK(GKConnectOK m, OutputStream out) throws IOException {
    }

    private GKConnectOK readGKConnectOK(byte[] buffer) throws IOException {
        WorldModel model = new WorldModel();
	ByteArrayInputStream in = new ByteArrayInputStream(buffer);
	List<LegacyEntity> entities = entityCodec.decodeEntities(in);
	for (LegacyEntity next : entities) {
	    model.addEntity(next);
	}
        return new GKConnectOK(model);
    }

    private void writeGKConnectError(GKConnectError m, OutputStream out) throws IOException {
        writeString(m.getReason(), out);
    }

    private GKConnectError readGKConnectError(byte[] buffer) {
        String reason = readString(buffer);
        return new GKConnectError(reason);
    }

}