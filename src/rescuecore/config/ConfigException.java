package rescuecore.config;

public class ConfigException extends Exception {
    public ConfigException() {super();}
    public ConfigException(String msg) {super(msg);}
    public ConfigException(Throwable cause) {super(cause);}
    public ConfigException(String msg, Throwable cause) {super(msg, cause);}
}