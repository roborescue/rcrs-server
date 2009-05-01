package rescuecore2.messages.legacy;

import rescuecore2.worldmodel.WorldModel;

/**
   A message the GIS sends to the kernel once a connection has been made.
 */
public class GKConnectOK extends LegacyMessage {
    private WorldModel model;

    /**
       Construct a KGConnectOK message with a world model.
       @param model The world model to send to the kernel.
     */
    public GKConnectOK(WorldModel model) {
	super(MessageType.GK_CONNECT_OK);
        this.model = model;
    }

    /**
       Get the world model.
       @return The world model.
     */
    public WorldModel getWorldModel() {
        return model;
    }
}