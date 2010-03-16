package rescuecore2.components;

/**
   An interface for objects that can generate request IDs.
*/
public interface RequestIDGenerator {
    /**
       Generate a request ID.
       @return A new, unique request ID.
    */
    int generateRequestID();
}
