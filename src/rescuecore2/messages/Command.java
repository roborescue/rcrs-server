package rescuecore2.messages;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import rescuecore2.worldmodel.EntityID;

/**
   A sub-interface of Message that tags messages that are interpreted as agent commands.
 */
public interface Command {
    /**
       Get the id of the agent-controlled entity that has issued this command.
       @return The id of the agent.
     */
    public EntityID getAgentID();
}