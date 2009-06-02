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
                          ControlMessageConstants.KV_CONNECT_ERROR
        };
    }

    @Override
    public Message createMessage(int id, InputStream data) throws IOException {
        Message result = null;
        switch (id) {
        case ControlMessageConstants.KG_CONNECT:
            result = new KGConnect();
            break;
        case ControlMessageConstants.KG_ACKNOWLEDGE:
            result = new KGAcknowledge();
            break;
        case ControlMessageConstants.GK_CONNECT_OK:
            result = new GKConnectOK();
            break;
        case ControlMessageConstants.GK_CONNECT_ERROR:
            result = new GKConnectError();
            break;
        case ControlMessageConstants.SK_CONNECT:
            result = new SKConnect();
            break;
        case ControlMessageConstants.SK_ACKNOWLEDGE:
            result = new SKAcknowledge();
            break;
        case ControlMessageConstants.SK_UPDATE:
            result = new SKUpdate();
            break;
        case ControlMessageConstants.KS_CONNECT_OK:
            result = new KSConnectOK();
            break;
        case ControlMessageConstants.KS_CONNECT_ERROR:
            result = new KSConnectError();
            break;
        case ControlMessageConstants.VK_CONNECT:
            result = new VKConnect();
            break;
        case ControlMessageConstants.VK_ACKNOWLEDGE:
            result = new VKAcknowledge();
            break;
        case ControlMessageConstants.KV_CONNECT_OK:
            result = new KVConnectOK();
            break;
        case ControlMessageConstants.KV_CONNECT_ERROR:
            result = new KVConnectError();
            break;
            /*
              case MessageConstants.AK_CONNECT:
              result = new AKConnect();
              break;
              case MessageConstants.AK_ACKNOWLEDGE:
              result = new AKAcknowledge();
              break;
              case MessageConstants.KA_CONNECT_OK:
              result = new KAConnectOK();
              break;
              case MessageConstants.KA_CONNECT_ERROR:
              result = new KAConnectError();
              break;
              case MessageConstants.KA_SENSE:
              result = new KASense();
              break;
              case MessageConstants.KA_HEAR_SAY:
              result = new KAHearSay();
              break;
              case MessageConstants.KA_HEAR_TELL:
              result = new KAHearTell();
              break;
            */
        default:
            System.out.println("Unrecognised message ID: " + id);
            return null;
        }
        result.read(data);
        return result;
    }
}