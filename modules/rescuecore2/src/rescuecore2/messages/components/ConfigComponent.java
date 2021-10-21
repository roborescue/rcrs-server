package rescuecore2.messages.components;

import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.writeInt32;
import static rescuecore2.misc.EncodingTools.readString;
import static rescuecore2.misc.EncodingTools.writeString;

import rescuecore2.messages.AbstractMessageComponent;import rescuecore2.URN;
import rescuecore2.messages.protobuf.RCRSProto.ConfigProto;
import rescuecore2.messages.protobuf.RCRSProto.MessageComponentProto;
import rescuecore2.config.Config;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;

/**
   A Config component to a message.
 */
public class ConfigComponent extends AbstractMessageComponent {
    private Config config;

    /**
       Construct a ConfigComponent with no content.
       @param name The name of the component.
     */
    public ConfigComponent(URN name) {
        super(name);
        config = new Config();
    }

    /**
       Construct a ConfigComponent with a specific config as content.
       @param name The name of the component.
       @param data The content.
     */
    public ConfigComponent(URN name, Config data) {
        super(name);
        this.config = data;
    }

    /**
       Get the content of this message component.
       @return The content of the component.
     */
    public Config getConfig() {
        return config;
    }

    /**
       Set the content of this message component.
       @param config The new content.
     */
    public void setConfig(Config config) {
        this.config = config;
    }

    @Override
    public void write(OutputStream out) throws IOException {
        Set<String> keys = config.getAllKeys();
        writeInt32(keys.size(), out);
        for (String key : keys) {
            writeString(key, out);
            writeString(config.getValue(key), out);
        }
    }

    @Override
    public void read(InputStream in) throws IOException {
        int count = readInt32(in);
        config = new Config();
        for (int i = 0; i < count; ++i) {
            String key = readString(in);
            String value = readString(in);
            config.setValue(key, value);
        }
    }

    @Override
    public String toString() {
        return getName() + " (" + config.getAllKeys().size() + " entries)";
    }

	@Override
	public void fromMessageComponentProto(MessageComponentProto proto) {
        config = new Config();
        for (Entry<String, String> entry : proto.getConfig().getDataMap().entrySet()) {
            config.setValue(entry.getKey(), entry.getValue());
        }		
	}

	@Override
	public MessageComponentProto toMessageComponentProto() {
		return MessageComponentProto.newBuilder().setConfig(ConfigProto.newBuilder().putAllData(config.getData())).build();
	}
}
