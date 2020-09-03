package rescuecore2.messages;

import org.json.JSONObject;
import rescuecore2.worldmodel.EntityID;

/**
   A sub-interface of Message that tags messages that are interpreted as agent commands.
 */
public interface Command extends Message {
    /**
       Get the id of the agent-controlled entity that has issued this command.
       @return The id of the agent.
     */
    EntityID getAgentID();

    /**
       Get the timestep this command is intended for.
       @return The timestep.
     */
    int getTime();

    JSONObject toJson();
}
