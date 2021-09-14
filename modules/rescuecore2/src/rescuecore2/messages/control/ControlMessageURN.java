package rescuecore2.messages.control;

import rescuecore2.messages.protobuf.ControlMessageProto.MsgURN;

/**
   URNs for control messages.
 */
public enum ControlMessageURN {
    /** Kernel-GIS connect. */
//    KG_CONNECT("urn:rescuecore2:messages.control:kg_connect"),
//    /** Kernel-GIS acknowledge. */
//    KG_ACKNOWLEDGE("urn:rescuecore2:messages.control:kg_acknowledge"),
//    /** GIS-Kernel OK. */
//    GK_CONNECT_OK("urn:rescuecore2:messages.control:gk_connect_ok"),
//    /** GIS-Kernel error. */
//    GK_CONNECT_ERROR("urn:rescuecore2:messages.control:gk_connect_error"),
//
//    /** Simulator-Kernel connect. */
//    SK_CONNECT("urn:rescuecore2:messages.control:sk_connect"),
//    /** Simulator-Kernel acknowledge. */
//    SK_ACKNOWLEDGE("urn:rescuecore2:messages.control:sk_acknowledge"),
//    /** Simulator-Kernel update. */
//    SK_UPDATE("urn:rescuecore2:messages.control:sk_update"),
//    /** Kernel-Simulator OK. */
//    KS_CONNECT_OK("urn:rescuecore2:messages.control:ks_connect_ok"),
//    /** Kernel-Simulator error. */
//    KS_CONNECT_ERROR("urn:rescuecore2:messages.control:ks_connect_error"),
//    /** Kernel update broadcast. */
//    KS_UPDATE("urn:rescuecore2:messages.control:ks_update"),
//    /** Kernel commands broadcast. */
//    KS_COMMANDS("urn:rescuecore2:messages.control:ks_commands"),
//    /** Kernel commands aftershocks info. */
//    KS_AFTERSHOCKS_INFO("urn:rescuecore2:messages.control:ks_aftershocks_info"),
//
//    /** Viewer-Kernel connect. */
//    VK_CONNECT("urn:rescuecore2:messages.control:vk_connect"),
//    /** Viewer-Kernel acknowledge. */
//    VK_ACKNOWLEDGE("urn:rescuecore2:messages.control:vk_acknowledge"),
//    /** Kernel-Viewer OK. */
//    KV_CONNECT_OK("urn:rescuecore2:messages.control:kv_connect_ok"),
//    /** Kernel-Viewer error. */
//    KV_CONNECT_ERROR("urn:rescuecore2:messages.control:kv_connect_error"),
//    /** Kernel-Viewer timestep. */
//    KV_TIMESTEP("urn:rescuecore2:messages.control:kv_timestep"),
//
//    /** Agent-Kernel connect. */
//    AK_CONNECT("urn:rescuecore2:messages.control:ak_connect"),
//    /** Agent-Kernel acknowledge. */
//    AK_ACKNOWLEDGE("urn:rescuecore2:messages.control:ak_acknowledge"),
//    /** Kernel-Agent OK. */
//    KA_CONNECT_OK("urn:rescuecore2:messages.control:ka_connect_ok"),
//    /** Kernel-Agent error. */
//    KA_CONNECT_ERROR("urn:rescuecore2:messages.control:ka_connect_error"),
//    /** Kernel-Agent perception update. */
//    KA_SENSE("urn:rescuecore2:messages.control:ka_sense"),
//
//    /** Shutdown message. */
//    SHUTDOWN("urn:rescuecore2:messages.control:shutdown"),
//
//    /** New EntityID request. */
//    ENTITY_ID_REQUEST("urn:rescuecore2:messages.control:entity_id_request"),
//
//    /** New EntityID response. */
//    ENTITY_ID_RESPONSE("urn:rescuecore2:messages.control:entity_id_response");

	KG_CONNECT(MsgURN.KG_CONNECT),
	KG_ACKNOWLEDGE(MsgURN.KG_ACKNOWLEDGE),
	GK_CONNECT_OK(MsgURN.GK_CONNECT_OK),
	GK_CONNECT_ERROR(MsgURN.GK_CONNECT_ERROR),
	SK_CONNECT(MsgURN.SK_CONNECT),
	SK_ACKNOWLEDGE(MsgURN.SK_ACKNOWLEDGE),
	SK_UPDATE(MsgURN.SK_UPDATE),
	KS_CONNECT_OK(MsgURN.KS_CONNECT_OK),
	KS_CONNECT_ERROR(MsgURN.KS_CONNECT_ERROR),
	KS_UPDATE(MsgURN.KS_UPDATE),
	KS_COMMANDS(MsgURN.KS_COMMANDS),
	KS_AFTERSHOCKS_INFO(MsgURN.KS_AFTERSHOCKS_INFO),
	VK_CONNECT(MsgURN.VK_CONNECT),
	VK_ACKNOWLEDGE(MsgURN.VK_ACKNOWLEDGE),
	KV_CONNECT_OK(MsgURN.KV_CONNECT_OK),
	KV_CONNECT_ERROR(MsgURN.KV_CONNECT_ERROR),
	KV_TIMESTEP(MsgURN.KV_TIMESTEP),
	AK_CONNECT(MsgURN.AK_CONNECT),
	AK_ACKNOWLEDGE(MsgURN.AK_ACKNOWLEDGE),
	KA_CONNECT_OK(MsgURN.KA_CONNECT_OK),
	KA_CONNECT_ERROR(MsgURN.KA_CONNECT_ERROR),
	KA_SENSE(MsgURN.KA_SENSE),
	SHUTDOWN(MsgURN.SHUTDOWN),
	ENTITY_ID_REQUEST(MsgURN.ENTITY_ID_REQUEST),
	ENTITY_ID_RESPONSE(MsgURN.ENTITY_ID_RESPONSE);
	

    private String urn;

    private ControlMessageURN(String urn) {
        this.urn = urn;
    }

    ControlMessageURN(MsgURN urn) {
    	this.urn=urn.toString();
    }

	@Override
    public String toString() {
        return urn;
    }

    /**
       Convert a String to a ControlMessageURN.
       @param s The String to convert.
       @return A ConotrlMessageURN.
    */
    public static ControlMessageURN fromString(String s) {
        for (ControlMessageURN next : ControlMessageURN.values()) {
            if (next.urn.equals(s)) {
                return next;
            }
        }
        throw new IllegalArgumentException(s);
    }
}
