package rescuecore.config;

import java.io.InputStream;
import java.io.IOException;

public class Config {
    public Config() {
    }

    public Config(InputStream in) throws IOException, ConfigException {
	read(in);
    }

    public void read(InputStream in) throws IOException, ConfigException {
    }
}