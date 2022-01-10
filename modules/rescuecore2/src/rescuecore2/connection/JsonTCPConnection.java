package rescuecore2.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.google.protobuf.util.JsonFormat;

import rescuecore2.messages.protobuf.RCRSProto.MessageProto;

public class JsonTCPConnection extends TCPConnection{

	private OutputStreamWriter writer;
	private InputStreamReader reader;
	public JsonTCPConnection(Socket s) throws IOException {
		super(s);
		writer=new OutputStreamWriter(out);
		reader = new InputStreamReader(in);
	}

	@Override
	protected void serializeMessageProto(MessageProto messageProto) throws IOException {
		writer.append(JsonFormat.printer().print(messageProto));
    }
	@Override
    protected MessageProto deserializeMessageProto() throws IOException {
    	MessageProto.Builder builder=MessageProto.newBuilder();
    	JsonFormat.parser().merge(reader, builder);
    	return builder.build();
    }
}
