package rescuecore2.config;

/**
   An unchecked exception that is thrown when an application attempts to read an undefined config option.
 */
public class NoSuchConfigOptionException extends RuntimeException {
    /**
       Construct an exception.
       @param key The name of the key that does not exist in the config.
     */
    public NoSuchConfigOptionException(final String key) {
        super(key);
    }
}
