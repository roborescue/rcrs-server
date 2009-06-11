package rescuecore2.messages.control;

import rescuecore2.messages.Control;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.IntComponent;
import rescuecore2.messages.EntityIDComponent;
import rescuecore2.worldmodel.EntityID;

/**
   A message for acknowleding a connection to the kernel.
 */
public class AKAcknowledge extends AbstractMessage implements Control {
    private IntComponent requestID;
    private EntityIDComponent agentID;

    /**
       AKAcknowledge message with an undefined request ID component.
     */
    public AKAcknowledge() {
        super("AK_ACKNOWLEDGE", ControlMessageConstants.AK_ACKNOWLEDGE);
        requestID = new IntComponent("Request ID");
        agentID = new EntityIDComponent("Agent ID");
        addMessageComponent(requestID);
        addMessageComponent(agentID);
    }

    /**
       AKAcknowledge message with specific request ID and agent ID components.
       @param requestID The request ID.
       @param agentID The agent ID.
    */
    public AKAcknowledge(int requestID, EntityID agentID) {
        this();
        this.requestID.setValue(requestID);
        this.agentID.setValue(agentID);
    }

    /**
       Get the request ID of this acknowledgement.
       @return The request ID component.
     */
    public int getRequestID() {
        return requestID.getValue();
    }

    /**
       Get the agent ID of this acknowledgement.
       @return The agent ID component.
     */
    public EntityID getAgentID() {
        return agentID.getValue();
    }
}