package rescuecore2.messages.control;

/**
   URNs for control messages.
 */
public enum ControlMessageURN {
    /** Kernel-GIS connect. */
    KG_CONNECT("urn:rescuecore2:messages.control:kg_connect"),
    /** Kernel-GIS acknowledge. */
    KG_ACKNOWLEDGE("urn:rescuecore2:messages.control:kg_acknowledge"),
    /** GIS-Kernel OK. */
    GK_CONNECT_OK("urn:rescuecore2:messages.control:gk_connect_ok"),
    /** GIS-Kernel error. */
    GK_CONNECT_ERROR("urn:rescuecore2:messages.control:gk_connect_error"),

    /** Simulator-Kernel connect. */
    SK_CONNECT("urn:rescuecore2:messages.control:sk_connect"),
    /** Simulator-Kernel acknowledge. */
    SK_ACKNOWLEDGE("urn:rescuecore2:messages.control:sk_acknowledge"),
    /** Simulator-Kernel update. */
    SK_UPDATE("urn:rescuecore2:messages.control:sk_update"),
    /** Kernel-Simulator OK. */
    KS_CONNECT_OK("urn:rescuecore2:messages.control:ks_connect_ok"),
    /** Kernel-Simulator error. */
    KS_CONNECT_ERROR("urn:rescuecore2:messages.control:ks_connect_error"),
    /** Kernel update broadcast. */
    KS_UPDATE("urn:rescuecore2:messages.control:ks_update"),
    /** Kernel commands broadcast. */
    KS_COMMANDS("urn:rescuecore2:messages.control:ks_commands"),
    /** Kernel commands aftershocks info. */
    KS_AFTERSHOCKS_INFO("urn:rescuecore2:messages.control:ks_aftershocks_info"),

    /** Viewer-Kernel connect. */
    VK_CONNECT("urn:rescuecore2:messages.control:vk_connect"),
    /** Viewer-Kernel acknowledge. */
    VK_ACKNOWLEDGE("urn:rescuecore2:messages.control:vk_acknowledge"),
    /** Kernel-Viewer OK. */
    KV_CONNECT_OK("urn:rescuecore2:messages.control:kv_connect_ok"),
    /** Kernel-Viewer error. */
    KV_CONNECT_ERROR("urn:rescuecore2:messages.control:kv_connect_error"),
    /** Kernel-Viewer timestep. */
    KV_TIMESTEP("urn:rescuecore2:messages.control:kv_timestep"),

    /** Agent-Kernel connect. */
    AK_CONNECT("urn:rescuecore2:messages.control:ak_connect"),
    /** Agent-Kernel acknowledge. */
    AK_ACKNOWLEDGE("urn:rescuecore2:messages.control:ak_acknowledge"),
    /** Kernel-Agent OK. */
    KA_CONNECT_OK("urn:rescuecore2:messages.control:ka_connect_ok"),
    /** Kernel-Agent error. */
    KA_CONNECT_ERROR("urn:rescuecore2:messages.control:ka_connect_error"),
    /** Kernel-Agent perception update. */
    KA_SENSE("urn:rescuecore2:messages.control:ka_sense"),

    /** Shutdown message. */
    SHUTDOWN("urn:rescuecore2:messages.control:shutdown"),

    /** New EntityID request. */
    ENTITY_ID_REQUEST("urn:rescuecore2:messages.control:entity_id_request"),

    /** New EntityID response. */
    ENTITY_ID_RESPONSE("urn:rescuecore2:messages.control:entity_id_response");

    private String urn;

    private ControlMessageURN(String urn) {
        this.urn = urn;
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
