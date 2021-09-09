package rescuecore2.log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import rescuecore2.config.Config;
import rescuecore2.messages.control.ControlMessageProto.ConfigLogProto;
import rescuecore2.messages.control.MsgProtoBuf;

/**
   A configuration record.
*/
public class ConfigRecord implements LogRecord {
    private Config config;

    /**
       Construct a new ConfigRecord.
       @param config The Config to record.
     */
    public ConfigRecord(Config config) {
        this.config = config;
    }

    /**
       Construct a new ConfigRecord and read data from an InputStream.
       @param in The InputStream to read from.
       @throws IOException If there is a problem reading the stream.
     */
    public ConfigRecord(InputStream in) throws IOException {
        read(in);
    }

    @Override
    public RecordType getRecordType() {
        return RecordType.CONFIG;
    }

    @Override
    public void write(OutputStream out) throws IOException {
    	ConfigLogProto.newBuilder()
    			.setConfig(MsgProtoBuf.setConfigProto( this.config ))
    			.build().writeTo( out );
    }

    @Override
    public void read(InputStream in) throws IOException {
    	ConfigLogProto configLogProto =ConfigLogProto.parseFrom(in);
    	this.config = MsgProtoBuf.setConfig(configLogProto.getConfig());
    }

    /**
       Get the Config.
       @return The config.
    */
    public Config getConfig() {
        return config;
    }
}
