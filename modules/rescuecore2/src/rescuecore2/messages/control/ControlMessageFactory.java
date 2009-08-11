package rescuecore2.messages.control;

import java.io.InputStream;
import java.io.IOException;

import rescuecore2.messages.Message;
import rescuecore2.messages.MessageFactory;

/**
   A factory for control messages.
 */
public final class ControlMessageFactory implements MessageFactory {
    @Override
    public int[] getKnownMessageTypeIDs() {
        return new int[] {ControlMessageConstants.KG_CONNECT,
                          ControlMessageConstants.KG_ACKNOWLEDGE,
                          ControlMessageConstants.GK_CONNECT_OK,
                          ControlMessageConstants.GK_CONNECT_ERROR,
                          ControlMessageConstants.SK_CONNECT,
                          ControlMessageConstants.SK_ACKNOWLEDGE,
                          ControlMessageConstants.SK_UPDATE,
                          ControlMessageConstants.KS_CONNECT_OK,
                          ControlMessageConstants.KS_CONNECT_ERROR,
                          ControlMessageConstants.VK_CONNECT,
                          ControlMessageConstants.VK_ACKNOWLEDGE,
                          ControlMessageConstants.KV_CONNECT_OK,
                          ControlMessageConstants.KV_CONNECT_ERROR,
                          ControlMessageConstants.AK_CONNECT,
                          ControlMessageConstants.AK_ACKNOWLEDGE,
                          ControlMessageConstants.KA_CONNECT_OK,
                          ControlMessageConstants.KA_CONNECT_ERROR,
                          ControlMessageConstants.KA_SENSE,
                          ControlMessageConstants.COMMANDS,
                          ControlMessageConstants.UPDATE
        };
    }

    @Override
    public Message createMessage(int id, InputStream data) throws IOException {
        switch (id) {
        case ControlMessageConstants.KG_CONNECT:
            return new KGConnect(data);
        case ControlMessageConstants.KG_ACKNOWLEDGE:
            return new KGAcknowledge(data);
        case ControlMessageConstants.GK_CONNECT_OK:
            return new GKConnectOK(data);
        case ControlMessageConstants.GK_CONNECT_ERROR:
            return new GKConnectError(data);
        case ControlMessageConstants.SK_CONNECT:
            return new SKConnect(data);
        case ControlMessageConstants.SK_ACKNOWLEDGE:
            return new SKAcknowledge(data);
        case ControlMessageConstants.SK_UPDATE:
            return new SKUpdate(data);
        case ControlMessageConstants.KS_CONNECT_OK:
            return new KSConnectOK(data);
        case ControlMessageConstants.KS_CONNECT_ERROR:
            return new KSConnectError(data);
        case ControlMessageConstants.VK_CONNECT:
            return new VKConnect(data);
        case ControlMessageConstants.VK_ACKNOWLEDGE:
            return new VKAcknowledge(data);
        case ControlMessageConstants.KV_CONNECT_OK:
            return new KVConnectOK(data);
        case ControlMessageConstants.KV_CONNECT_ERROR:
            return new KVConnectError(data);
        case ControlMessageConstants.AK_CONNECT:
            return new AKConnect(data);
        case ControlMessageConstants.AK_ACKNOWLEDGE:
            return new AKAcknowledge(data);
        case ControlMessageConstants.KA_CONNECT_OK:
            return new KAConnectOK(data);
        case ControlMessageConstants.KA_CONNECT_ERROR:
            return new KAConnectError(data);
        case ControlMessageConstants.KA_SENSE:
            return new KASense(data);
        case ControlMessageConstants.COMMANDS:
            return new Commands(data);
        case ControlMessageConstants.UPDATE:
            return new Update(data);
        default:
            System.out.println("Unrecognised message ID: " + id);
            return null;
        }
    }
}