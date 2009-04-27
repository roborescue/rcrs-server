package rescuecore2.version0.messages;

import java.util.List;

import rescuecore2.worldmodel.Entity;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.version0.messages.IntComponent;

/**
   A message for connecting an agent to the kernel.
 */
public class AKConnect extends AbstractMessage {
    private IntComponent version;
    private IntComponent tempID;
    private IntComponent agentMask;

    public AKConnect() {
        super("AK_CONNECT", MessageConstants.AK_CONNECT);
        version = new IntComponent("Version", 0);
        tempID = new IntComponent("TempID");
        agentMask = new IntComponent("Agent type mask");
        addMessageComponent(tempID);
        addMessageComponent(version);
        addMessageComponent(agentMask);
    }

    /**
       Get the version number of this request.
       @return The version number.
     */
    public int getVersion() {
        return version.getValue();
    }

    /**
       Get the temp ID of this request.
       @return The temp ID.
     */
    public int getTemporaryID() {
        return tempID.getValue();
    }

    /**
       Set the temp ID of this request.
       @param id The new temp ID.
     */
    public void setTemporaryID(int id) {
        tempID.setValue(id);
    }

    /**
       Get the agent type mask of this request.
       @return The agent type mask.
     */
    public int getAgentTypeMask() {
        return agentMask.getValue();
    }

    /**
       Set the agent type mask of this request.
       @param mask The new agent type mask.
     */
    public void setAgentTypeMask(int mask) {
        agentMask.setValue(mask);
    }
}